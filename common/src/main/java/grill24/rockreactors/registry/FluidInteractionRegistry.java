package grill24.rockreactors.registry;

import grill24.rockreactors.RockReactors;
import grill24.rockreactors.data.FluidInteractionData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry constants for fluid interactions.
 */
public class FluidInteractionRegistry {
    public static final ResourceKey<Registry<FluidInteractionData>> FLUID_INTERACTION_REGISTRY_KEY =
        ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(RockReactors.MOD_ID, "fluid_interaction"));

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RockReactors.MOD_ID, path);
    }
}
