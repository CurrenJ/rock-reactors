package grill24.rockreactors.neoforge;

import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

import grill24.rockreactors.RockReactors;

@EventBusSubscriber(modid = RockReactors.MOD_ID)
public class FluidInteractionIntegrationNeoForge {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        registerFluidInteractions(server);
    }

    private static void registerFluidInteractions(MinecraftServer server) {
        try {
            Registry<FluidInteractionData> registry = server.registryAccess().registryOrThrow(FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY);

            registry.forEach(interaction -> {
                if (!interaction.isDisabled()) {
                    // Convert our FluidInteractionData to NeoForge's InteractionInformation
                    net.neoforged.neoforge.fluids.FluidInteractionRegistry.InteractionInformation info =
                        new net.neoforged.neoforge.fluids.FluidInteractionRegistry.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> interaction.shouldInteract(level, currentPos, relativePos, currentState),
                        interaction.getResult()
                    );

                    // Register with NeoForge's registry for lava type
                    net.neoforged.neoforge.fluids.FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), info);
                }
            });

            RockReactors.LOGGER.info("[NeoForge] Registered {} fluid interactions with NeoForge registry",
                registry.stream().filter(i -> !i.isDisabled()).count());
        } catch (Exception e) {
            RockReactors.LOGGER.error("[NeoForge] Failed to register fluid interactions", e);
        }
    }
}
