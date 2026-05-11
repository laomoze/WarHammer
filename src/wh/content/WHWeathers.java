package wh.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Interval;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.WeatherState;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.type.weather.RainWeather;
import mindustry.world.meta.Attribute;
import wh.graphics.Drawn;
import wh.graphics.WHPal;

public final class WHWeathers {
    public static Weather
            radiationSandstorm,
            acidRain;

    private static final float buildingEfficiencyMultiplier = 0.8f;

    private WHWeathers() {
    }

    public static void load() {
        radiationSandstorm = new ParticleWeather("radiation-sandstorm") {
            @Override
            public void update(WeatherState state) {
                super.update(state);
                Interval t = new Interval();
                if (t.get(60)) {
                    Groups.build.each(build -> {
                        if (build.block.canOverdrive) {
                            build.applySlowdown(buildingEfficiencyMultiplier, 61f);
                        }
                    });
                }
            }

            {
                color = noiseColor = Color.valueOf("c3d67a");
                particleRegion = "particle";
                drawNoise = true;
                useWindVector = true;
                sizeMax = 140f;
                sizeMin = 70f;
                minAlpha = 0f;
                maxAlpha = 0.22f;
                density = 1500f;
                baseSpeed = 5.6f;
                attrs.set(Attribute.light, -0.2f);
                attrs.set(Attribute.water, -0.15f);
                status = WHStatusEffects.rust;
                statusAir = true;
                statusGround = true;
                opacityMultiplier = 0.4f;
                force = 0.12f;
                sound = Sounds.wind;
                soundVol = 0.85f;
                duration = 4f * Time.toMinutes;
            }
        };

        acidRain = new RainWeather("acid-rain") {
            private float lightningTimer = 0f;

            @Override
            public void update(WeatherState state) {
                super.update(state);
                if (Vars.headless || Vars.world == null || Vars.world.width() <= 0 || Vars.world.height() <= 0) return;

                lightningTimer -= Time.delta;
                if (lightningTimer > 0f) return;

                lightningTimer = Mathf.random(14f, 32f) / Mathf.clamp(state.intensity, 0.35f, 1.2f);
                float x = Mathf.random(0f, Vars.world.unitWidth());
                float y = Mathf.random(0f, Vars.world.unitHeight());
                float range = Mathf.random(55f, 150f);
                float piece = Mathf.random(7f, 13f);
                Drawn.randFadeLightningEffect(x, y, range, piece, WHPal.SkyBlue, Mathf.chance(0.5));
            }

            {
                color = Color.valueOf("8fbf7dff");
                yspeed = 6.8f;
                xspeed = 1.6f;
                density = 1150f;
                stroke = 0.85f;
                sizeMin = 10f;
                sizeMax = 46f;
                attrs.set(Attribute.light, -0.12f);
                attrs.set(Attribute.water, 0.2f);
                status = WHStatusEffects.acidRain;
                statusAir = true;
                statusGround = true;
                sound = Sounds.rain;
                soundVol = 0.35f;
                duration = 4f * Time.toMinutes;
            }
        };
    }
}
