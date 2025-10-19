package grill24.rockreactors.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import grill24.rockreactors.RockReactors;

@Mod(RockReactors.MOD_ID)
public final class RockReactorsNeoForge {
    public RockReactorsNeoForge(IEventBus modEventBus) {
        // Register the fluid interaction registry
        FluidInteractionRegistryNeoForge.register(modEventBus);

        // Run our common setup.
        RockReactors.init();

        // Register the common setup method for modloading
        modEventBus.addListener(this::onCommonSetup);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        // Common setup - fluid interactions are registered when server starts
    }
}
