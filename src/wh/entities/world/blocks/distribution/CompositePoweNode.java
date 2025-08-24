package wh.entities.world.blocks.distribution;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.BeamNode.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import mindustry.world.modules.*;
import wh.content.*;
import wh.graphics.*;

import java.util.*;

import static mindustry.Vars.*;

public class CompositePoweNode extends PowerNode{

    public Color laserColor1 = Color.white;
    public Color laserColor2 = Pal.powerLight;
    public float pulseScl = 7, pulseMag = 0.05f;
    public float laserWidth = 0.4f;
    public int crossLinkRange = 30;

    public CompositePoweNode(String name){
        super(name);
        configurable = true;
        consumesPower = false;
        outputsPower = false;
        canOverdrive = false;
        swapDiagonalPlacement = true;
        schematicPriority = -10;
        drawDisabled = false;
        envEnabled |= Env.space;
        destructible = true;

        update = true;
        allowDiagonal = false;
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= crossLinkRange + size - 1);
    }

    public void drawBeamNodePlace(int x, int y, int rotation, boolean valid){
        for(int i = 0; i < 4; i++){
            int maxLen = (crossLinkRange + size / 2);
            Building dest = null;
            var dir = Geometry.d4[i];
            int dx = dir.x, dy = dir.y;
            int offset = size / 2;
            for(int j = 1 + offset; j <= crossLinkRange + offset; j++){
                var other = world.build(x + j * dir.x, y + j * dir.y);

                //hit insulated wall
                if(other != null && other.isInsulated()){
                    break;
                }

                if(other != null && other.block.hasPower && other.team == Vars.player.team()){
                    maxLen = j;
                    dest = other;
                    break;
                }
            }

            Drawf.dashLine(Pal.placing,
            x * tilesize + dx * (tilesize * size / 2f + 2),
            y * tilesize + dy * (tilesize * size / 2f + 2),
            x * tilesize + dx * (maxLen) * tilesize,
            y * tilesize + dy * (maxLen) * tilesize
            );

            if(dest != null){
                Drawf.square(dest.x, dest.y, dest.block.size * tilesize / 2f + 2.5f, 0f);
            }
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        drawBeamNodePlace(x, y, rotation, valid);
    }

    public class CompositePoweNodeBuild extends PowerNodeBuild{

        //current links in cardinal directions
        public Building[] links = new Building[4];
        public Tile[] dests = new Tile[4];
        public int lastChange = -2;


        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }

        @Override
        public void draw(){
            super.draw();
            DrawBeamNode();
            if(player == null || team != player.team()) return;

            if(isPayload()) return;

            Draw.z(WHContent.POWER_AREA);
            Draw.color(team.color);
            Fill.square(x, y, laserRange*tilesize);

            Draw.z(WHContent.POWER_DYNAMIC);
            Draw.color(team.color);
            Fill.square(x, y, (laserRange * 0.8f + laserRange * 0.2f * Interp.exp5Out.apply(Time.time / 240f % 1f))*tilesize);

        }

        public void DrawBeamNode(){
            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);
            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for(int i = 0; i < 4; i++){
                if(dests[i] != null && links[i] != null && links[i].wasVisible){
                    if(links[i].block.hasPower && links[i].team == team){
                        int dst = Math.max(Math.abs(dests[i].x - tile.x), Math.abs(dests[i].y - tile.y));
                        //don't draw lasers for adjacent blocks
                        if(dst > 1 + size / 2){
                            var point = Geometry.d4[i];
                            float poff = tilesize / 2f;
                            Drawf.laser(laser, laserEnd, x + poff * size * point.x, y + poff * size * point.y, dests[i].worldx() - poff * point.x, dests[i].worldy() - poff * point.y, w);
                        }
                    }else{
                        links[i] = null;
                        dests[i] = null;
                    }
                }
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(linkValid(this, other)){
                configure(other.pos());
                return false;
            }

            if(this == other){ //double tapped
                if(other.power.links.size == 0){ //find links
                    Seq<Point2> points = new Seq<>();
                    getPotentialLinks(tile, team, link -> {
                        if(!insulated(this, link) && points.size < maxNodes){
                            points.add(new Point2(link.tileX() - tile.x, link.tileY() - tile.y));
                        }
                    });
                    configure(points.toArray(Point2.class));
                    Arrays.fill(links, null);
                    Arrays.fill(dests, null);
                    updateDirections();
                }else{ //clear links
                    configure(new Point2[0]);
                    Arrays.fill(links, null);
                    Arrays.fill(dests, null);
                }
                deselect();
                return false;
            }

            return true;
        }

        @Override
        public void updateTile(){
            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateDirections();
            }
        }

        public void updateDirections(){
            for(int i = 0; i < 4; i++){
                var prev = links[i];
                var dir = Geometry.d4[i];
                links[i] = null;
                dests[i] = null;
                int offset = size / 2;
                //find first block with power in range
                for(int j = 1 + offset; j <= crossLinkRange + offset; j++){
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    //hit insulated wall
                    if(other != null && other.isInsulated()){
                        break;
                    }

                    if(other != null && other.block.hasPower && other.block.connectedPower && other.team == team){
                        links[i] = other;
                        dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }

                var next = links[i];

                if(next != prev){
                    //unlinked, disconnect and reflow
                    if(prev != null && prev.isAdded()){
                        prev.power.links.removeValue(pos());
                        power.links.removeValue(prev.pos());

                        PowerGraph newgraph = new PowerGraph();
                        //reflow from this point, covering all tiles on this side
                        newgraph.reflow(this);

                        if(prev.power.graph != newgraph){
                            //reflow power for other end
                            PowerGraph og = new PowerGraph();
                            og.reflow(prev);
                        }
                    }

                    //linked to a new one, connect graphs
                    if(next != null){
                        power.links.addUnique(next.pos());
                        next.power.links.addUnique(pos());

                        power.graph.addGraph(next.power.graph);
                    }
                }
            }
        }
    }

}

