package grill24.rockreactors.fabric;

import net.fabricmc.api.ModInitializer;

import grill24.rockreactors.RockReactors;

public final class RockReactorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Register the fluid interaction registry
        FluidInteractionRegistryFabric.register();

        // Register commands
        CommandRegistrationFabric.register();

        // Run our common setup.
        RockReactors.init();
    }
}
