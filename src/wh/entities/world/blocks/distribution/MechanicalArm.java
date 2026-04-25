package wh.entities.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class MechanicalArm extends Block {
    public static final int commandClear = -1;
    public static final int commandClearSource = -2;
    public static final int commandClearTarget = -3;
    public static final int targetOffset = 10;

    public float range = 120f;
    public float armSpeed = 5f;
    public int transferAmount = 5;
    public float armRotateSpeed = 2;
    public float forearmRotateSpeed = 1.5f;
    public float frontRotateSpeed = 0.5f;
    public float armLengthLerp = 0.04f;
    public float armThickness = 2.5f;
    public float carriedItemSize = 5.2f;
    public boolean sourceMustBeUnloadable = false;

    public Color armColor = Color.valueOf("989AA3FF");


    public TextureRegion topRegion;
    public TextureRegion jointRegion;
    public TextureRegion frontRegion;

    public Effect unloadEffect = Fx.conveyorPoof;

    public MechanicalArm(String name) {
        super(name);

        update = true;
        solid = true;
        configurable = true;
        saveConfig = false;
        hasItems = false;
        noUpdateDisabled = true;
        group = BlockGroup.transportation;
        envEnabled |= Env.space;
        clearOnDoubleTap = true;
        sync = true;

        config(Integer.class, (MechanicalArmBuild build, Integer value) -> build.applyLinkConfig(value));
        config(Building.class, (MechanicalArmBuild build, Building other) -> build.applyLinkFromBuilding(other));

        config(Item.class, (MechanicalArmBuild build, Item item) -> {
            build.filter = item;
            build.payloadFilter = null;
        });
        config(UnitType.class, (MechanicalArmBuild build, UnitType unit) -> {
            build.payloadFilter = canPayloadUnit(unit) ? unit : null;
            build.filter = null;
        });
        configClear((MechanicalArmBuild build) -> {
            build.filter = null;
            build.payloadFilter = null;
        });
    }

    @Override
    public void init() {
        super.init();
        updateClipRadius(range + tilesize * 2f);
    }

    @Override
    public void load() {
        super.load();
        jointRegion = Core.atlas.find(name + "-joint");
        frontRegion = Core.atlas.find(name + "-front");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        stats.add(Stat.speed, armSpeed, StatUnit.tilesSecond);
    }

    public boolean canPayloadUnit(UnitType t) {
        return t != null && !t.isHidden() && !t.isBanned() && t.supportsEnv(state.rules.env);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize, y * tilesize, range, Pal.accent);
    }


    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        out.addAll(frontRegion, jointRegion);
    }

    public class MechanicalArmBuild extends Building {
        public int source = -1;
        public int target = -1;
        public Item filter;
        public UnitType payloadFilter;
        public Item carrying;
        public int carryingAmount;
        public Payload carryingPayload;
        public int itemOffset;

        public float tipX, tipY;
        public float elbowX, elbowY;
        public float shoulderRot = 90f;
        public float forearmRotLerp = 90f;
        public float frontOffsetLerp = 90f;
        public float forearmRot = 90f;
        public float len1 = tilesize * 0.7f, len2 = tilesize * 0.7f;
        public boolean tipInitialized;

        public void applyLinkConfig(int value) {
            if (value == commandClear) {
                source = -1;
                target = -1;
                return;
            }
            if (value == commandClearSource) {
                source = -1;
                return;
            }
            if (value == commandClearTarget) {
                target = -1;
                return;
            }
            if (value >= 0) {
                source = value;
                if (target == value) {
                    target = -1;
                }
            } else if (value <= -targetOffset) {
                int decodedTarget = -value - targetOffset;
                target = decodedTarget;
                if (source == decodedTarget) {
                    source = -1;
                }
            }
        }

        public void applyLinkFromBuilding(Building other) {
            if (other == null) return;
            if (other == this || !baseValid(other)) return;

            int linkedPos = other.pos();

            // Logic-friendly behavior: repeated command keeps links stable instead of toggling.
            if (source == -1) {
                applyLinkConfig(linkedPos);
                return;
            }

            if (source == linkedPos || target == linkedPos) {
                return;
            }

            applyLinkConfig(encodeTarget(linkedPos));
        }

        @Override
        public void updateTile() {
            if (!tipInitialized) {
                tipX = x;
                tipY = y;
                elbowX = x;
                elbowY = y;
                tipInitialized = true;
            }

            Building from = world.build(source);
            Building to = world.build(target);

            if (!sourceValid(from)) {
                source = -1;
                from = null;
            } else {
                source = from.pos();
            }

            if (!targetValid(to)) {
                target = -1;
                to = null;
            } else {
                target = to.pos();
            }

            if (carryingAmount <= 0) {
                carrying = null;
                carryingAmount = 0;
            }

            if (carryingPayload != null && to != null) {
                moveArmToward(to.x, to.y, to);
                if (atTipTarget(to)) {
                    Building acceptSource = from != null ? from : this;
                    if (to.acceptPayload(acceptSource, carryingPayload)) {
                        to.handlePayload(acceptSource, carryingPayload);
                        carryingPayload = null;
                    }
                }
                return;
            }

            if (carryingPayload == null && carryingAmount == 0 && payloadFilter != null && from != null && to != null) {
                moveArmToward(from.x, from.y, from);
                if (atTipTarget(from)) {
                    Payload sourcePayload = from.getPayload();
                    if (sourcePayload instanceof UnitPayload up && up.unit != null && up.unit.type == payloadFilter) {
                        if (to.acceptPayload(from, sourcePayload)) {
                            Payload taken = from.takePayload();
                            if (taken instanceof UnitPayload tup && tup.unit != null && tup.unit.type == payloadFilter) {
                                carryingPayload = taken;
                                from.noSleep();
                            } else if (taken != null) {
                                if (from.acceptPayload(this, taken)) {
                                    from.handlePayload(this, taken);
                                } else {
                                    taken.dump();
                                }
                            }
                        }
                    }
                }
                return;
            }

            if (carrying != null && to != null) {
                moveArmToward(to.x, to.y, to);
                if (atTipTarget(to)) {
                    int moved = 0;
                    Building acceptSource = from != null ? from : this;

                    while (moved < carryingAmount) {
                        int remain = carryingAmount - moved;
                        int stackMoved = to.acceptStack(carrying, remain, acceptSource);
                        if (stackMoved > 0) {
                            to.handleStack(carrying, stackMoved, acceptSource);
                            moved += stackMoved;
                            unloadEffect.at(tipX, tipY);
                            continue;
                        }
                        if (to.acceptItem(acceptSource, carrying)) {
                            to.handleItem(acceptSource, carrying);
                            moved++;
                            continue;
                        }
                        break;
                    }
                    if (moved > 0) {
                        carryingAmount -= moved;
                    }
                    if (carryingAmount <= 0) {
                        carrying = null;
                        carryingAmount = 0;
                    }
                }
                return;
            }

            if (carryingPayload == null && carryingAmount == 0 && filter != null && from != null && to != null) {
                moveArmToward(from.x, from.y, from);
                if (atTipTarget(from)) {
                    Item item = pickExtractable(from);
                    if (item != null) {
                        int amount = Math.min(transferAmount, from.items.get(item));
                        int stackCap = to.acceptStack(item, amount, from);
                        if (stackCap > 0) {
                            amount = Math.min(amount, stackCap);
                        } else if (to.acceptItem(from, item)) {
                            amount = Math.min(amount, 1);
                        } else {
                            amount = 0;
                        }
                        if (amount > 0) {
                            from.items.remove(item, amount);
                            for (int i = 0; i < amount; i++) {
                                from.itemTaken(item);
                            }
                            unloadEffect.at(tipX, tipY);
                            carrying = item;
                            carryingAmount = amount;
                        }
                    }
                }
                return;
            }

            moveArmToward(x, y, null);
        }

        public Item pickExtractable(Building from) {
            if (from.items == null || filter == null || !from.items.has(filter)) return null;
            return filter;
        }

        public float frontOffsetTarget(Building focus) {
            if (focus == null) return forearmRotLerp;

            float tangentX = Angles.trnsx(forearmRotLerp, 1f);
            float tangentY = Angles.trnsy(forearmRotLerp, 1f);
            float toFocusX = focus.x - tipX;
            float toFocusY = focus.y - tipY;
            float cross = tangentX * toFocusY - tangentY * toFocusX;

            float deadZone = 0.35f * tilesize;
            if (cross > deadZone) return 90f;
            if (cross < -deadZone) return -90f;
            return forearmRotLerp;
        }

        public void moveArmToward(float tx, float ty, Building focus) {
            Tmp.v1.set(tx, ty).sub(x, y).limit(range);
            tx = x + Tmp.v1.x;
            ty = y + Tmp.v1.y;

            float dist = Mathf.dst(x, y, tx, ty);
            float maxLen = range * 0.7f;
            float minLen = tilesize * 0.45f;

            float totalLen = Mathf.clamp(dist, minLen * 2f, maxLen * 2f);
            float targetLen1 = Mathf.clamp(totalLen * 0.55f, minLen, maxLen);
            float targetLen2 = Mathf.clamp(totalLen - targetLen1, minLen, maxLen);

            len1 = Mathf.lerpDelta(len1, targetLen1, armLengthLerp);
            len2 = Mathf.lerpDelta(len2, targetLen2, armLengthLerp);

            float targetShoulder = Angles.angle(x, y, tx, ty);
            shoulderRot = Angles.moveToward(shoulderRot, targetShoulder, armRotateSpeed * edelta());

            elbowX = x + Angles.trnsx(shoulderRot, len1);
            elbowY = y + Angles.trnsy(shoulderRot, len1);

            // second segment points to target in real time and rotates faster for a "follow-through" feel
            float targetForearm = Angles.angle(elbowX, elbowY, tx, ty);
            forearmRot = Angles.moveToward(forearmRot, targetForearm, forearmRotateSpeed * edelta());

            float maxStep = armSpeed * edelta() * tilesize;
            float targetTipX = elbowX + Angles.trnsx(forearmRot, len2);
            float targetTipY = elbowY + Angles.trnsy(forearmRot, len2);
            float previousTipX = tipX;
            float previousTipY = tipY;
            Tmp.v2.set(targetTipX, targetTipY).sub(tipX, tipY);
            float stepLen = Tmp.v2.len();
            if (stepLen <= maxStep || stepLen <= 0.001f) {
                tipX = targetTipX;
                tipY = targetTipY;
            } else {
                Tmp.v2.scl(maxStep / stepLen);
                tipX += Tmp.v2.x;
                tipY += Tmp.v2.y;
            }

            // Front claw follows the tangent of actual tip motion for smoother visual direction.
            float moved = Mathf.dst(previousTipX, previousTipY, tipX, tipY);
            if (moved > 0.001f) {
                float tangentRot = Angles.angle(previousTipX, previousTipY, tipX, tipY);
                forearmRotLerp = Angles.moveToward(forearmRotLerp, tangentRot, forearmRotateSpeed * edelta());
            } else {
                forearmRotLerp = Angles.moveToward(forearmRotLerp, forearmRot, forearmRotateSpeed * edelta() * 0.6f);
            }

            float targetOffset = frontOffsetTarget(focus);
            float frontStep = Math.max(frontRotateSpeed, 0.01f) * edelta() * 8f;
            frontOffsetLerp = Angles.moveToward(frontOffsetLerp, targetOffset, frontStep);
        }

        public boolean atTipTarget(Building target) {
            if (target == null) return false;
            float reach = target.block.size * tilesize * 0.5f + 2f;
            return Mathf.within(tipX, tipY, target.x, target.y, reach);
        }

        public boolean baseValid(Building other) {
            return other != null
                    && other != this
                    && other.isValid()
                    && other.team == team
                    && within(other, range);
        }

        public boolean sourceValid(Building other) {
            return baseValid(other) && (!sourceMustBeUnloadable || other.canUnload());
        }

        public boolean targetValid(Building other) {
            return baseValid(other);
        }

        public Building activeFocus() {
            Building from = world.build(source);
            Building to = world.build(target);

            if (carryingPayload != null || carryingAmount > 0) {
                return targetValid(to) ? to : null;
            }

            if ((filter != null || payloadFilter != null) && sourceValid(from) && targetValid(to)) {
                return from;
            }

            return null;
        }

        public float logicProgress() {
            Building focus = activeFocus();
            if (focus == null) return 0f;

            float reach = focus.block.size * tilesize * 0.5f + 2f;
            float remain = Math.max(0f, Mathf.dst(tipX, tipY, focus.x, focus.y) - reach);
            return Mathf.clamp(1f - remain / range);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            super.control(type, p1, p2, p3, p4);

            if (type == LAccess.config) {
                applyLinkConfig(Mathf.round((float) p1));
            }
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4) {
            if (type == LAccess.config && p1 instanceof Building other) {
                applyLinkFromBuilding(other);
                return;
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor) {
            return switch (sensor) {
                case progress -> logicProgress();
                case totalItems -> carryingAmount;
                case payloadCount -> carryingPayload != null ? 1d : 0d;
                default -> super.sense(sensor);
            };
        }

        @Override
        public Object senseObject(LAccess sensor) {
            return switch (sensor) {
                case firstItem -> carrying;
                case config -> config();
                case payloadType ->
                        carryingPayload instanceof UnitPayload up && up.unit != null ? up.unit.type : super.senseObject(sensor);
                default -> super.senseObject(sensor);
            };
        }

        @Override
        public double sense(mindustry.ctype.Content content) {
            if (content instanceof Item item) {
                return carrying == item ? carryingAmount : 0d;
            }
            if (content instanceof UnitType unit) {
                return carryingPayload instanceof UnitPayload up && up.unit != null && up.unit.type == unit ? 1d : 0d;
            }
            return super.sense(content);
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
            Draw.z(Layer.blockOver - 0.001f);
            drawArm();

            if (carryingPayload != null) {
                Draw.z(Layer.blockOver + 0.2f);
                carryingPayload.set(tipX, tipY, carryingPayload.rotation());
                carryingPayload.draw();
            } else if (carrying != null && carryingAmount > 0) {
                TextureRegion icon = carrying.fullIcon != null ? carrying.fullIcon : carrying.uiIcon;
                Tmp.v4.trns(forearmRotLerp + frontOffsetLerp, icon.width / 8f);
                Draw.rect(icon, tipX + Tmp.v4.x, tipY + Tmp.v4.y, carriedItemSize, carriedItemSize);
                Draw.color();
            }
        }

        public void drawArm() {

            Draw.color(Pal.darkerGray);
            Lines.stroke(armThickness * 1.5f);
            Lines.line(x, y, elbowX, elbowY, false);
            Lines.line(elbowX, elbowY, tipX, tipY, false);

            Draw.color(armColor);
            Lines.stroke(armThickness);
            Lines.line(x, y, elbowX, elbowY, false);
            Lines.line(elbowX, elbowY, tipX, tipY, false);

            Draw.color();
            Draw.z(Layer.blockOver + 0.002f);
            Draw.rect(frontRegion, tipX, tipY, forearmRotLerp + frontOffsetLerp - 90f);
            Draw.rect(jointRegion, elbowX, elbowY, 0);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (filter != null) {
                drawItemSelection(filter);
            } else if (payloadFilter != null) {
                Draw.color();
                Draw.rect(payloadFilter.fullIcon, x, y + block.size * tilesize * 0.55f, 8f, 8f);
                Draw.color();
            }
        }

        @Override
        public void drawConfigure() {
            float pulse = Mathf.absin(Time.time, 6f, 1f);

            Drawf.select(x, y, block.size * tilesize / 2f + 2f, Pal.accent);
            Drawf.dashCircle(x, y, range, Pal.accent);

            Building from = world.build(source);
            Building to = world.build(target);

            if (sourceValid(from)) {
                Drawf.select(from.x, from.y, from.block.size * tilesize / 2f + 2f + pulse, Pal.place);
                Drawf.arrow(from.x, from.y, x, y, from.block.size * tilesize / 2f + 2f, 4f + pulse, Pal.place);
            }

            if (targetValid(to)) {
                Drawf.select(to.x, to.y, to.block.size * tilesize / 2f + 2f + pulse, Pal.accent);
                Drawf.arrow(x, y, to.x, to.y, block.size * tilesize / 2f + 2f, 4f + pulse, Pal.accent);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(itemTable -> {
                itemTable.add("Items").left().row();
                ItemSelection.buildTable(
                        MechanicalArm.this,
                        itemTable,
                        content.items(),
                        () -> filter,
                        this::configure,
                        selectionRows,
                        selectionColumns
                );
            }).growX().row();

            table.table(payloadTable -> {
                payloadTable.add("Payload Units").left().row();
                ItemSelection.buildTable(
                        MechanicalArm.this,
                        payloadTable,
                        content.units().select(MechanicalArm.this::canPayloadUnit),
                        () -> payloadFilter,
                        this::configure,
                        selectionRows,
                        selectionColumns
                );
            }).growX();
        }

        public int encodeTarget(int pos) {
            return -(pos + targetOffset);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other == this) {
                if (source == -1 && target == -1) {
                    deselect();
                }
                configure(commandClear);
                return false;
            }

            if (!baseValid(other)) {
                return true;
            }

            int pos = other.pos();
            if (source == pos) {
                configure(commandClearSource);
            } else if (target == pos) {
                configure(commandClearTarget);
            } else if (source == -1) {
                configure(pos);
            } else {
                configure(encodeTarget(pos));
            }
            return false;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public Object config() {
            return filter != null ? filter : payloadFilter;
        }

        @Override
        public byte version() {
            return 3;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(source);
            write.i(target);
            write.s(filter == null ? -1 : filter.id);
            write.s(carrying == null ? -1 : carrying.id);
            write.s(carryingAmount);
            write.f(tipX);
            write.f(tipY);
            write.f(elbowX);
            write.f(elbowY);
            write.f(shoulderRot);
            write.f(forearmRotLerp);
            write.f(forearmRot);
            write.f(len1);
            write.f(len2);
            write.s(itemOffset);
            write.bool(tipInitialized);
            write.s(payloadFilter == null ? -1 : payloadFilter.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            source = read.i();
            target = read.i();

            int filterId = read.s();
            filter = filterId < 0 ? null : content.item(filterId);

            int carryingId = read.s();
            carrying = carryingId < 0 ? null : content.item(carryingId);
            carryingPayload = null;

            if (revision >= 2) {
                carryingAmount = read.s();
                tipX = read.f();
                tipY = read.f();
                elbowX = read.f();
                elbowY = read.f();
                shoulderRot = read.f();
                forearmRotLerp = read.f();
                forearmRot = read.f();
                len1 = read.f();
                len2 = read.f();
                itemOffset = read.s();
                tipInitialized = read.bool();
                int payloadFilterId = revision >= 3 ? read.s() : -1;
                payloadFilter = payloadFilterId < 0 ? null : content.unit(payloadFilterId);
            }
        }
    }
}
