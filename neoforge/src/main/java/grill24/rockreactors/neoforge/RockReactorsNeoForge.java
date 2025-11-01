package grill24.rockreactors.neoforge;

import grill24.rockreactors.compat.GelatinMenusCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import grill24.rockreactors.RockReactors;

@Mod(RockReactors.MOD_ID)
public final class RockReactorsNeoForge {
    public RockReactorsNeoForge(IEventBus modEventBus) {
        // Try to register GelatinUI menus, if GelatinUI is present.
        GelatinMenusCompat.init();

        // Register the fluid interaction registry
        FluidInteractionRegistryNeoForge.register(modEventBus);
    }
}
