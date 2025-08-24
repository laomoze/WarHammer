package wh.entities.world.blocks.defense;

import arc.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.gen.*;
import wh.graphics.*;
import wh.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static wh.ui.UIUtils.*;

public abstract class CommandableAttackerBlock extends CommandableBlock{
    public float spread = 120f;
    public float prepareDelay = 60f;
    public int storage = 1;
    public ShootPattern shoot = new ShootPattern();

    protected BulletType bullet = Bullets.placeholder;

    public CommandableAttackerBlock(String name){
        super(name);

        replaceable = true;
        canOverdrive = false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.damage, StatValues.ammo(ObjectMap.of(this, bullet)));
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("progress", (CommandableAttackerBlockBuild tile) -> new Bar(
        () -> bundle.get("bar.progress"),
        () -> Pal.power,
        () -> (tile.reload % reloadTime) / reloadTime
        ));
        addBar("storage", (CommandableAttackerBlockBuild tile) -> new Bar(
        () -> bundle.format("bar.capacity", UI.formatAmount(tile.ammo())),
        () -> Pal.ammo,
        () -> (float)tile.ammo() / storage
        ));
    }

    public abstract class CommandableAttackerBlockBuild extends CommandableBlockBuild{
        @Override
        public boolean isCharging(){
            return efficiency > 0 && reload < reloadTime * storage && !initiateConfigure;
        }

        @Override
        public boolean shouldCharge(){
            return reload < reloadTime * storage;
        }

        public int ammo(){
            return (int)(reload / reloadTime);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public BlockStatus status(){
            return canCommand(targetVec) ? BlockStatus.active : isCharging() ? BlockStatus.noOutput : BlockStatus.noInput;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(shouldChargeConfigure()){
                configureChargeProgress += edelta() * warmup;
                if(configureChargeComplete()){
                    shoot(lastConfirmedTarget);
                }
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            target = read.i();
            reload = read.f();
            initiateConfigure = read.bool();
            configureChargeProgress = read.f();

            TypeIO.readVec2(read, lastConfirmedTarget);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(target);
            write.f(reload);
            write.bool(initiateConfigure);
            write.f(configureChargeProgress);

            TypeIO.writeVec2(write, lastConfirmedTarget);
        }

        @Override
        public void command(Vec2 pos){
            lastConfirmedTarget.set(pos);
            targetVec.set(pos);
            target = Point2.pack(World.toTile(pos.x), World.toTile(pos.y));
            initiateConfigure = true;

            WHFx.attackWarningPos.at(lastConfirmedTarget.x, lastConfirmedTarget.y, configureChargeTime, team.color, tile);
        }

        /**
         * Should Be Overridden.
         */
        public void shoot(Vec2 target){
            configureChargeProgress = 0;
            initiateConfigure = false;
            reload = Math.max(0, reload - reloadTime);

            consume();
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();

            Drawf.dashCircle(x, y, range, team.color);

            Seq<CommandableBlockBuild> builds = new Seq<>();
            for(CommandableBlockBuild build : WorldRegister.commandableBuilds){
                if(build != this && build != null && build.team == team && sameGroup(build.block) && build.canCommand(targetVec)){
                    builds.add(build);
                    Drawn.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, targetVec.x, targetVec.y);
                }
            }

            for(CommandableBlockBuild build : builds){
                Drawn.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, targetVec.x, targetVec.y);
            }

            if(builds.any()){
                Drawn.posSquareLink(Pal.accent, 1, 2, true, x, y, targetVec.x, targetVec.y);
                Drawn.drawConnected(targetVec.x, targetVec.y, 10f, Pal.accent);
            }

            if(canCommand(targetVec)) builds.add(this);
            if(builds.any())
                Drawn.overlayText(Core.bundle.format("wh-participants", builds.size), targetVec.x, targetVec.y, tilesize * 2f, Pal.accent, true);
        }

        public void lastAccessed(String lastAccessed){
            this.lastAccessed = lastAccessed;
        }

        public void commandAll(Vec2 pos){
            participantsTmp.clear();

            for(CommandableBlockBuild build : WorldRegister.commandableBuilds){
                if(build.team == team && sameGroup(build.block) && build.canCommand(pos)){
                    build.command(pos);
                    participantsTmp.add(build);
                    lastAccessed(Iconc.modeAttack + "");
                    Log.info("Commanded " + build.block.name + " " + build.x + ", " + build.y);
                }
            }

            if(!headless && participantsTmp.any()){
                if(team != player.team())
                    UIUtils.showToast(Icon.warning, "[#ff7b69]Caution: []Attack " + (int)(pos.x / 8) + ", " + (int)(pos.y / 8), WHSounds.alert2);
                WHFx.attackWarningRange.at(pos.x, pos.y, 80, team.color);
            }
        }

        @Override
        public boolean canCommand(Vec2 target){
            return ammo() > 0 && warmup > 0.25f && within(target, range()) && !isChargingConfigure();
        }

        @Override
        public void buildConfiguration(Table table){
            control.input.selectedBlock();

            table.table(Tex.paneSolid, t -> {
                t.button(Icon.modeAttack, Styles.cleari, () -> configure(targetVec)).size(LEN).disabled(b -> targetVec.epsilonEquals(x, y, 0.1f));
                t.button(bundle.get("wh-select-target"), Icon.move, Styles.cleart, LEN, () -> UIUtils.selectPos(t, this::configure)).size(LEN * 4, LEN).row();
            }).fill();
        }
    }

}
