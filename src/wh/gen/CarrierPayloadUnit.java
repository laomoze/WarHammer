package wh.gen;

import wh.gen.CarrierUnit.*;

public class CarrierPayloadUnit extends CarrierRuntime{
    @Override
    public int classId(){
        return EntityRegister.getId(CarrierPayloadUnit.class);
    }
}
