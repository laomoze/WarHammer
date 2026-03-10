package wh.entities.world.blocks.distribution;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class CompositePoweNode extends PowerNode{

    public Color laserColor1 = Color.white;
    public Color laserColor2 = Pal.powerLight;
    public float pulseScl = 7, pulseMag = 0.05f;
    public float laserWidth = 0.8f;
    public int crossLinkRange = 50;

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
         /*   if(player == null || team != player.team()) return;

            if(isPayload()) return;

            Draw.z(WHContent.POWER_AREA);
            Draw.color(team.color);
            Fill.square(x, y, laserRange*tilesize);

            Draw.z(WHContent.POWER_DYNAMIC);
            Draw.color(team.color);
            Fill.square(x, y, (laserRange * 0.8f + laserRange * 0.2f * Interp.exp5Out.apply(Time.time / 240f % 1f))*tilesize);*/

        }

        public void DrawBeamNode(){
            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);
            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for(int i = 0; i < 4; i++){
                if(dests[i] != null && links[i] != null){
                    if(links[i].block.hasPower && links[i].team == team){
                        //Avoid duplicate beams when both endpoints are visible.
                        //If the other end is not visible, still draw from this side.
                        if(links[i].wasVisible && id > links[i].id) continue;

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
            Building[] prevLinks = links.clone();
            Tile[] prevDests = dests.clone();
            Building[] candidates = new Building[4];
            Tile[] candidateDests = new Tile[4];

            //Scan potential 4-direction links.
            for(int i = 0; i < 4; i++){
                var dir = Geometry.d4[i];
                int offset = size / 2;
                for(int j = 1 + offset; j <= crossLinkRange + offset; j++){
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    if(other != null && other.isInsulated()){
                        break;
                    }

                    if(other != null && other.block.hasPower && other.block.connectedPower && other.team == team){
                        candidates[i] = other;
                        candidateDests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }
            }

            //Count non-directional links already present; those also consume maxNodes.
            IntSet prevDirectional = new IntSet(4);
            for(var prev : prevLinks){
                if(prev != null) prevDirectional.add(prev.pos());
            }

            int externalLinks = 0;
            for(int i = 0; i < power.links.size; i++){
                int p = power.links.get(i);
                if(!prevDirectional.contains(p)){
                    externalLinks++;
                }
            }

            int allowedDirectional = Math.max(0, maxNodes - externalLinks);
            int kept = 0;

            //Apply cap: total links (manual + directional) must not exceed maxNodes.
            for(int i = 0; i < 4; i++){
                if(candidates[i] != null && kept < allowedDirectional){
                    links[i] = candidates[i];
                    dests[i] = candidateDests[i];
                    kept++;
                }else{
                    links[i] = null;
                    dests[i] = null;
                }
            }

            //Diff previous and current, then sync graph links.
            for(int i = 0; i < 4; i++){
                var prev = prevLinks[i];
                var next = links[i];

                if(next != prev){
                    if(prev != null && prev.isAdded()){
                        prev.power.links.removeValue(pos());
                        power.links.removeValue(prev.pos());

                        PowerGraph newgraph = new PowerGraph();
                        newgraph.reflow(this);

                        if(prev.power.graph != newgraph){
                            PowerGraph og = new PowerGraph();
                            og.reflow(prev);
                        }
                    }

                    if(next != null){
                        power.links.addUnique(next.pos());
                        next.power.links.addUnique(pos());
                        power.graph.addGraph(next.power.graph);
                    }
                }else if(next == null){
                    //keep arrays clean if nothing is linked on this side
                    dests[i] = null;
                }else if(dests[i] == null){
                    //preserve previous destination tile when endpoint didn't change
                    dests[i] = prevDests[i];
                }
            }
        }
    }

}

