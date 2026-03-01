package wh.entities.event.ui;

import arc.*;
import arc.flabel.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;

/**
 * 过场 UI 桥接层。
 * 用于显示 signal/info/curtain 等逻辑动作效果。
 */
public class WHCutsceneUI{
    private static final float OVERLAY_SPEED = 0.0065f;

    public WidgetGroup root;
    public WidgetGroup curtain;

    public Table textTable = new Table();
    public Table textArea = new Table();
    public Table infoTable = new Table();

    public FLabel textLabel = new FLabel("");
    public FLabel infoLabel = new FLabel("");

    public boolean controlOverride = false;
    public float curtainProgress = 0f;
    public float targetOverlayAlpha = 0f;
    public float overlayAlphaShiftSpeed = OVERLAY_SPEED;

    private boolean built = false;

    /** 确保 UI 节点已创建并挂载。 */
    public void ensureSetup(){
        if(Vars.headless || built) return;
        if(Core.scene == null || Core.scene.root == null) return;

        buildRoot();
        buildCurtain();
        buildTextTable();
        buildInfoTable();
        attach();
        reset();
        built = true;
    }

    private void buildRoot(){
        root = new WidgetGroup();
        root.setFillParent(true);
        root.touchable = Touchable.childrenOnly;
    }

    private void buildCurtain(){
        curtain = new WidgetGroup(){
            {
                color.a = 1f;
                setFillParent(true);
                touchable = Touchable.disabled;
            }

            @Override
            public void draw(){
                super.draw();

                float width = Core.graphics.getWidth();
                float height = Core.graphics.getHeight();

                float curtainScale = Core.graphics.isPortrait() ? 0.22f : 0.1185f;
                float heightC = height * curtainScale * Interp.pow2Out.apply(curtainProgress);

                // 上下黑边。
                Draw.color(Color.black);
                Draw.alpha(Interp.pow3Out.apply(Mathf.curve(curtainProgress, 0f, 0.75f)));
                Fill.quad(0f, 0f, 0f, heightC, width, heightC, width, 0f);
                Fill.quad(0f, height, 0f, height - heightC, width, height - heightC, width, height);
                Draw.reset();

                // 全屏遮罩淡入淡出。
                Draw.color(0f, 0f, 0f, color.a);
                Fill.quad(0f, 0f, 0f, height, width, height, width, 0f);
                Draw.reset();
            }
        };
    }

    private void buildTextTable(){
        textTable = new Table(Tex.buttonEdge3);
        textTable.touchable(() -> Touchable.disabled);
        textTable.visible(() -> Vars.state != null && Vars.state.isGame());
        textTable.color.a = 0f;

        textTable.pane(Styles.smallPane, t -> {
            textArea = t;
            textArea.defaults().grow().pad(2f);
            textArea.exited(() -> Core.scene.unfocus(textArea));
            t.fillParent = true;
        }).grow();

        textTable.update(() -> {
            float width = Core.graphics.getWidth();
            float height = Core.graphics.getHeight();

            if(Vars.mobile){
                textTable.setSize(width, height * 0.22f);
                textTable.setPosition(0f, 0f);
            }else{
                textTable.setSize(Scl.scl(width * 0.65f), Scl.scl(height * 0.1f));
                textTable.setPosition((width - textTable.getWidth()) / 2f, height * 0.14f);
            }
        });
    }

    private void buildInfoTable(){
        infoTable = new Table(Tex.clear);
        infoTable.touchable(() -> Touchable.disabled);
        infoTable.visible(() -> Vars.state != null && Vars.state.isGame());
        infoTable.color.a = 0f;

        infoTable.update(() -> {
            float width = Core.graphics.getWidth();
            float height = Core.graphics.getHeight();

            if(Vars.mobile){
                infoTable.setSize(width, height * 0.4f);
                infoTable.setPosition(0f, 0f);
            }else{
                infoTable.setSize(Scl.scl(width * 0.25f), Scl.scl(height * 0.1f));
                infoTable.setPosition(width * 0.05f, height * 0.1f);
            }
        });
    }

    private void attach(){
        Vars.control.input.addLock(() -> controlOverride);
        Core.scene.root.addChildAt(0, root);
        root.addChild(curtain);
        root.addChild(textTable);
        root.addChild(infoTable);
    }

    /** 每帧更新遮罩 alpha。 */
    public void update(){
        if(Vars.headless || !built) return;
        curtain.color.a = Mathf.approachDelta(curtain.color.a, targetOverlayAlpha, overlayAlphaShiftSpeed);
    }

    /** 重置过场 UI 状态。 */
    public void reset(){
        if(Vars.headless || !built) return;

        controlOverride = false;
        curtainProgress = 0f;
        targetOverlayAlpha = 0f;
        overlayAlphaShiftSpeed = OVERLAY_SPEED;
        curtain.color.a = targetOverlayAlpha;

        infoLabel = new FLabel("");
        infoTable.clear();
        infoTable.add(infoLabel);
        infoTable.actions(Actions.alpha(0f));

        textLabel = new FLabel("");
        textArea.clear();
        textArea.add(textLabel).pad(4f, 32f, 4f, 32f);
        textTable.actions(Actions.alpha(0f));
    }

    /**
     * 预留接口：在世界中显示标记。
     * 当前仅保留调用点，具体渲染可后续补充。
     */
    public void mark(float x, float y, float radius, float lifetime, Color color, MarkStyle style){
    }
}
