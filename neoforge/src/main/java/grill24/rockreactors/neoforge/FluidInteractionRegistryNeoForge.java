package grill24.rockreactors.neoforge;

import grill24.rockreactors.RockReactors;
import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * Registers the fluid interaction registry for NeoForge.
 */
public class FluidInteractionRegistryNeoForge {

    public static void register(IEventBus modEventBus) {
        RockReactors.LOGGER.info("Registering NeoForge fluid interaction registry");

        modEventBus.addListener(FluidInteractionRegistryNeoForge::onDataPackRegistry);

        RockReactors.LOGGER.info("Finished registering fluid interaction registry");
    }

    private static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
            FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY,
            FluidInteractionData.CODEC,
            FluidInteractionData.CODEC
        );
    }
}
