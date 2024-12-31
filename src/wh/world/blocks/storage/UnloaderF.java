package wh.world.blocks.storage;

import mindustry.world.blocks.storage.*;

/**
 * AN unloader that is not affected by game frame rates.
 *
 * @author Eipusino
 */
public class UnloaderF extends Unloader {
    public UnloaderF(String name) {
        super(name);
    }

    public class UnloaderBuildF extends UnloaderBuild {
        protected float counter;

        @Override
        public void updateTile() {
            counter += edelta();

            while (counter >= speed) {
                unloadTimer = speed;
                super.updateTile();
                counter -= speed;
            }
        }
    }
}
