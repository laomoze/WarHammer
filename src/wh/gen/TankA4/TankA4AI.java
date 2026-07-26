package wh.gen.TankA4;

import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import wh.content.WHUnitCommands;

public class TankA4AI extends CommandAI {
    @Override
    public void updateUnit() {
        updateDeploymentCommand();
        super.updateUnit();
    }

    protected void updateDeploymentCommand() {
        if (!(unit instanceof TankA4 tank)) return;

        if (currentCommand() != null && WHUnitCommands.deploy != null && currentCommand().name.equals(WHUnitCommands.deploy.name)) {
            if (!tank.isDeployCommandHandled()) {
                tank.requestDeployment(!tank.isDeploymentRequested());
                resetToMoveCommand();
                tank.setDeployCommandHandled(true);
            }
        } else {
            tank.setDeployCommandHandled(false);
        }
    }

    protected void resetToMoveCommand() {
        command = UnitCommand.moveCommand;
    }
}
