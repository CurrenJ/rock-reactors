package grill24.rockreactors.fabric;

import grill24.rockreactors.command.FluidInteractionCommand;
import grill24.rockreactors.command.RockReactorsCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Registers commands for Fabric.
 */
public class CommandRegistrationFabric {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            FluidInteractionCommand.register(dispatcher);
            RockReactorsCommand.register(dispatcher);
        });
    }
}
