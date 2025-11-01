package grill24.rockreactors.fabric;

import net.fabricmc.api.ModInitializer;

public final class RockReactorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register the fluid interaction registry
        FluidInteractionRegistryFabric.register();

        // Register commands
        CommandRegistrationFabric.register();
    }
}
