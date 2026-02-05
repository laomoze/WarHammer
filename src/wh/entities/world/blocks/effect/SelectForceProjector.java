package wh.entities.world.blocks.effect;


import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.graphics.*;
import wh.util.*;

import static arc.Core.atlas;
import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.*;

public class SelectForceProjector extends Block {
    // 基本参数
    public TextureRegion topRegion;
    public float range = 500f;
    public int maxLink = 4;
    public int maxMount = 4;

    // 护盾参数
    public final int timerUse = timers++;
    public float OneTileShieldHealth = 200f;
    public float shieldRecovery = 0.1f;
    public float healPercent = 0.08f;
    public float shieldPercent = 0.1f;
    public float cooldownNormal = 1f;
    public float cooldownBrokenBase = 0.6f;
    public float cooldownLiquid = 1.1f;
    public boolean consumeCoolant = true;
    public float coolantAmount = 0.3f;

    public float phaseUseTime = 350f;
    public float phaseShieldBoost = 400f;
    public int trailLength = 30;
    public Color baseColor = WHPal.SkyBlue;

    public @Nullable Consume itemConsumer, coolantConsumer;

    public Effect shieldBreakEffect = new Effect(120, e -> {
        color(e.color);
        stroke(6f * e.fout());
        int size = (int) e.data;
        Lines.square(e.x, e.y, size * tilesize / 2f);
    }).followParent(true);

    public Effect absorbEffect = Fx.absorb;

    public SelectForceProjector(String name) {
        super(name);
        solid = true;
        group = BlockGroup.projectors;
        hasPower = true;
        emitLight = true;
        lightRadius = 50f;
        envEnabled |= Env.space;
        configurable = true;
        saveConfig = update = true;

        consumePower(2f);

        if(consumeCoolant){
            consume(coolantConsumer = new ConsumeCoolant(coolantAmount)).boost().update(false);
        }

        config(Integer.class, (ForceWallBuild entity, Integer value) -> entity.linkPos(value));
        config(Point2.class, (ForceWallBuild entity, Point2 point2) -> entity.linkPos(point2.pack()));
        config(Point2[].class, (ForceWallBuild entity, Point2[] point2s) -> {
            for (Point2 p : point2s) {
                entity.linkPos(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
            }
        });
    }


    @Override
    public void load() {
        super.load();
        topRegion = atlas.find(name + "-top");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.shieldHealth, Core.bundle.format("stat.wh-shield-health-detail", OneTileShieldHealth, shieldPercent));
    }


    @Override
    public void init() {
        super.init();
        clipSize = Math.max(clipSize, range);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("warmup", (ForceWallBuild entity) -> new Bar(
                () -> Core.bundle.get("bar.wh-warmup") + ": " + Mathf.round(entity.warmup * 100) + "%",
                () -> Pal.lightOrange,
                () -> entity.warmup
        ));

        addBar("team-builds", (ForceWallBuild entity) -> new Bar(
        () -> Core.bundle.get("bar.wh-amount")+ Groups.build.count(b -> b.team == entity.team && b.block == this)+" / " + maxMount,
        () -> Pal.accent,
        () -> (float)Groups.build.count(b -> b.team == entity.team && b.block == this) / maxMount
        ));

    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (team != Team.derelict && Groups.build.count(b -> b.team == team && b.block == this) >= maxMount) {
            drawPlaceText("Maximum Placement Quantity Reached", tile.x, tile.y, false);
            return false;
        }
        return super.canPlaceOn(tile, team, rotation);
    }


    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashSquare(baseColor, x * tilesize + offset, y * tilesize + offset, range);
        indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, range), b -> true, t -> {
            Drawf.selected(t, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
        });

    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region, topRegion};
    }


    public class ForceWallBuild extends Building implements Ranged {
        protected IntSeq targets = new IntSeq(maxLink);
        public Seq<ShieldData> shieldData = new Seq<>();
        protected float shieldHit = 0f;
        public float warmup, phaseHeat, trailWarmUp;
        private final Trail[] sideTrails = new Trail[2];

        @Override
        public void drawSelect() {
            Drawf.dashSquare(baseColor, x, y, range);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public void updateTile() {
            if (/*efficiency >= 0.9999f &&*/ power.status >= 0.99f) {
                warmup = Mathf.lerpDelta(warmup, 1f, 0.006f);
                trailWarmUp = Mathf.lerpDelta(trailWarmUp, 1f, 0.01f);
                if (Mathf.equal(warmup, 1f, 0.08f)) {
                    warmup = 1f;
                }
            } else {
                warmup = Mathf.lerpDelta(warmup, 0f, 0.1f);
                trailWarmUp = Mathf.lerpDelta(trailWarmUp, 0f, 0.1f);
            }
            if (warmup >= 0.95f) {
                updateShields();
                absorbBullets();
            }
            updateTrail();
        }

        private float MaxHealth(ShieldData shield) {
            return (OneTileShieldHealth + (phaseHeat * phaseShieldBoost)) * Mathf.pow(shield.building.hitSize() / tilesize, 2) + shield.building.maxHealth * shieldPercent;
        }

        private float MaxHeal(ShieldData shield) {
            return delta() * shieldRecovery * Mathf.pow(shield.building.hitSize() / tilesize
                    , 2) + shield.building.maxHealth() * (healPercent) / 100f * efficiency;
        }

        private void updateShields() {
            shieldData.removeAll(s ->
                    s.building == null ||
                            s.building.dead ||
                            !linkValid(s.building) ||
                            !targets.contains(s.building.pos())
            );

            for (Building b : linkBuilds()) {
                if (shieldData.find(s -> s.building == b) == null) {
                    ShieldData shield = new ShieldData();
                    shield.building = b;
                    shieldData.add(shield);
                }
            }

            boolean phaseValid = itemConsumer != null && itemConsumer.efficiency(this) > 0;

            phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(phaseValid), 0.1f);

            for (ShieldData shield : shieldData) {
                float regen = shieldRecovery * Mathf.pow(shield.building.block.size, 2) * warmup;
                float scale = !shield.broken ? regen*cooldownNormal : regen * cooldownBrokenBase;
                if (shield.buildup > 0) {

                    if (coolantConsumer != null && coolantConsumer.efficiency(this) > 0) {
                        scale *= (cooldownLiquid * (1f + (liquids.current().heatCapacity - 0.4f) * 0.9f));
                    }

                    shield.buildup -= delta() * scale;

                }

                if (phaseValid && !shield.broken && timer(timerUse, phaseUseTime) && efficiency > 0) {
                    consume();
                }

                if (!shield.broken) shield.building.heal(MaxHeal(shield));

                if (shield.broken && shield.buildup <= 0) {
                    shield.broken = false;
                }

                if (!shield.broken && shield.buildup >= MaxHealth(shield)) {
                    shield.broken = true;
                    shieldBreakEffect.at(shield.building.x, shield.building.y,
                            shield.building.block.size * tilesize / 2f, team.color,
                            shield.building.block.size);
                    if (team != state.rules.defaultTeam) {
                        Events.fire(EventType.Trigger.forceProjectorBreak);
                    }
                }

                shield.shieldSize = Mathf.lerpDelta(shield.shieldSize,
                        shield.broken ? 0 : 1, 0.1f);

                if (shield.hit > 0) shield.hit -= delta() / 5f;
            }

            if (shieldHit > 0) shieldHit -= delta() / 5f;

            if (Mathf.chanceDelta(shieldData.sumf(s -> s.buildup / OneTileShieldHealth) * 0.008f)) {
                new Effect(33.0f, e -> {
                    Draw.color(WHPal.SkyBlue, Items.titanium.color.cpy(), e.fin() * 0.8f);
                    Lines.stroke(3.0f * e.fout());
                    Lines.spikes(e.x, e.y, 10.0f * e.finpow(), 2.0f * e.fout() + 4.0f * e.fslope(), 4, 45.0f);
                }).at(x + Mathf.range(tilesize / 2f),
                        y + Mathf.range(tilesize / 2f));
            }
        }

        private void absorbBullets() {
            for (ShieldData shield : shieldData) {
                if (!shield.broken && shield.building != null) {
                    float radius = (shield.building.block.size * tilesize) + 2;

                    Groups.bullet.intersect(
                            shield.building.x - radius, shield.building.y - radius,
                            radius * 2f, radius * 2f,
                            bullet -> {
                                if (bullet.team != team /*&& bullet.type.absorbable && !bullet.absorbed*/ &&
                                        Intersector.isInRegularPolygon(4, shield.building.x, shield.building.y, radius, 45, bullet.x, bullet.y)) {
                                    bullet.absorb();
                                    absorbEffect.at(bullet.x, bullet.y, radius, team.color);
                                    shield.buildup += bullet.type.shieldDamage(bullet);
                                    shield.hit = 1f;
                                    shieldHit = 1f;
                                }
                            }
                    );
                }
            }
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

     /*   public void linkPos(int value) {
            Building other = world.build(value);
            if (other != null && !targets.removeValue(value) && targets.size < maxLink-1 && within(other, range)) {
                targets.add(value);
            }
        }*/


        public void linkPos(int value) {
            Building other = world.build(value);
            if (other != this && other != null && !targets.removeValue(value) && targets.size < maxLink - 1 && within(other, range)) {
                ShieldData existing = shieldData.find(s -> s.building == other);
                if (existing == null) {
                    ShieldData newShield = new ShieldData();
                    newShield.building = other;
                    newShield.buildup = MaxHealth(newShield) - 1f;
                    shieldData.add(newShield);
                }
                targets.add(value);
            }
        }


        public Seq<Building> linkBuilds() {
            Seq<Building> buildings = new Seq<>();
            for (int pos : targets.shrink()) {
                Building b = world.build(pos);
                if (linkValid(b)) buildings.add(b);
                else targets.removeValue(pos);
            }
            return buildings;
        }

        public boolean linkValid(Building other) {
            return other != null &&
                    other.team == team &&
                    Mathf.within(x, y, other.x, other.y, range) &&
                    !Groups.build.contains(b -> b != this &&
                            b.block instanceof SelectForceProjector &&
                            ((ForceWallBuild) b).targets.contains(other.pos()));
        }

        @Override
        public void draw() {
            super.draw();
            drawBlock();
            drawShield();

        }

        public void drawShield() {
            for (ShieldData shield : shieldData) {
                if (!shield.broken && shield.building != null) {
                    float radius = shield.building.block.size * tilesize / 2f * shield.shieldSize * Mathf.clamp(warmup * 3, 0, 1);
                    color(team.color, Color.white, Mathf.clamp(shield.hit));

                    Draw.z(Layer.shields);

                    if (renderer.animateShields) {
                        Fill.square(shield.building.x, shield.building.y, radius);
                    } else {
                        stroke(1.5f);
                        Draw.alpha(0.09f + Mathf.clamp(0.08f * shield.hit));
                        Fill.square(shield.building.x, shield.building.y, radius);
                        Draw.alpha(1f);
                        Lines.poly(shield.building.x, shield.building.y, 4, radius, 45f);
                        Draw.reset();
                    }
                }
            }
            Draw.reset();
        }


        public void updateTrail() {
            for (int i = 0; i < 2; i++) {
                if (sideTrails[i] == null) {
                    sideTrails[i] = new Trail(trailLength);
                }
                var trailOffset = 32 * 2 / tilesize;
                float progress = trailWarmUp,
                        offset = (i == 0 ? trailOffset : -trailOffset),
                        angle = Time.time * 3f % 360f * progress * (i == 0 ? 1 : -1);
                float tx = WHUtils.ellipseXY(x + offset, y, size * progress * tilesize * 0.32f, size * progress * tilesize * 0.18f, rotation - 90, angle, 0);
                float ty = WHUtils.ellipseXY(x + offset, y, size * progress * tilesize * 0.32f, size * progress * tilesize * 0.18f, rotation - 90, angle, 1);
                sideTrails[i].update(tx, ty, 1.2f);
            }
        }

        public void drawBlock() {
            Draw.rect(region, x, y);
            Draw.blend(Blending.additive);
            float wOffset = 32f * 2 / tilesize, hOffset = 25f * 2 / tilesize;
            Draw.color(WHPal.SkyBlue);
            for (int h = 1; h < 3; h++) {
                for (int s : Mathf.signs) {
                    float offsetY = y + hOffset * h * s;
                    for (int n : Mathf.signs) {
                        float offsetX = x + wOffset * n;
                        for (int m : Mathf.signs) {
                            /*Lines.stroke(1.5f * warmup * (1 - Mathf.sin(10, 0.15f)));
                            Lines.lineAngle(offsetX, offsetY, rotation + 90 + m * 90f, 7 * warmup * (1 - Mathf.sin(10, 0.7f)));*/
                            Drawf.flameFront(offsetX, offsetY + Mathf.sin(Time.time + Mathf.PI * h * 3, 10, 0.9f)
                                    , 13, rotation + 90 + m * 90f,
                                    3.6f * warmup * (1 - Mathf.sin(Time.time + Mathf.PI * h * 3, 10 + m * 0.1f, 0.9f)),
                                    1.2f * warmup * (1 - Mathf.sin(Time.time, 10, 0.2f)));
                        }
                    }
                }
            }
            Draw.blend();
            Draw.color();
            Draw.rect(topRegion, x, y);
            Draw.z(Layer.effect);
            for (int i = 0; i < 2; i++) {
                sideTrails[i].draw(WHPal.SkyBlue, 1.2f * warmup);
                sideTrails[i].drawCap(WHPal.SkyBlue, 1.2f * warmup);
            }
            Draw.color(team.color);
            for (ShieldData shield : shieldData) {
                if (shield.building != null && !shield.broken) {
                    for (int i = 0; i < 4; i++) {
                        float f = (Time.time - 25 * i) % 100 / 100;
                        float angle = Angles.angle(shield.building.x, shield.building.y, x, y);
                        Tmp.v1.trns(angle, f * tilesize * size * 4);
                        stroke(warmup * 2f * (1 - f));
                        Lines.square(
                                shield.building.x + Tmp.v1.x,
                                shield.building.y + Tmp.v1.y,
                                (1 - f) * 8,
                                45
                        );
                    }
                }
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();

            if (shieldData.any()) {
                Table linkBuild = new Table();
                linkBuild.background(Tex.pane);
                linkBuild.defaults().growX().left().pad(10).size(30).width(250).scaling(Scaling.fit);

                for (ShieldData shield : shieldData) {
                    linkBuild.table(b -> {
                        b.background(Styles.black6);
                        b.add(new Bar(
                                () -> shield.building.block.localizedName + " " + Mathf.round(shield.broken ? 0f : (1f - shield.buildup / MaxHealth(shield)) * 100) + "%",
                                () -> Pal.accent,
                                () -> shield.broken ? 0f : 1f - shield.buildup / MaxHealth(shield)
                        )).growX().height(15f).width(225);
                    }).growX().row();
                }
                table.add(linkBuild).growX();
            } else {
                table.add("@none").color(Color.lightGray);
            }
            table.pack();
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

            stroke(3f, Pal.gray);
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

                color(Pal.gray);
                stroke(3);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);
                if ((verticalX || verticalY) && !onSameXAxis && !onSameYAxis) {
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                } else
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                Lines.linePoint(toX, toY);
                Lines.endLine();
                stroke(3);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);

                Draw.reset();
                color(Pal.gray);
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

                color(this.team.color);
                stroke(1);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);

                if ((verticalX || verticalY) && !onSameXAxis && !onSameYAxis) {
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                } else
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);

                Lines.linePoint(toX, toY);
                Lines.endLine();
                stroke(1);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);
                Draw.reset();
                color(this.team.color);
                float ang;
                if (verticalX || verticalY) {
                    ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, toX, toY);
                } else ang = Angles.angle(Tmp.v1.x, Tmp.v1.y, toX, toY);
                Draw.rect(atlas.find("logic-node"), toX, toY, 12 / 2f, 12 / 2f, ang);
            }

            color(this.team.color);
            Lines.square(x, y, offset);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(warmup);

            write.i(shieldData.size);
            for (ShieldData shield : shieldData) {
                write.i(shield.building.pos());
                write.f(shield.buildup);
                write.bool(shield.broken);
                write.f(shield.hit);
            }
            write.i(targets.size);
            for (int i = 0; i < targets.size; i++) {
                write.i(targets.items[i]);
            }

        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            warmup = read.f();
            shieldData.clear();

            int shieldCount = read.i();
            for (int i = 0; i < shieldCount; i++) {
                ShieldData shield = new ShieldData();
                shield.building = world.build(read.i());
                shield.buildup = read.f();
                shield.broken = read.bool();
                shield.hit = read.f();
                if (shield.building != null) {
                    shieldData.add(shield);
                }
            }

            targets.clear();
            int targetCount = read.i();
            for (int i = 0; i < targetCount; i++) {
                targets.add(read.i());
            }


        }
    }
    public static class ShieldData {
        public Building building;
        public float buildup;
        public boolean broken;
        public float hit;
        public float shieldSize;
    }
}
