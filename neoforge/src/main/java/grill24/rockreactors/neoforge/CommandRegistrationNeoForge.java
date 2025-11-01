package grill24.rockreactors.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import grill24.rockreactors.command.FluidInteractionCommand;
import grill24.rockreactors.command.RockReactorsCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import grill24.rockreactors.RockReactors;

/**
 * Registers commands for NeoForge.
 */
@EventBusSubscriber(modid = RockReactors.MOD_ID)
public class CommandRegistrationNeoForge {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        FluidInteractionCommand.register(dispatcher);
        RockReactorsCommand.register(dispatcher);
    }
}
