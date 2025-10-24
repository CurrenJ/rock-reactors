package grill24.rockreactors.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import grill24.rockreactors.RockReactors;

public class RockReactorsFabricServer implements net.fabricmc.api.DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        // Built-in datapacks are automatically registered from resources/data/
        // No additional registration needed - Minecraft discovers them automatically
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            RockReactors.LOGGER.info("Rock Reactors built-in datapacks loaded from resources/data/");
        });
    }
}
