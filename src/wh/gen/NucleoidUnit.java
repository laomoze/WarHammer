package wh.gen;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.graphics.*;
import wh.type.unit.*;

import static arc.Core.*;

public class NucleoidUnit extends UnitEntity implements Nucleoidc {
    public float recentDamage = 0f;
    public float reinforcementsReload = 0f;

    protected NucleoidUnit() {}

    @Override
    public int classId() {
        return EntityRegister.getId(NucleoidUnit.class);
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);

        if (type instanceof NucleoidUnitType nType) {
            recentDamage = nType.maxDamagedPerSec;
            reinforcementsReload = nType.reinforcementsSpacing;
        }
    }

    @Override
    public float mass() {
        if (type instanceof NucleoidUnitType nType) {
            return nType.mass;
        }
        return 8000000f;
    }

    @Override
    public void update() {
        super.update();

        if (type instanceof NucleoidUnitType nType) {
            recentDamage += nType.recentDamageResume * Time.delta;
            if (recentDamage >= nType.maxDamagedPerSec) {
                recentDamage = nType.maxDamagedPerSec;
            }

            reinforcementsReload += Time.delta;
            if (healthf() < 0.3f && reinforcementsReload >= nType.reinforcementsSpacing) {
                reinforcementsReload = 0;
                for (int i : Mathf.signs) {
                    Tmp.v1.trns(rotation + 60 * i, -hitSize * 1.85f).add(x, y);
                }
            }
        }
    }

    @Override
    public void draw() {
        super.draw();

        if (type instanceof NucleoidUnitType nType) {
            float z = Draw.z();
            Draw.z(Layer.bullet);

            Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4f, 0.15f));
            Draw.color(Tmp.c1);
            Lines.stroke(3f);
            Drawn.circlePercent(x, y, hitSize * 1.15f, reinforcementsReload / nType.reinforcementsSpacing, 0);

            float scl = Interp.pow3Out.apply(Mathf.curve(reinforcementsReload / nType.reinforcementsSpacing, 0.96f, 1f));
            TextureRegion arrowRegion = atlas.find("wh-jump-gate-arrow");

            for (int l : Mathf.signs) {
                float angle = 90 + 90 * l;
                for (int i = 0; i < 4; i++) {
                    Tmp.v1.trns(angle, i * 50 + hitSize * 1.32f);
                    float f = (100 - (Time.time + 25 * i) % 100) / 100;

                    Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * f * scl, arrowRegion.height * f * scl, angle + 90);
                }
            }

            Draw.z(z);
        }
    }

    @Override
    public void rawDamage(float amount) {
        if (type instanceof NucleoidUnitType nType) {
            float a = amount * nType.damageMultiplier;

            boolean hadShields = shield > 1e-4f;
            if (hadShields) {
                shieldAlpha = 1f;
            }

            a = Math.min(a, nType.maxOnceDamage);

            float shieldDamage = Math.min(Math.max(shield, 0f), a);
            shield -= shieldDamage;
            hitTime = 1f;

            a -= shieldDamage;
            a = Math.min(recentDamage / healthMultiplier, a);
            recentDamage -= a * 1.5f * healthMultiplier;

            if (a > 0f && type.killable) {
                health -= a;
                if (health <= 0f && !dead) {
                    kill();
                }

                if (hadShields && shield <= 1e-4f) {
                    Fx.unitShieldBreak.at(x, y, 0f, team.color, this);
                }
            }
        }
    }

    @Override
    public void read(Reads read) {
        reinforcementsReload = read.f();

        super.read(read);
    }

    @Override
    public void write(Writes write) {
        write.f(reinforcementsReload);

        super.write(write);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);

        if (!isLocal()) {
            reinforcementsReload = read.f();
        } else {
            read.f();
        }
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);

        write.f(reinforcementsReload);
    }

    @Override
    public float recentDamage() {
        return recentDamage;
    }

    @Override
    public float reinforcementsReload() {
        return reinforcementsReload;
    }

    @Override
    public void recentDamage(float value) {
        recentDamage = value;
    }

    @Override
    public void reinforcementsReload(float value) {
        reinforcementsReload = value;
    }

    public static NucleoidUnit create() {
        return new NucleoidUnit();
    }
}
