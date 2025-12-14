package wh.ui;

import arc.Core;
import arc.audio.*;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.StatusEffects;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;

import java.text.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.world.meta.StatValues.*;

public final class UIUtils{
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    public static final TextArea textArea = headless ? null : new TextArea("");

    private static final Vec2 ctrlVec = new Vec2();
    private static final DecimalFormat df = new DecimalFormat("######0.0");
    private static final Vec2 point = new Vec2(-1, -1);
    private static final Table starter = new Table(Tex.paneSolid){
    };

    private static long lastToast;
    private static Table pTable = new Table(), floatTable = new Table();

    private UIUtils(){
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

        if(!pTable.hasParent()) ctrlVec.set(camera.unproject(input.mouse()));

        if(!pTable.hasParent()) pTable = new Table(Tex.clear){
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

        if(!pTable.hasParent()) floatTable = new Table(Tex.clear){{
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

        pTable.button(Icon.cancel, Styles.emptyi, () -> {
            cons.get(Tmp.p1.set(World.toTile(ctrlVec.x), World.toTile(ctrlVec.y)));
            parentT.touchablility = original;
            parentT.touchable = parentTouchable;
            pTable.remove();
            floatTable.remove();
        }).center();

        scene.root.addChildAt(Math.max(parentT.getZIndex() - 1, 0), pTable);
        scene.root.addChildAt(Math.max(parentT.getZIndex() - 2, 0), floatTable);
    }


    public static void selectTwoPos(Table parentT, Cons2<Point2, Point2> cons){
        var original = parentT.touchablility;
        var parentTouchable = parentT.touchable;
        parentT.touchablility = () -> Touchable.disabled;

        if(!pTable.hasParent()) ctrlVec.set(camera.unproject(input.mouse()));


        if(!pTable.hasParent()) pTable = new Table(Tex.clear){
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

        if(!pTable.hasParent()) floatTable = new Table(Tex.clear){{
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
                    pTable.remove();
                    floatTable.remove();
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

    public static void showToast(Drawable icon, String text, Sound sound){
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
            table.image(icon).pad(3);
            table.add(text).wrap().width(280f).get().setAlignment(Align.center, Align.center);
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

    public static ImageButton selfStyleImageButton(Drawable imageUp, ImageButton.ImageButtonStyle is, Runnable listener){
        ImageButton ib = new ImageButton(new ImageButton.ImageButtonStyle(null, null, null, imageUp, null, null));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(is);
        style.imageUp = imageUp;
        ib.setStyle(style);
        if(listener != null) ib.changed(listener);
        return ib;
    }

    public static StatValue itemRangeBoosters(String unit, StatusEffect[] status, float timePeriod, float rangeBoost, ItemStack[] items, boolean replace, Boolf<Item> filter){
        return table -> {
            table.row();
            table.table(c -> {
                for(Item item : content.items()){
                    if(!filter.get(item)) continue;
                    c.table(Styles.grayPanel, b -> {
                        b.table(it -> {
                            for(ItemStack stack : items){
                                if(timePeriod < 0){
                                    it.add(displayItem(stack.item, stack.amount, true)).pad(10f).padLeft(15f).left();
                                }else{
                                    it.add(displayItem(stack.item, stack.amount, timePeriod, true)).pad(10f).padLeft(15f).left();
                                }
                                if(items.length > 1) it.row();
                                ;
                            }
                        }).left();

                        b.table(bt -> {
                            bt.left().defaults().left();
                            if(status.length > 0){
                                for(StatusEffect s : status){
                                    if(s == StatusEffects.none) continue;
                                    bt.row();
                                    bt.add(UIUtils.selfStyleImageButton(new TextureRegionDrawable(s.uiIcon), Styles.emptyi, () -> ui.content.show(s))).padTop(2f).padBottom(6f).size(42);
                                    bt.add(s.localizedName).padLeft(5);
                                }
                                if(replace){
                                    bt.row();
                                    bt.add(Core.bundle.get("statValue.replace"));
                                }
                            }
                            bt.row();
                            if(rangeBoost != 0) bt.add("[lightgray]+[stat]" + Strings.autoFixed(rangeBoost / tilesize, 2) + "[lightgray] " + StatUnit.blocks.localized()).row();
                        }).right().grow().pad(10f).padRight(15f);
                    }).growX().pad(5).padBottom(-5).row();
                }
            }).growX().colspan(table.getColumns());
            table.row();
        };
    }

    //疑似太简洁了
    public static StatValue enhancedAmmo(ObjectMap<Item, Item> enhancerMap){
        return table -> {
            table.row();
            table.table(Styles.grayPanel, bt -> {
                bt.left().top().defaults().padRight(15);

                // 创建分组映射：强化物品 -> 主弹药列表
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
                        if(type.continuousDamage() > 0){
                            bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                        }else{
                            bt.add(Core.bundle.format("bullet.damage", type.damage));
                        }
                    }

                    if(type instanceof CritBulletType stype){
                        sep(bt, bundle.format("bullet.wh-crit-chance", (int)(stype.critChance * 100f)));
                        sep(bt, bundle.format("bullet.wh-crit-multiplier",  Strings.fixed(stype.critMultiplier,1)));
                    }

                    if(type instanceof DOTBulletType stype){
                        sep(bt, bundle.format("bullet.wh-continuous-splash-damage", stype.continuousDamage(), (stype.radIncrease*60) / tilesize));
                        sep(bt, bundle.format("bullet.wh-continuous-splash-damage-radius",  Strings.fixed(stype.DOTRadius / tilesize,1)));
                    }

                    if(type instanceof LightningLinkerBulletType stype){
                        sep(bt, bundle.format("bullet.wh-lighting-per-second",  stype.hitSpacing,  Strings.fixed(60/stype.hitSpacing,2)));
                        if(stype.maxHit>0)sep(bt, bundle.format("bullet.wh-max-hit",  Strings.fixed(stype.maxHit,2)));
                        if(stype.randomGenerateRange>0)sep(bt, bundle.format("bullet.wh-random-generate-range",  Strings.fixed(stype.randomGenerateRange/tilesize,2)));
                        if(stype.randomGenerateChance>0)sep(bt, bundle.format("bullet.wh-random-generate-chance",  Strings.fixed(stype.randomGenerateChance,2)));
                    }

                    if(type instanceof LaserBeamBulletType stype){
                        sep(bt, bundle.format("bullet.wh-extension-length",  Strings.fixed((stype.extensionProportion*stype.length)/tilesize,1)));
                        sep(bt, bundle.format("bullet.wh-max-damgae",  Strings.fixed((stype.damageMult*stype.damage)/60,1)));
                    }

                    if(type instanceof ChainLightingBulletType stype){
                        sep(bt, bundle.format("bullet.wh-max-hit",  Strings.fixed(stype.maxHit,2)));
                        sep(bt, bundle.format("bullet.wh-lightning-length",  Strings.fixed(stype.length/tilesize,2)));
                        sep(bt, bundle.format("bullet.wh-lightning-range",  Strings.fixed(stype.chainRange/tilesize,2)));
                    }

                    if(type instanceof PositionLightningBulletType stype){
                        sep(bt, bundle.format("bullet.wh-lightning-length",  Strings.fixed(stype.maxRange/tilesize,1)));
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

                    if(type.healPercent > 0f){
                        sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
                    }

                    if(type.healAmount > 0f){
                        sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
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

                    if(type.lightning > 0){
                        sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                    }

                    if(type.pierceArmor){
                        sep(bt, "@bullet.armorpierce");
                    }

                    if(type.maxDamageFraction > 0){
                        sep(bt, Core.bundle.format("bullet.maxdamagefraction", (int)(type.maxDamageFraction * 100)));
                    }

                    if(type.suppressionRange > 0){
                        sep(bt, Core.bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
                    }

                    if(type.status != StatusEffects.none&& type.statusDuration > 0&& type.status!=null){
                        sep(bt, (type.status.hasEmoji() ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" +
                        Strings.autoFixed(type.statusDuration / 60f, 1) + "[lightgray] " + Core.bundle.get("unit.seconds"))).with(c -> withTooltip(c, type.status));
                    }

                    if(type instanceof CritBulletType stype && stype.bouncing){
                        sep(bt, Core.bundle.format("@bullet.wh-bouncing"));
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
                }).padLeft(5).padTop(5).padBottom(compact ? 0 : 5).growX().margin(compact ? 0 : 10);
                table.row();
            }
        };
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