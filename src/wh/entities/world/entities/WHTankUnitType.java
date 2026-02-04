package wh.entities.world.entities;

import mindustry.world.meta.*;

public class WHTankUnitType extends WHUnitType{

    public WHTankUnitType(String name){
        super(name);
        squareShape = true;
        omniMovement = false;
        rotateMoveFirst = true;
        rotateSpeed = 1.3f;
        envDisabled = Env.none;
        speed = 0.8f;
    }
}
