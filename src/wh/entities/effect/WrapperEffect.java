package wh.entities.effect;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;

@SuppressWarnings("unused")
public class WrapperEffect extends Effect {
    public Effect effect = Fx.none;
    public Color color = Color.white.cpy();
    public float rot = -1;
    public boolean rotModifier = false;

    public WrapperEffect() {}

    public WrapperEffect(Effect effect, Color color) {
        this.effect = effect;
        this.color = color;
    }

    public WrapperEffect(Effect effect, Color color, float rot) {
        this.effect = effect;
        this.color = color;
        this.rot = rot;
    }

    public static WrapperEffect wrap(Effect effect, Color color) {
        return new WrapperEffect(effect, color);
    }

    public static WrapperEffect wrap(Effect effect, Color color, float rot) {
        return new WrapperEffect(effect, color, rot);
    }

    public static WrapperEffect wrap(Effect effect, float rot, boolean rotModifier) {
        return new WrapperEffect(effect, Color.white, rot).setRotModifier(rotModifier);
    }

    public WrapperEffect setRotModifier(boolean rotModifier) {
        this.rotModifier = rotModifier;
        return this;
    }

    @Override
    public void init() {
        effect.init();
        clip = effect.clip;
        lifetime = effect.lifetime;
    }

    @Override
    public void render(EffectContainer e) {}

    @Override
    public void create(float x, float y, float rotation, Color color, Object data) {
        effect.create(x, y, rot > 0 ? rotModifier ? rot + rotation : rot : rotation, this.color, data);
    }
}
