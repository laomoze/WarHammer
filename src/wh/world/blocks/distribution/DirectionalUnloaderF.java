package wh.world.blocks.distribution;

import mindustry.world.blocks.distribution.*;

public class DirectionalUnloaderF extends DirectionalUnloader {
    public DirectionalUnloaderF(String name) {
        super(name);
    }

    public class DirectionalUnloaderBuildF extends DirectionalUnloaderBuild {
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
