package grill24.rockreactors.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import grill24.rockreactors.compat.CompatUtil;
import grill24.rockreactors.compat.GelatinOpenMenuCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.ClickEvent;

/**
 * Command to open the Rock Reactors UI screen.
 */
public class RockReactorsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("rockreactors")
                .executes(RockReactorsCommand::openScreen)
        );
    }

    private static int openScreen(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer player) {
            if (!CompatUtil.isGelatinUiPresent()) {
                // Show simple fluid interactions summary when Gelatin UI is not available
                FluidInteractionCommand.listInteractionsSimple(source);
                // Append dependency suggestion
                Component installMessage = Component.literal("Install ")
                    .append(Component.literal("Gelatin UI")
                        .withStyle(style -> style
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/gelatin-ui"))
                            .withUnderlined(true)))
                    .append(Component.literal(" for a visual display.")
                        .withStyle(ChatFormatting.YELLOW));
                source.sendSuccess(() -> installMessage, false);
                return 1;
            } else {
                GelatinOpenMenuCompat.openRockReactorsMenu(player);
            }
        } else {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }

        return 1;
    }
}
