package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.*;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import wh.graphics.PositionLightning;

import static mindustry.Vars.state;

public class LightningChainBulletType extends BasicBulletType {
    public Color chainColor = Pal.lancerLaser;

    public float lengthFalloff = 0.5f;
    public float oscScl = 2f, oscMag = 0.15f;
    public int fragBullets = 2;
    public int pierceDamage = 100;
    public float fragAngle = 0f;
    public float fragOffsetMin = 0f, fragOffsetMax = 0f;
    public Color[]  colors = {Pal.sapBullet.cpy().mul(1f, 1f, 1f, 0.4f), Pal.sapBullet, Color.white};


    public LightningChainBulletType(float lifetime, float damage) {
        super(lifetime, damage);
        speed = 0f;
        hitEffect=Fx.hitLancer;
        hitColor = Pal.lancerLaser;
        despawnEffect = Fx.none;
        collides = collidesTiles = collidesAir = collidesGround = false;
        keepVelocity = false;
        absorbable = false;
        hittable = false;
    }

    public LightningChainBulletType() {
        super();
    }

    @Override
    public void init(Bullet b) {
        if (fragBullet != null && !b.absorbed && !(b.frags >= pierceFragCap && pierceFragCap > 0)) {
            createFrags(b, b.x, b.y);
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y) {
        if (fragBullet != null && (fragOnAbsorb || !b.absorbed) && !(b.frags >= pierceFragCap && pierceFragCap > 0)) {
            Seq<Bullet> frags = new Seq<>(fragBullets);

            for (int i = 0; i < fragBullets; i++) {
                float angle = (360f / fragBullets) * i + fragAngle;
                float len = Mathf.random(fragOffsetMin, fragOffsetMax);
                float nx = x + Angles.trnsx(angle, len);
                float ny = y + Angles.trnsy(angle, len);
                Bullet frag = fragBullet.create(b, nx, ny, angle,
                        Mathf.random(fragVelocityMin, fragVelocityMax),
                        Mathf.random(fragLifeMin, fragLifeMax));
                if (frag != null) {
                    frags.add(frag);
                }
            }
            b.data = frags;
            b.frags++;
        }
    }

    @Override
    public void update(Bullet b) {
        if (b.timer.get(2, 10) && !state.isPaused()) {
            if (b.data instanceof Seq<?> frags && frags.size > 1) {
                frags.removeAll(f -> f == null || !((Bullet)f).isAdded());

                for (int i = 0; i < frags.size; i++) {
                    Bullet current = (Bullet) frags.get(i);
                    Bullet next = (Bullet) frags.get((i + 1) % frags.size);

                    if (current != null && current.isAdded() &&
                            next != null && next.isAdded()) {
                        float dst = current.dst(next);
                        float angle = Angles.angle(current.x, current.y, next.x, next.y);
                        BulletType PreiceBullet = new LaserBulletType() {{
                            this.damage = 1;
                            lifetime =10;
                            width=0f;
                            this.length=dst;
                            pierceBuilding=true;
                            pierce =true;
                            absorbable=false;
                        }};
                        PreiceBullet.create(current, b.team, current.x, current.y, angle, pierceDamage, 0, 1, hitColor);
                        PositionLightning.createEffect(current, next, chainColor, 1, Mathf.random(1, 2));
                    }
                }
            }
        }
    }

    @Override
    public void draw(Bullet b) {
        if (b.data instanceof Seq<?> frags && frags.size > 1) {
            frags.removeAll(f -> f == null || !((Bullet)f).isAdded());
            for (int i = 0; i < frags.size; i++) {
                Bullet current = (Bullet) frags.get(i);
                Bullet next = (Bullet) frags.get((i + 1) % frags.size);
                float compound = 1f;
                float cwidth = 8;
                for (Color color : colors) {
                    float fadeIn = Mathf.curve(b.fin(), 0f, 0.3f),
                            fadeOut = Mathf.curve(b.fin(Interp.pow2Out), 0.9f, 1f);
                    float fade = fadeIn * (1f - fadeOut);
                    float sin = 1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag);
                    float w = (cwidth *= lengthFalloff) * fade * sin;

                    Draw.color(color);
                    Lines.stroke(w);
                    Lines.line(current.x,current.y,next.x,next.y);
                    Draw.reset();

                    compound *= lengthFalloff;
                }
            }
        }
    }


    @Override
    public void hit(Bullet b, float x, float y) {
        createSplashDamage(b, x, y);
    }

    @Override
    public void despawned(Bullet b) {
        if (b.data instanceof Seq<?>) {
            ((Seq<?>) b.data).clear();
        }
    }
}