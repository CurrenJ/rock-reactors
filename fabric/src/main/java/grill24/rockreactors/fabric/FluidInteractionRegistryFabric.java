package grill24.rockreactors.fabric;

import grill24.rockreactors.RockReactors;
import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

/**
 * Registers the fluid interaction registry for Fabric.
 */
public class FluidInteractionRegistryFabric {

    public static void register() {
        RockReactors.LOGGER.info("Registering Fabric fluid interaction registry");

        // Register the dynamic registry for fluid interactions
        DynamicRegistries.registerSynced(
            FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY,
            FluidInteractionData.CODEC,
            FluidInteractionData.CODEC
        );

        RockReactors.LOGGER.info("Finished registering fluid interaction registry");
    }
}
