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
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Command to dump all registered fluid interactions.
 */
public class FluidInteractionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("fluidinteractions")
                .requires(source -> source.hasPermission(2)) // Requires operator permission
                .executes((context) -> listInteractionsSimple(context.getSource()))
                .then(Commands.literal("verbose").executes(FluidInteractionCommand::listInteractionsVerbose))
        );
    }

    public static int listInteractionsSimple(CommandSourceStack source) {
        try {
            Registry<FluidInteractionData> registry = source.registryAccess()
                .registryOrThrow(FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY);

            int count = 0;
            for (var entry : registry.entrySet()) {
                ResourceLocation id = entry.getKey().location();
                FluidInteractionData data = entry.getValue();

                // Skip disabled interactions
                if (data.isDisabled()) {
                    continue;
                }

                count++;

                // Build message using Components
                MutableComponent message = Component.empty()
                        .append(hoverItem(data.getFluidText(), data.getFluidItemStackRepresentation()))
                        .append(" + ")
                        .append(formatCondition(data))
                        .append(" -> ")
                        .append(hoverItem(Component.translatable(data.getResultBlock().getDescriptionId()), new ItemStack(data.getResultBlock())));

                source.sendSuccess(() -> message
                        .withStyle(style -> style
                                .withColor(ChatFormatting.AQUA)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(id.toString())))
                        ), false);
            }

            int finalCount = count;
            source.sendSuccess(() -> Component.literal(finalCount + " fluid interactions available")
                .withStyle(ChatFormatting.GREEN), false);

            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error reading fluid interactions: " + e.getMessage())
                .withStyle(ChatFormatting.RED));
        }

        return 0;
    }

    private static int listInteractionsVerbose(CommandContext<CommandSourceStack> context) {
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

                // Build message using Components
                MutableComponent message = Component.literal("[" + id + "] ")
                        .append(data.getFluidType() + " + ")
                        .append(formatCondition(data, true))
                        .append(" -> ")
                        .append(hoverItem(Component.translatable(data.getResultBlock().getDescriptionId()), new ItemStack(data.getResultBlock())));

                source.sendSuccess(() -> message
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

    private static MutableComponent formatCondition(FluidInteractionData data, boolean simple) {
        FluidInteractionData.InteractionConditionData condition = data.getCondition();

        if (!simple) {
            return switch (condition.getType()) {
                case "adjacent_block" -> condition.getBlock()
                        .map(block -> Component.literal("adjacent_block(")
                                .append(hoverItem(Component.translatable(block.getDescriptionId()), new ItemStack(block)))
                                .append(")"))
                        .orElse(Component.literal("adjacent_block(unknown)"));

                case "adjacent_fluid_with_y" -> {
                    Component fluidComponent = condition.getFluid()
                            .map(fluid -> Component.translatable(BuiltInRegistries.FLUID.getKey(fluid).toString()))
                            .orElse(Component.literal("unknown_fluid"));

                    MutableComponent result = Component.literal("adjacent_fluid(").append(fluidComponent);

                    if (condition.getYCondition().isPresent()) {
                        result = result.append(Component.literal(", " + condition.getYCondition().get()));
                    }

                    result = result.append(Component.literal(")"));
                    yield result;
                }

                default -> Component.literal(condition.getType() + "(unknown)");
            };
        } else {
            return switch (condition.getType()) {
                case "adjacent_block" -> condition.getBlock()
                        .map(block -> hoverItem(Component.translatable(block.getDescriptionId()), new ItemStack(block)))
                        .orElse(Component.translatable(Blocks.AIR.getDescriptionId()));
                case "adjacent_fluid_with_y" -> condition.getFluid()
                        .map(fluid -> Component.translatable(BuiltInRegistries.FLUID.getKey(fluid).toString()))
                        .orElse(Component.literal("error_fluid"));

                default -> Component.literal(condition.getType());
            };
        }
    }

    /**
     * Adds a hover event to display item information using GelatinUI if available,
     * otherwise don't add a hover event.
     * @param component
     * @param item
     * @return
     */
    private static MutableComponent hoverItem(MutableComponent component, ItemStack item) {
        try {
            Class<?> tooltipClass = Class.forName("io.github.currenj.gelatinui.tooltip.ItemStacksTooltip");
            Class<?> infoClass = Class.forName("io.github.currenj.gelatinui.tooltip.ItemStacksInfo");

            Object action = tooltipClass.getField("SHOW_ITEM_STACKS").get(null);
            Object info = infoClass.getConstructor(List.class).newInstance(List.of(item));

            HoverEvent hoverEvent = new HoverEvent((HoverEvent.Action) action, info);
            return component.withStyle(style -> style.withHoverEvent(hoverEvent));
        } catch (Exception e) {
            // Fallback - no hover event
            return component;
        }
    }

    private static Component formatCondition(FluidInteractionData data) {
        return formatCondition(data, true);
    }
}
