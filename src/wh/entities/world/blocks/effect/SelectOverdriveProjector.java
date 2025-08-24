package wh.entities.world.blocks.effect;

import arc.*;
import arc.func.Cons2;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.TypeIO;
import mindustry.logic.*;
import mindustry.type.StatusEffect;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.graphics.Drawn;
import wh.ui.UIUtils;

import static arc.Core.atlas;
import static mindustry.Vars.*;

public class SelectOverdriveProjector extends Block {
    public TextureRegion topRegion;
    public float reload = 60f;
    public float range = 160f;
    public float speedBoost = 1.5f;
    public float speedBoostPhase = 0.75f;
    public float useTime = 400f;
    public float phaseRangeBoost = 140f;
    public boolean hasBoost = true;
    public Color baseColor = Color.valueOf("feb380");
    public Color phaseColor = Color.valueOf("ffd59e");
    public Effect BoostEffect = smoothBoostEffect(240f);
    //unitBoost
    public StatusEffect[] status;
    public StatusEffect[] boostStatus;
    public boolean boostReplace = false;
    //SelectOverdrive
    public int maxLink = 6;

    public SelectOverdriveProjector(String name) {
        super(name);
        solid = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
        emitLight = true;
        lightRadius = 50f;
        envEnabled |= Env.space;

        configurable = true;
        saveConfig = update = true;

        config(Integer.class, (Cons2<SelectOverdriveBuild, Integer>) SelectOverdriveBuild::linkPos);
        config(Point2.class, (Cons2<SelectOverdriveBuild, Point2>) SelectOverdriveBuild::linkPos);
        config(Point2[].class, (SelectOverdriveBuild entity, Point2[] point2s) -> {
            for (Point2 p : point2s) {
                entity.linkPos(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
            }
        });
    }

    public Effect smoothBoostEffect(float lifetime) {
        float lifetime1 = 0.7f*lifetime, lifetime2= 0.4f*lifetime;
        return new MultiEffect(new Effect(lifetime1, e -> {
            float realRange = range + e.fin() * phaseRangeBoost;
            Drawn.fillOctagon(e.x, e.y, realRange*0.97f, e.fout(Interp.pow2Out),phaseColor);
        }), new Effect(lifetime2 , e -> {
            float realRange = range + e.fin() * phaseRangeBoost;
            Draw.color(phaseColor);
            Drawn.smoothSquareLine(e.x, e.y, realRange*1.44f, 4 * e.fout(Interp.pow4Out),Interp.pow4Out, e.fin(), 45,false, phaseColor);
        }).startDelay(lifetime1*0.9f)
        );
    }


    @Override
    public void load() {
        super.load();
        topRegion = atlas.find(name + "-top");

    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashSquare(baseColor, x * tilesize + offset, y * tilesize + offset, range);
        indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, range), b -> true, t -> {
            Drawf.selected(t, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
        });

    }

    @Override
    public void setStats() {
        stats.timePeriod = useTime;
        super.setStats();

        stats.add(Stat.speedIncrease, "+" + (int) (speedBoost * 100f - 100) + "%");
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.productionTime, useTime / 60f, StatUnit.seconds);

        if (hasBoost && findConsumer(f -> f instanceof ConsumeItems) instanceof ConsumeItems items) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, StatValues.itemBoosters("+{0}%", stats.timePeriod, speedBoostPhase * 100f, phaseRangeBoost, items.items));
        }

        if (status.length > 0) stats.add(Stat.abilities, t -> {
            t.row();
            t.add(Core.bundle.get("statValue.showStatus")).left();
            t.row();
            t.table(Styles.grayPanel, inner -> {
                inner.left().defaults().left();
                for (StatusEffect s : status) {
                    if (s == StatusEffects.none) continue;
                    inner.row();
                    inner.add(UIUtils.selfStyleImageButton(new TextureRegionDrawable(s.uiIcon), Styles.emptyi, () -> ui.content.show(s))).padTop(4f).padBottom(6f).size(42);
                    inner.add(s.localizedName).padLeft(5);
                }
            }).left().growX().margin(6).pad(5).padBottom(-5).row();
        });

        if (findConsumer(c -> c instanceof ConsumeItems) instanceof ConsumeItems cons) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, UIUtils.itemRangeBoosters(
                    "{0}" + StatUnit.timesSpeed.localized(),
                    boostStatus,
                    stats.timePeriod,
                    phaseRangeBoost,
                    cons.items,
                    boostReplace,
                    this::consumesItem
            ));
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("boost", (SelectOverdriveBuild entity) -> new Bar(() -> Core.bundle.format("bar.boost", Mathf.round(Math.max((entity.realBoost() * 100 - 100), 0))), () -> Pal.accent, () -> entity.realBoost() / (hasBoost ? speedBoost + speedBoostPhase : speedBoost)));
    }

    public class SelectOverdriveBuild extends Building implements Ranged {
        public float heat, charge = Mathf.random(reload), phaseHeat, smoothEfficiency, useProgress;
        protected IntSeq targets = new IntSeq(maxLink);
        protected boolean boostEffect = false;

        @Override
        public float range() {
            return range;
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;

            if (hasBoost) {
                phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
                if (phaseHeat > 0.5f) {
                    if (!boostEffect) {
                        boostEffect = true;
                        BoostEffect.at(x, y);
                    }
                } else {
                    boostEffect = false;
                }
            }

            if (charge >= reload) {
                float realRange = range + phaseHeat * phaseRangeBoost;
                charge = 0f;
                indexer.eachBlock(team, Tmp.r1.setCentered(x, y, realRange), other -> other.block.canOverdrive, other -> other.applyBoost(realBoost(), reload + 1f));
                if (hasBoost) {
                    linkBuilds().each(other -> other.applyBoost(realBoost(), reload + 1f));
                }
            }

            Units.nearby(Tmp.r1.setCentered(x, y, realBoost()), u -> {
                if (u.team == team) {
                    boolean phase = phaseHeat > 0.5f;
                    if (phase && boostStatus.length > 0) {
                        for (StatusEffect s : boostStatus) {
                            if (s == StatusEffects.none) continue;
                            u.apply(s, 10);
                        }
                    }
                    if (!boostReplace || !phase) {
                        for (StatusEffect s : status) {
                            if (s == StatusEffects.none) continue;
                            u.apply(s, 10);
                        }
                    }
                }
            });

            if (efficiency > 0) {
                useProgress += delta();
            }

            if (useProgress >= useTime) {
                consume();
                useProgress %= useTime;
            }

        }

        public void linkPos(int value) {
            Building other = world.build(value);

            if (other != null && !targets.removeValue(value) && targets.size < maxLink - 1 && within(other, range()))
                targets.add(value);
        }

        public void linkPos(Point2 point2) {
            linkPos(point2.pack());
        }
        public IntSeq linkGroup() {
            return targets;
        }

        public Seq<Building> linkBuilds() {
            Seq<Building> buildings = new Seq<>();
            for (int pos : linkGroup().shrink()) {
                Building b = world.build(pos);
                if (linkValid(b)) buildings.add(b);
                else linkGroup().removeValue(pos);
            }
            return buildings;
        }

        public boolean linkValid(Building other) {
            return other != null && other.team == team && other.block.canOverdrive && Mathf.within(x, y, other.x, other.y, range + phaseHeat * phaseRangeBoost);
        }

        public float realBoost() {
            return (speedBoost + phaseHeat * speedBoostPhase) * efficiency;
        }

        @Override
        public void drawSelect() {
            float realRange = range + phaseHeat * phaseRangeBoost;
            Drawf.dashSquare(hasBoost ? baseColor : phaseColor, x, y, realRange);


            indexer.eachBlock(this, realRange, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
        }


        private int directionLR(Building b) {
            float angle = angleTo(b);
            if (angle > 90 && angle < 270) return -1;
            else return 1;
        }

        private int directionUD(Building b) {
            float angle = angleTo(b);
            if (angle > 180 && angle < 360) return -1;
            else return 1;
        }

        @Override
        public void drawConfigure() {

            float offset = size * tilesize / 2f + 1f;

            Lines.stroke(3f, Pal.gray);
            Lines.square(x, y, offset + 1f);

            Seq<Building> buildings = linkBuilds();

            for (Building b : buildings) {
                float targetOffset = b.block.size * tilesize / 2f + 1f;
                float angle = angleTo(b);
                boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45) || Mathf.equal(angle, 360, 45);

                float edgeX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                float edgeY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;

                boolean verticalY = (edgeX >= x - offset && edgeX <= x + offset);
                boolean verticalX = (edgeY >= y - offset && edgeY <= y + offset);

                boolean onSameXAxis = Mathf.zero(b.x - x, 0.1f);
                boolean onSameYAxis = Mathf.zero(b.y - y, 0.1f);

                float fromX, toX, fromY, toY;

                if (verticalY || verticalX) {
                    fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                    toX = b.x + Mathf.num(horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(!horizontal) * -directionUD(b) * targetOffset;
                } else {
                    fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                    toX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;
                }


                if (verticalY) {
                    Tmp.v1.set(fromX, (fromY + toY) / 2);
                    Tmp.v2.set(toX, (fromY + toY) / 2);
                } else if (verticalX) {
                    Tmp.v1.set((fromX + toX) / 2, fromY);
                    Tmp.v2.set((fromX + toX) / 2, toY);
                } else {
                    Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);
                }

                Draw.color(Pal.gray);
                Lines.stroke(3);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);
                if ((verticalX || verticalY) && !onSameXAxis && !onSameYAxis) {
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                } else
                    Lines.linePoint(Tmp.v1.x - (horizontal ? 0.15f * (Tmp.v1.x - fromX) : 0), Tmp.v1.y - (!horizontal ? 0.15f * (Tmp.v1.y - fromY) : 0));
                Lines.linePoint(toX, toY);
                Lines.endLine();
                Lines.stroke(3);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);

                Draw.reset();
                Draw.color(Pal.gray);
                float ang;
                if (verticalX || verticalY) {
                    ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, toX, toY);
                } else ang = Angles.angle(Tmp.v1.x, Tmp.v1.y, toX, toY);
                Draw.rect(atlas.find("logic-node"), toX, toY, 12 / 2f, 12 / 2f, ang);
            }

            for (Building b : buildings) {
                float targetOffset = b.block.size * tilesize / 2f + 1f;
                float angle = angleTo(b);
                boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45) || Mathf.equal(angle, 360, 45);

                float edgeX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                float edgeY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;

                boolean verticalY = (edgeX >= x - offset && edgeX <= x + offset);
                boolean verticalX = (edgeY >= y - offset && edgeY <= y + offset);

                boolean onSameXAxis = Mathf.zero(b.x - x, 0.1f);
                boolean onSameYAxis = Mathf.zero(b.y - y, 0.1f);

                float fromX, toX, fromY, toY;

                if (verticalY || verticalX) {
                    fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                    toX = b.x + Mathf.num(horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(!horizontal) * -directionUD(b) * targetOffset;
                } else {
                    fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                    toX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;
                }

                if (verticalY) {
                    Tmp.v1.set(fromX, (fromY + toY) / 2);
                    Tmp.v2.set(toX, (fromY + toY) / 2);
                } else if (verticalX) {
                    Tmp.v1.set((fromX + toX) / 2, fromY);
                    Tmp.v2.set((fromX + toX) / 2, toY);
                } else {
                    Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);
                }

                Draw.color(baseColor);
                Lines.stroke(1);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);

                if ((verticalX || verticalY) && !onSameXAxis && !onSameYAxis) {
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                } else
                    Lines.linePoint(Tmp.v1.x - (horizontal ? 0.15f * (Tmp.v1.x - fromX) : 0), Tmp.v1.y - (!horizontal ? 0.15f * (Tmp.v1.y - fromY) : 0));

                Lines.linePoint(toX, toY);
                Lines.endLine();
                Lines.stroke(1);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);
                Draw.reset();
                Draw.color(baseColor);
                float ang;
                if (verticalX || verticalY) {
                    ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, toX, toY);
                } else ang = Angles.angle(Tmp.v1.x, Tmp.v1.y, toX, toY);
                Draw.rect(atlas.find("logic-node"), toX, toY, 12 / 2f, 12 / 2f, ang);

            }


            Lines.stroke(1f, baseColor);
            Lines.square(x, y, offset);
            float realRange = range + phaseHeat * phaseRangeBoost;
            Drawf.dashSquare(hasBoost ? baseColor : phaseColor, x, y,realRange );
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other != null && within(other, range())) {
                configure(other.pos());
                return false;
            }
            return true;
        }

        @Override
        public Point2[] config() {
            Point2[] out = new Point2[targets.size];
            for (int i = 0; i < out.length; i++) {
                out[i] = Point2.unpack(targets.get(i)).sub(tile.x, tile.y);
            }
            return out;
        }

        @Override
        public void draw() {
            super.draw();

            float f = 1f - (Time.time / 100f) % 1f;

            Draw.color(baseColor, phaseColor, phaseHeat);
            Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.1f) * heat);

            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for (int i = 0; i < 4; i++) {
                Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if (f < 0.5f)
                    Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);

            Draw.reset();
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
            write.f(phaseHeat);
            TypeIO.writeObject(write, targets);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
            phaseHeat = read.f();
            targets = (IntSeq) TypeIO.readObject(read);
        }
    }
}

