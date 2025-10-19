package grill24.rockreactors.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Command to dump all registered fluid interactions.
 */
public class FluidInteractionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("fluidinteractions")
                .requires(source -> source.hasPermission(2)) // Requires operator permission
                .executes(FluidInteractionCommand::listInteractions)
        );
    }

    private static int listInteractions(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Registry<FluidInteractionData> registry = source.registryAccess()
                .registryOrThrow(FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY);

            // Header
            source.sendSuccess(() -> Component.literal("=== Fluid Interactions Registry ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

            int count = 0;
            for (var entry : registry.entrySet()) {
                ResourceLocation id = entry.getKey().location();
                FluidInteractionData data = entry.getValue();

                // Skip disabled interactions
                if (data.isDisabled()) {
                    continue;
                }

                count++;

                // Format: [namespace:path] fluid_type + condition -> result
                String conditionDesc = formatCondition(data);
                String message = String.format("[%s] %s + %s -> %s",
                    id,
                    data.getFluidType(),
                    conditionDesc,
                    data.getResultBlock().getDescriptionId()
                );

                source.sendSuccess(() -> Component.literal(message)
                    .withStyle(ChatFormatting.AQUA), false);
            }

            // Footer
            final int totalCount = count;
            source.sendSuccess(() -> Component.literal("Total interactions: " + totalCount)
                .withStyle(ChatFormatting.GREEN), false);

            return count;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error reading fluid interactions: " + e.getMessage())
                .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static String formatCondition(FluidInteractionData data) {
        FluidInteractionData.InteractionConditionData condition = data.getCondition();

        return switch (condition.getType()) {
            case "adjacent_block" ->
                condition.getBlock()
                    .map(block -> "adjacent_block(" + block.getDescriptionId() + ")")
                    .orElse("adjacent_block(unknown)");

            case "adjacent_fluid_with_y" -> {
                StringBuilder sb = new StringBuilder("adjacent_fluid(");
                condition.getFluid().ifPresent(fluid ->
                    sb.append(BuiltInRegistries.FLUID.getKey(fluid)));
                condition.getYCondition().ifPresent(yCondition ->
                    sb.append(", ").append(yCondition));
                sb.append(")");
                yield sb.toString();
            }

            default -> condition.getType() + "(unknown)";
        };
    }
}
