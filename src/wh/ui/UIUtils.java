package wh.ui;

import arc.Core;
import arc.audio.Sound;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.EmpBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.MultiBulletType;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.*;
import wh.content.WHContent;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.ChainLightingBulletType;
import wh.entities.bullet.laser.LaserBeamBulletType;
import wh.entities.bullet.laser.LightningLinkerBulletType;
import wh.entities.bullet.laser.PositionLightningBulletType;

import java.text.DecimalFormat;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.world.meta.StatValues.*;

public final class UIUtils{
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    public static @Nullable TextArea textArea;
    //only allocate once, dont break unit tests
    static @Nullable TextureRegionDrawable noteIcon = Icon.arrowNoteSmall != null ? new TextureRegionDrawable(Icon.arrowNoteSmall) : null;

    private static final Vec2 ctrlVec = new Vec2();
    private static final DecimalFormat df = new DecimalFormat("######0.0");
    private static final Vec2 point = new Vec2(-1, -1);

    private static long lastToast;
    private static @Nullable Table pTable, floatTable;

    private UIUtils(){
    }

    /**
     * 通用的配置行背景条：支持渐变底色、进度填充与文本裁剪。
     * 供不同面板复用，避免各模块重复维护同一套绘制代码。
     */
    public static class PlanBackBar extends Table{
        public Prov<Color> barColor;
        public Prov<CharSequence> info;
        public Floatp fraction;

        public PlanBackBar(Prov<Color> barColor, Prov<CharSequence> info, Floatp fraction){
            this.barColor = barColor;
            this.info = info;
            this.fraction = fraction;
            setClip(true);
            this.left();

            Label infoLabel = this.label(() -> info.get()).left().padLeft(58f).padRight(72f).growX().get();
            infoLabel.setEllipsis(true);
            infoLabel.setWrap(false);
        }

        @Override
        public void draw(){
            float alpha = parentAlpha * color.a;
            float barWidth = width;
            float barHeight = height;

            float bgL = Tmp.c1.set(0.07f, 0.09f, 0.12f, 0.9f * alpha).toFloatBits();
            float bgR = Tmp.c2.set(0.07f, 0.09f, 0.12f, 0.28f * alpha).toFloatBits();
            Fill.quad(
            x, y, bgL,
            x + barWidth, y, bgR,
            x + barWidth, y + barHeight, bgR,
            x, y + barHeight, bgL
            );

            float progress = Mathf.clamp(fraction.get());
            float fillWidth = barWidth * progress;
            Color fillColor = barColor.get();
            if(fillWidth > 0.001f){
                float fillL = Tmp.c1.set(fillColor).mul(0.9f).a(0.82f * alpha).toFloatBits();
                float fillR = Tmp.c2.set(fillColor).mul(0.9f).a(0.22f * alpha).toFloatBits();
                Fill.quad(
                x, y, fillL,
                x + fillWidth, y, fillR,
                x + fillWidth, y + barHeight, fillR,
                x, y + barHeight, fillL
                );
            }

            Lines.stroke(1f);
            Draw.color(0f, 0f, 0f, 0.55f * alpha);
            Lines.rect(x + 0.5f, y + 0.5f, Math.max(0f, barWidth - 1f), Math.max(0f, barHeight - 1f));
            Draw.color(0f, 0f, 0f, 0.32f * alpha);
            Lines.rect(x + 1.5f, y + 1.5f, Math.max(0f, barWidth - 3f), Math.max(0f, barHeight - 3f));

            Draw.color();
            super.draw();
        }
    }

    public static void statToTable(Stats stat, Table table){
        var m = stat.toMap().keys().toSeq();
        for(int i = 0; i < m.size; i++){
            var s = stat.toMap().get(m.get(i)).keys().toSeq();
            for(int j = 0; j < s.size; j++){
                var v = stat.toMap().get(m.get(i)).get(s.get(j));
                for(int k = 0; k < v.size; k++){
                    v.get(k).display(table);
                }
            }
        }
    }

    /**
     * 将搜索框与内容列表绑定。
     * 当搜索文本变化时自动重建列表；匹配内容的内部名与本地化名。
     */
    public static <T extends UnlockableContent> Runnable bindContentSearch(TextField search, Table list, Seq<T> contents, Cons<T> rowBuilder){
        Runnable rebuild = () -> {
            if(list == null) return;
            list.clearChildren();
            if(contents == null || rowBuilder == null) return;

            String query = "";
            if(search != null && search.getText() != null){
                query = search.getText().trim().toLowerCase();
            }

            for(T content : contents){
                if(content == null) continue;
                if(!query.isEmpty() && !matchesContentQuery(content, query)) continue;
                rowBuilder.get(content);
            }
        };

        rebuild.run();
        if(search != null){
            search.changed(rebuild);
        }
        return rebuild;
    }

    private static boolean matchesContentQuery(UnlockableContent content, String query){
        String n1 = content.name == null ? "" : content.name.toLowerCase();
        String localized = content.localizedName == null ? "" : Strings.stripColors(content.localizedName);
        String n2 = localized.toLowerCase();
        return n1.contains(query) || n2.contains(query);
    }

    public static void statTurnTable(Stats stats, Table table){
        for(StatCat cat : stats.toMap().keys()){
            var map = stats.toMap().get(cat);

            if(map.size == 0) continue;

            if(stats.useCategories){
                table.add("@category." + cat.name).color(Pal.accent.cpy()).fillX();
                table.row();
            }

            for(Stat stat : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[lightgray]" + stat.localized() + ":[] ").left().top();
                    Seq<StatValue> arr = map.get(stat);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }

                }).fillX().padLeft(10);
                table.row();
            }
        }
    }

    public static void selectPos(Table parentT, Cons<Point2> cons){
        var original = parentT.touchablility;
        var parentTouchable = parentT.touchable;

        parentT.touchablility = () -> Touchable.disabled;

        boolean createTables = pTable == null || !pTable.hasParent();
        if (createTables) ctrlVec.set(camera.unproject(input.mouse()));

        if (createTables) pTable = new Table(Tex.clear) {
            {
                update(() -> {
                    if(state.isMenu()){
                        remove();
                    }else{
                        Vec2 v = camera.project(World.toTile(ctrlVec.x) * tilesize, World.toTile(ctrlVec.y) * tilesize);
                        setPosition(v.x, v.y, 0);
                    }
                });
            }

            @Override
            public void draw(){
                super.draw();
                Lines.stroke(9, Pal.gray);
                drawLines();
                Lines.stroke(3, Pal.accent);
                drawLines();
            }

            private void drawLines(){
                Lines.square(x, y, 28, 45);
                Lines.line(x - OFFSET * 4, y, 0, y);
                Lines.line(x + OFFSET * 4, y, graphics.getWidth(), y);
                Lines.line(x, y - OFFSET * 4, x, 0);
                Lines.line(x, y + OFFSET * 4, x, graphics.getHeight());
            }
        };

        if (createTables || floatTable == null || !floatTable.hasParent()) floatTable = new Table(Tex.clear) {{
            update(() -> {
                if(state.isMenu()) remove();
            });
            touchable = Touchable.enabled;
            setFillParent(true);

            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    ctrlVec.set(camera.unproject(x, y));
                    return false;
                }
            });
        }};

        pTable.clearChildren();
        pTable.button(Icon.cancel, Styles.emptyi, () -> {
            cons.get(Tmp.p1.set(World.toTile(ctrlVec.x), World.toTile(ctrlVec.y)));
            parentT.touchablility = original;
            parentT.touchable = parentTouchable;
            if (pTable != null) pTable.remove();
            if (floatTable != null) floatTable.remove();
        }).center();

        scene.root.addChildAt(Math.max(parentT.getZIndex() - 1, 0), pTable);
        scene.root.addChildAt(Math.max(parentT.getZIndex() - 2, 0), floatTable);
    }


    public static void selectTwoPos(Table parentT, Cons2<Point2, Point2> cons){
        var original = parentT.touchablility;
        var parentTouchable = parentT.touchable;
        parentT.touchablility = () -> Touchable.disabled;

        boolean createTables = pTable == null || !pTable.hasParent();
        if (createTables) ctrlVec.set(camera.unproject(input.mouse()));


        if (createTables) pTable = new Table(Tex.clear) {
            {
                update(() -> {
                    if(state.isMenu()){
                        remove();
                    }else{
                        Vec2 v = camera.project(World.toTile(ctrlVec.x) * tilesize, World.toTile(ctrlVec.y) * tilesize);
                        setPosition(v.x, v.y, 0);
                    }
                });
            }

            @Override
            public void draw(){
                super.draw();
                Lines.stroke(9, Pal.gray);
                drawLines();
                Lines.stroke(3, Pal.accent);
                drawLines();
            }

            private void drawLines(){
                Lines.square(x, y, 32, 45);
                Lines.line(x - OFFSET * 4, y, 0, y);
                Lines.line(x + OFFSET * 4, y, graphics.getWidth(), y);
                Lines.line(x, y - OFFSET * 4, x, 0);
                Lines.line(x, y + OFFSET * 4, x, graphics.getHeight());
            }
        };

        if (createTables || floatTable == null || !floatTable.hasParent()) floatTable = new Table(Tex.clear) {{
            update(() -> {
                if(state.isMenu()) remove();
            });
            touchable = Touchable.enabled;
            setFillParent(true);

            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    ctrlVec.set(camera.unproject(x, y));
                    return false;
                }
            });
        }};

        final Point2[] firstPos = {new Point2()};
        pTable.clearChildren();
        pTable.table(buttons -> {
            buttons.button("1", new TextButtonStyle(Styles.nonet){{
                font = Fonts.tech;
                overFontColor = Pal.remove.cpy().lerp(Pal.accent, 0.1f);
                fontColor = Color.white;
            }}, () -> {
                firstPos[0] = Tmp.p1.set(World.toTile(ctrlVec.x), World.toTile(ctrlVec.y));
                TextButton button = (TextButton)buttons.getCells().first().get();
                button.setText("2");
                button.clearListeners();
                button.setStyle(new TextButtonStyle(Styles.nonet){{
                    font = Fonts.tech;
                    overFontColor = Pal.remove.cpy().lerp(Pal.accent, 0.1f);
                    fontColor = Color.white;
                }});
                button.clicked(() -> {
                    Point2 secondPos = Tmp.p2.set(World.toTile(ctrlVec.x), World.toTile(ctrlVec.y));
                    cons.get(firstPos[0], secondPos);
                    parentT.touchablility = original;
                    parentT.touchable = parentTouchable;
                    if (pTable != null) pTable.remove();
                    if (floatTable != null) floatTable.remove();
                });
            }).size(90).center();

        }).center();

        scene.root.addChildAt(Math.max(parentT.getZIndex() - 1, 0), pTable);
        scene.root.addChildAt(Math.max(parentT.getZIndex() - 2, 0), floatTable);
    }


    private static void scheduleToast(Runnable run){
        long duration = (int)(3.5 * 1000);
        long since = Time.timeSinceMillis(lastToast);
        if(since > duration){
            lastToast = Time.millis();
            run.run();
        }else{
            Time.runTask((duration - since) / 1000f * 60f, run);
            lastToast += duration;
        }
    }

    public static boolean hasText(String text){
        return text != null && !text.isEmpty();
    }

    public static void applyTrigBlink(Label label, Color base){
        if(label == null) return;
        label.update(() -> {
            Color baseColor = Color.white.cpy().lerp(base, Mathf.absin(Time.time, 6f, 0.45f));
            float alpha = 0.55f + Mathf.absin(Time.time, 6f, 0.45f);
            label.setColor(baseColor.r, baseColor.g, baseColor.b, Mathf.clamp(alpha));
        });
    }

    public static void showFleetWarnHudCentered(TextureRegion region, Color color, String text, float duration){
        float width = Core.graphics.getWidth();
        float height = Core.graphics.getHeight() * 0.22f;
        TextureRegion iconRegion = WHContent.safeRegion(region);

        Table warning = new Table(Tex.paneSolid);
        warning.touchable = Touchable.enabled;
        warning.margin(8f);
        warning.table(t2 -> {
            t2.defaults().growY();
            t2.image().growX().height(Math.max(4f, height * 0.06f)).padRight(-10f).color(color);
            t2.image(iconRegion).size(Math.min(height * 0.68f, 140f)).color(color);
            t2.image().growX().height(Math.max(4f, height * 0.06f)).padLeft(-10f).color(color);
        }).growX().growY().row();

        if(hasText(text)){
            String formattedCenteredText = text.trim();
            if(!(formattedCenteredText.startsWith("<<") && formattedCenteredText.endsWith(">>"))){
                formattedCenteredText = "<< " + formattedCenteredText + " >>";
            }
            final String centeredAlertText = formattedCenteredText;
            warning.table(tText -> {
                tText.center();
                Label centeredLabel = new Label(centeredAlertText);
                centeredLabel.setWrap(true);
                centeredLabel.setAlignment(Align.center);
                applyTrigBlink(centeredLabel, color);
                tText.add(centeredLabel)
                .width(width * 0.72f).padTop(6f).center();
            }).growX().center().row();
        }

        Label skipHint = new Label("Left click to skip");
        skipHint.setAlignment(Align.center);
        skipHint.setFontScale(0.9f);
        skipHint.setColor(color);
        warning.add(skipHint).growX().padTop(4f).padBottom(2f).center().row();

        Table container = Core.scene.table();
        container.touchable = Touchable.enabled;
        container.setFillParent(true);
        container.center().add(warning).width(width).height(height);

        Runnable dismiss = () -> {
            container.clearActions();
            container.actions(
            Actions.fadeOut(0.22f, Interp.pow2Out),
            Actions.remove()
            );
        };
        warning.clicked(dismiss);

        container.actions(
        Actions.alpha(0f),
        Actions.fadeIn(0.28f, Interp.pow2In),
        Actions.delay(Math.max(0.1f, duration)),
        Actions.fadeOut(0.36f, Interp.pow2Out),
        Actions.remove()
        );
    }

    public static void showToast(Drawable icon, String text, Sound sound){
        showToast(icon, text, sound, Color.white);
    }

    public static void showToast(Drawable icon, String text, Sound sound, Color iconColor){
        if(state.isMenu()) return;

        scheduleToast(() -> {
            sound.play();

            Table table = new Table(Tex.button);
            table.update(() -> {
                if(state.isMenu() || !ui.hudfrag.shown){
                    table.remove();
                }
            });
            table.margin(12);
            table.image(icon).pad(3).color(iconColor == null ? Color.white : iconColor);
            table.add(text).wrap().width(LEN * 2).get().setAlignment(Align.center, Align.center);
            table.pack();

            //create container table which will align and move
            Table container = scene.table();
            container.top().add(table);
            container.setTranslation(0, table.getPrefHeight());
            container.actions(
            Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(2.5f),
            //nesting actions() calls is necessary so the right prefHeight() is used
            Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.remove()))
            );
        });
    }

    public static void showToastText(String text){
        if(headless || !hasText(text)) return;
        TextureRegionDrawable fl = new TextureRegionDrawable(WHContent.safeRegion(WHContent.fleet));
        showToast(fl, text, Sounds.none);
    }

    public static ImageButton selfStyleImageButton(Drawable imageUp, ImageButton.ImageButtonStyle is, Runnable listener){
        ImageButton ib = new ImageButton(new ImageButton.ImageButtonStyle(null, null, null, imageUp, null, null));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(is);
        style.imageUp = imageUp;
        ib.setStyle(style);
        if(listener != null) ib.changed(listener);
        return ib;
    }

    public static StatValue itemRangeBoosters(String unit, StatusEffect[] status, float timePeriod, float rangeBoost, float extraBoostPercent, int selectableCount, ItemStack[] items, boolean replace, Boolf<Item> filter){
        return table -> {
            table.row();
            table.table(Styles.grayPanel, panel -> {
                panel.left().top().defaults().left().top();

                panel.table(content -> {
                    content.left().top().defaults().left().padBottom(3f);

                    boolean any = false;
                    for(ItemStack stack : items){
                        if(!filter.get(stack.item)) continue;
                        any = true;
                        if(timePeriod < 0){
                            content.add(displayItem(stack.item, stack.amount, true)).left().row();
                        }else{
                            content.add(displayItem(stack.item, stack.amount, timePeriod, true)).left().row();
                        }
                    }
                    if(!any){
                        content.add("@none").color(Color.lightGray).left().row();
                    }

                    if(status != null && status.length > 0){
                        content.add(Core.bundle.get("statValue.boostStatus")).color(Pal.accent).padTop(2f).row();
                        for(StatusEffect s : status){
                            if(s == StatusEffects.none) continue;
                            content.table(row -> {
                                row.left();
                                row.add(UIUtils.selfStyleImageButton(new TextureRegionDrawable(s.uiIcon), Styles.emptyi, () -> ui.content.show(s))).size(36f).padRight(6f);
                                row.add(s.localizedName);
                            }).left().row();
                        }
                    }

                    if(replace){
                        content.add("[lightgray]" + Core.bundle.get("statValue.replace")).padTop(2f).row();
                    }

                    if(extraBoostPercent != 0f || rangeBoost != 0f){
                        content.table(extra -> {
                            extra.left().defaults().left().padRight(10f);
                            if(extraBoostPercent != 0f){
                                extra.add(Core.bundle.format("statValue.extraOverdrive", (int)extraBoostPercent));
                            }
                            if(rangeBoost != 0f){
                                extra.add(Core.bundle.format("statValue.extraRange", Strings.autoFixed(rangeBoost / tilesize, 2)));
                            }
                        }).left().padTop(2f).row();
                    }

                    content.add("[lightgray]" + Core.bundle.get("statValue.selectableCountLabel")).padTop(2f).left().row();
                    content.add("[stat]" + selectableCount).left();
                }).growX().pad(10f).left().top();
            }).growX().pad(5f).padBottom(-5f).colspan(table.getColumns()).left();
            table.row();
        };
    }

    //疑似太简洁了
    public static StatValue enhancedAmmo(ObjectMap<Item, Item> enhancerMap){
        return table -> {
            table.row();
            table.table(Styles.grayPanel, bt -> {
                bt.left().top().defaults().padRight(15);

                //强化物品 -> 主弹药列表
                ObjectMap<Item, Seq<Item>> groups = new ObjectMap<>();
                enhancerMap.each((base, enhancer) -> {
                    groups.get(enhancer, Seq::new).add(base);
                });

                int count = 0;
                for(Item enhancer : groups.keys()){
                    // 创建基础弹药图标列表
                    bt.table(baseTable -> {
                        baseTable.defaults().padRight(4);
                        for(Item base : groups.get(enhancer)){
                            baseTable.add(new Table(item -> {
                                item.image(base.uiIcon).size(24).scaling(Scaling.fit);
                                item.add(base.localizedName).padLeft(4);
                            })).padRight(2);

                            if(base == groups.get(enhancer).peek()) baseTable.add("->");
                            else baseTable.add("/");
                        }
                    });

                    bt.table(enhancerTable -> {
                        // 强化物品图标+名称
                        enhancerTable.image(enhancer.uiIcon).size(24).scaling(Scaling.fit);
                        enhancerTable.add(enhancer.localizedName).padLeft(4);
                    }).left().padRight(4);


                    if(++count % 4 == 0){
                        bt.row();
                    }
                }
            });
        };
    }

    public static void CollapseTextToTable(Table t, String text){
        Table ic = new Table();
        ic.add(text).wrap().fillX().width(500f).padTop(2).padBottom(6).left();
        ic.row();
        Collapser coll = new Collapser(ic, true);
        coll.setDuration(0.1f);
        t.row();
        t.table(st -> {
            st.add(Core.bundle.get("wh-clickToShow")).center();
            st.row();
            st.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(true)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).pad(5).size(8).center();
        }).left();
        t.row();
        t.add(coll);
        t.row();
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map){
        return ammo(map, false, false);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean showUnit){
        return ammo(map, false, showUnit);
    }

    //何以为
    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean nested, boolean showUnit){
        return table -> {

            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t : orderedKeys){
                boolean compact = t instanceof UnitType && !showUnit || nested;

                BulletType type = map.get(t);

                if(type.spawnUnit != null && type.spawnUnit.weapons.size > 0){
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), nested, false).display(table);
                    continue;
                }

                table.table(Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();
                    //no point in displaying unit icon twice
                    if(!compact && !(t instanceof Turret)){
                        bt.table(title -> {
                            title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top().with(i -> withTooltip(i, t, false));

                            title.add(t.localizedName).padRight(10).left().top();

                            if(type.displayAmmoMultiplier && type.statLiquidConsumed > 0f){
                                title.add("[stat]" + fixValue(type.statLiquidConsumed / type.ammoMultiplier * 60f) + " [lightgray]" + StatUnit.perSecond.localized());
                            }
                        });
                        bt.row();
                    }

                    if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                        bt.add(Core.bundle.format("bullet.damage", type.damage) + (type.continuousDamage() > 0 ?
                        "[lightgray] ~ [stat]" + Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized() : ""));
                    }

                    if(type.buildingDamageMultiplier != 1){
                        sep(bt, Core.bundle.format("bullet.buildingdamage", ammoStat((int)(type.buildingDamageMultiplier * 100 - 100))));
                    }

                    if(type.rangeChange != 0 && !compact){
                        sep(bt, Core.bundle.format("bullet.range", ammoStat(type.rangeChange / tilesize)));
                    }

                    if(type.shieldDamageMultiplier != 1){
                        sep(bt, Core.bundle.format("bullet.shielddamage", ammoStat((int)(type.shieldDamageMultiplier * 100 - 100))));
                    }

                    if(type.splashDamage > 0){
                        sep(bt, Core.bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                    }

                    if(type.statLiquidConsumed <= 0f && !compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)){
                        sep(bt, Core.bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
                    }

                    if(!compact && !Mathf.equal(type.reloadMultiplier, 1f)){
                        int val = (int)(type.reloadMultiplier * 100 - 100);
                        sep(bt, Core.bundle.format("bullet.reload", ammoStat(val)));
                    }

                    if(type.knockback > 0){
                        sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
                    }

                    if(type instanceof HealCone cone){
                        if(cone.healPercent > 0f){
                            sep(bt, bundle.format("bullet.wh-healpercent-per-second", Strings.autoFixed(cone.healPercent, 2)));
                        }
                        if(cone.healAmount > 0f){
                            sep(bt, bundle.format("wh-bullet.secondHealAmount", Strings.autoFixed(cone.healAmount, 2)));
                        }
                        sep(bt, bundle.format("bullet.wh-cone-range", Strings.fixed(cone.findRange / tilesize, 1)));
                        sep(bt, bundle.format("bullet.wh-cone-angle", Strings.fixed(cone.findAngle, 1)));
                    }else{
                        if(type.healPercent > 0f){
                            sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
                        }

                        if(type.healAmount > 0f){
                            sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
                        }
                    }

                    if(type.pierce || type.pierceCap != -1){
                        sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                    }

                    if(type.incendAmount > 0){
                        sep(bt, "@bullet.incendiary");
                    }

                    if(type.homingPower > 0.01f){
                        sep(bt, "@bullet.homing");
                    }

                    if(type instanceof MoveSuppressionBullet stype){
                        sep(bt, bundle.format("bullet.wh-cone-range", Strings.fixed(stype.findRange / tilesize, 1)));
                        sep(bt, bundle.format("bullet.wh-cone-angle", Strings.fixed(stype.findAngle, 1)));
                        sep(bt, bundle.get(stype.traction ? "bullet.wh-traction" : "bullet.wh-repulse"));
                        sep(bt, bundle.format("bullet.wh-force", Strings.autoFixed(stype.force, 1)));
                    }

                    if(type instanceof ApproachBullet stype){
                        if(stype.reload > 0f){
                            sep(bt, bundle.format("bullet.wh-approach-reload", Strings.autoFixed(stype.reload / 60f, 1)));
                        }
                        if(stype.bulletType != null && stype.bulletType.showStats){
                            bt.row();

                            Table ac = new Table();
                            ammo(ObjectMap.of(t, stype.bulletType), true, false).display(ac);
                            Collapser coll = new Collapser(ac, true);
                            coll.setDuration(0.1f);

                            bt.table(at -> {
                                at.left().defaults().left();
                                at.add(bundle.get("bullet.wh-approach-sub"));
                                at.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                            });
                            bt.row();
                            bt.add(coll);
                        }
                    }

                    if(type instanceof BlackHoleBulletType stype){
                        sep(bt, bundle.format("bullet.wh-blackhole-inner-range", Strings.fixed(stype.inRad / tilesize, 1)));
                        sep(bt, bundle.format("bullet.wh-blackhole-outer-range", Strings.fixed(stype.outRad / tilesize, 1)));
                        if(stype.damageInterval > 0f){
                            sep(bt, bundle.format("bullet.wh-blackhole-damage-interval", Strings.autoFixed(stype.damageInterval / 60f, 1)));
                        }
                    }

                    if(type instanceof ShieldBreakerType stype){
                        if(stype.maxShieldDamage > 0f){
                            sep(bt, bundle.format("bullet.wh-shield-breaker-max", Strings.autoFixed(stype.maxShieldDamage, 0)));
                        }
                        if(stype.fragSpawnSpacing > 0f){
                            sep(bt, bundle.format("bullet.wh-shield-breaker-interval", Strings.autoFixed(stype.fragSpawnSpacing / 60f, 1)));
                        }
                    }


                    if(type instanceof CritBulletType stype){
                        sep(bt, bundle.format("bullet.wh-crit-chance", (int)(stype.critChance * 100f)));
                        sep(bt, bundle.format("bullet.wh-crit-multiplier", Strings.fixed(stype.critMultiplier, 1)));
                    }

                    if(type instanceof DOTBulletType stype){
                        sep(bt, bundle.format("bullet.wh-continuous-splash-damage", stype.continuousDamage(), (stype.radIncrease * 60) / tilesize));
                        sep(bt, bundle.format("bullet.wh-continuous-splash-damage-radius", Strings.fixed(stype.DOTRadius / tilesize, 1)));
                    }

                    if(type instanceof LightningLinkerBulletType stype){
                        sep(bt, bundle.format("bullet.wh-lighting-per-second", stype.hitSpacing, Strings.fixed(60 / stype.hitSpacing, 2)));
                        if(stype.maxHit > 0) sep(bt, bundle.format("bullet.wh-max-hit", Strings.fixed(stype.maxHit, 2)));
                        if(stype.randomGenerateRange > 0) sep(bt, bundle.format("bullet.wh-random-generate-range", Strings.fixed(stype.randomGenerateRange / tilesize, 2)));
                        if(stype.randomGenerateChance > 0) sep(bt, bundle.format("bullet.wh-random-generate-chance", Strings.fixed(stype.randomGenerateChance, 2)));
                    }

                    if(type instanceof LaserBeamBulletType stype){
                        float maxLength = Math.max(stype.length * (1f + stype.extensionProportion), stype.maxRange);
                        sep(bt, bundle.format("bullet.wh-extension-length", Strings.fixed(maxLength / tilesize, 1)));
                        float extraDps = stype.damageInterval <= 0f ? 0f : (stype.damageMult * stype.damage) / stype.damageInterval * 60f;
                        sep(bt, bundle.format("bullet.wh-max-damage", Strings.fixed(extraDps, 1)));
                    }

                    if(type instanceof ChainLightingBulletType stype){
                        sep(bt, bundle.format("bullet.wh-max-hit", Strings.fixed(stype.maxHit, 2)));
                        sep(bt, bundle.format("bullet.wh-lightning-length", Strings.fixed(stype.length / tilesize, 2)));
                        sep(bt, bundle.format("bullet.wh-lightning-range", Strings.fixed(stype.chainRange / tilesize, 2)));
                    }

                    if(type instanceof PositionLightningBulletType stype){
                        sep(bt, bundle.format("bullet.wh-lightning-length", Strings.fixed(stype.maxRange / tilesize, 1)));
                    }

                    if(type.lightning > 0){
                        sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                    }

                    if(type instanceof LaserBulletType b && b.lightningSpacing > 0){
                        int count = (int)(b.length / b.lightningSpacing) * 2 + 2;
                        float damage = b.lightningDamage < 0 ? b.damage : b.lightningDamage;
                        sep(bt, Core.bundle.format("bullet.lightning", count, damage));
                        note(bt, Core.bundle.format("bullet.lightninginterval", Strings.autoFixed(b.lightningSpacing / tilesize, 2), Strings.autoFixed(b.lightningLength, 2))).left();
                    }

                    if(type instanceof EmpBulletType b && b.radius > 0f){
                        sep(bt, Core.bundle.format("bullet.empradius", Strings.fixed(b.radius / tilesize, 1)));
                        if(b.timeDuration > 0f && b.timeIncrease > 1f){
                            sep(bt, Core.bundle.format("bullet.empboost", Strings.autoFixed(b.timeIncrease * 100f, 2),
                            Strings.autoFixed(b.timeDuration / 60f, 1)) + " " + StatUnit.seconds.localized());
                        }
                        if(b.timeDuration > 0f && b.powerSclDecrease < 1f){
                            sep(bt, Core.bundle.format("bullet.empslowdown",
                            (b.powerSclDecrease < 1f ? "[negstat]" : "") + Strings.autoFixed((b.powerSclDecrease - 1f) * 100f, 2),
                            Strings.autoFixed(b.timeDuration / 60f, 1)) + " " + StatUnit.seconds.localized());
                        }
                        if(!Mathf.equal(b.powerDamageScl, 1f)){
                            sep(bt, Core.bundle.format("bullet.empdamage", Strings.autoFixed(b.powerDamageScl * 100f, 2)));
                        }
                        if(b.hitUnits){
                            sep(bt, Core.bundle.format("bullet.empunitdamage",
                            (b.unitDamageScl < 1f ? "[negstat]" : "") + Strings.autoFixed(b.unitDamageScl * 100f, 2)));
                        }
                    }

                    if(type.pierceArmor){
                        sep(bt, "@bullet.armorpierce");
                    }

                    if (type.armorMultiplier != 1f && !type.pierceArmor) {
                        if(type.armorMultiplier > 1f){
                            sep(bt, Core.bundle.format("bullet.armorweakness", (type.armorMultiplier)));
                        }else if(Mathf.sign(type.armorMultiplier) == 1){
                            sep(bt, Core.bundle.format("bullet.partialarmorpierce", (int) ((1 - type.armorMultiplier) * 100)));
                        }else{
                            sep(bt, Core.bundle.format("bullet.antiarmor", (-type.armorMultiplier)));
                        }
                    }

                    if(type.maxDamageFraction > 0){
                        sep(bt, Core.bundle.format("bullet.maxdamagefraction", (int)(type.maxDamageFraction * 100)));
                    }

                    if(type.suppressionRange > 0){
                        sep(bt, Core.bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
                    }

                    if(type.status != StatusEffects.none){
                        sep(bt, (type.status.hasEmoji() ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" +
                        Strings.autoFixed(type.statusDuration / 60f, 1) + "[lightgray] " + Core.bundle.get("unit.seconds"))).with(c -> withTooltip(c, type.status));
                    }

                    if(!type.targetMissiles){
                        sep(bt, "@bullet.notargetsmissiles");
                    }

                    if(!type.targetBlocks){
                        sep(bt, "@bullet.notargetsbuildings");
                    }

                    if(type.intervalBullet != null){
                        bt.row();

                        Table ic = new Table();
                        ammo(ObjectMap.of(t, type.intervalBullet), true, false).display(ic);
                        Collapser coll = new Collapser(ic, true);
                        coll.setDuration(0.1f);

                        bt.table(it -> {
                            it.left().defaults().left();

                            it.add(Core.bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                            it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.fragBullet != null){
                        bt.row();

                        Table fc = new Table();
                        ammo(ObjectMap.of(t, type.fragBullet), true, false).display(fc);
                        Collapser coll = new Collapser(fc, true);
                        coll.setDuration(0.1f);

                        bt.table(ft -> {
                            ft.left().defaults().left();

                            ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
                            ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.spawnBullets != null && type.spawnBullets.size > 0){
                        bt.row();

                        Table sc = new Table();
                        for(BulletType spawn : type.spawnBullets){
                            if(spawn.showStats) ammo(ObjectMap.of(t, spawn), true, false).display(sc);
                        }
                        Collapser coll = new Collapser(sc, true);
                        coll.setDuration(0.1f);

                        bt.table(st -> {
                            st.left().defaults().left();

                            st.add(Core.bundle.format("bullet.spawnBullets", type.spawnBullets.size));
                            if(sc.getChildren().size > 0) st.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type instanceof MultiBulletType mtype && mtype.bullets.length > 0){
                        bt.row();

                        Table mc = new Table();
                        int shown = 0;
                        for(BulletType sub : mtype.bullets){
                            if(sub == null) continue;
                            shown++;
                            int index = shown;
                            mc.table(sb -> {
                                sb.left().defaults().left();
                                sb.add(bundle.format("bullet.wh-multi-entry", index));
                            }).left().padTop(2f);
                            mc.row();
                            ammo(ObjectMap.of(t, sub), true, false).display(mc);
                        }
                        Collapser coll = new Collapser(mc, true);
                        coll.setDuration(0.1f);

                        int shownCount = shown;
                        int total = mtype.bullets.length;
                        int repeatCount = Math.max(1, mtype.repeat);
                        bt.table(mt -> {
                            mt.left().defaults().left();

                            mt.add(bundle.format("bullet.wh-multi-sub", shownCount, total, repeatCount));
                            if(mc.getChildren().size > 0) mt.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                }).padLeft(5).padTop(5).padBottom(compact ? 0 : 5).growX().margin(compact ? 0 : 10);
                table.row();
            }
        };
    }


    //add a note under a value
    private static Cell<?> note(Table table, String text){
        table.row();
        return table.table(t -> {
            if(noteIcon != null){
                noteIcon.setMinWidth(15f);
                noteIcon.setMinHeight(15f);
                t.image(noteIcon).color(Pal.stat).scaling(Scaling.fit).padRight(6).padLeft(12);
            }
            t.add(text);
        });
    }

    private static Cell<?> sep(Table table, String text){
        table.row();
        return table.add(text);
    }

    private static String ammoStat(float val){
        return (val > 0 ? "[stat]+" : "[negstat]") + Strings.autoFixed(val, 1);
    }

    private static String multStat(float val){
        return (val >= 1 ? "[stat]" : "[negstat]") + Strings.autoFixed(val, 2);
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.uiIcon;
    }
}
