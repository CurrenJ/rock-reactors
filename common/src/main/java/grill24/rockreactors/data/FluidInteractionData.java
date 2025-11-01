package grill24.rockreactors.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

/**
 * Data-driven representation of a fluid interaction that can be loaded from JSON.
 */
public class FluidInteractionData {
    public static final Codec<FluidInteractionData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("fluid_type").forGetter(data -> data.fluidType),
            InteractionConditionData.CODEC.fieldOf("condition").forGetter(data -> data.condition),
            Codec.STRING.fieldOf("result").forGetter(data -> data.resultString),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("consume_chance", 0.0f).forGetter(data -> data.consumeChance),
            Codec.BOOL.optionalFieldOf("replace_adjacent", false).forGetter(data -> data.replaceAdjacent),
            Codec.intRange(1, 16).optionalFieldOf("replace_radius", 1).forGetter(data -> data.replaceRadius),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("success_chance", 1.0f).forGetter(data -> data.successChance)
        ).apply(instance, FluidInteractionData::new)
    );

    private final String fluidType;
    private final InteractionConditionData condition;
    private final String resultString;
    private final Block resultBlock;
    private final boolean disabled;
    private final float consumeChance;
    private final boolean replaceAdjacent;
    private final int replaceRadius;
    private final float successChance;

    public FluidInteractionData(String fluidType, InteractionConditionData condition, String resultString, float consumeChance, boolean replaceAdjacent, int replaceRadius, float successChance) {
        this.fluidType = fluidType;
        this.condition = condition;
        this.resultString = resultString;
        this.consumeChance = consumeChance;
        this.replaceAdjacent = replaceAdjacent;
        this.replaceRadius = replaceRadius;
        this.successChance = successChance;

        if ("none".equals(resultString)) {
            this.resultBlock = null;
            this.disabled = true;
        } else {
            ResourceLocation blockId = ResourceLocation.tryParse(resultString);
            if (blockId != null) {
                this.resultBlock = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
                this.disabled = false;
            } else {
                this.resultBlock = null;
                this.disabled = true;
            }
        }
    }

    public boolean shouldInteract(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState) {
        return !disabled && testFluidType(currentState) && condition.test(level, currentPos, relativePos, currentState);
    }

    private boolean testFluidType(FluidState fluidState) {
        return switch (fluidType) {
            case "lava_source" -> fluidState.is(net.minecraft.tags.FluidTags.LAVA) && fluidState.isSource();
            case "lava_flowing" -> fluidState.is(net.minecraft.tags.FluidTags.LAVA) && !fluidState.isSource();
            case "any_lava" -> fluidState.is(net.minecraft.tags.FluidTags.LAVA);
            default -> false;
        };
    }

    public BlockState getResult() {
        return resultBlock != null ? resultBlock.defaultBlockState() : null;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getFluidType() {
        return fluidType;
    }

    public InteractionConditionData getCondition() {
        return condition;
    }

    public Block getResultBlock() {
        return resultBlock;
    }

    public float getConsumeChance() {
        return consumeChance;
    }

    public boolean shouldReplaceAdjacent() {
        return replaceAdjacent;
    }

    public int getReplaceRadius() {
        return replaceRadius;
    }

    public float getSuccessChance() {
        return successChance;
    }

    // ----- Convenience methods for UI representation -----

    public MutableComponent getFluidText() {
        return switch (fluidType) {
            case "lava_source", "any_lava", "lava_flowing" -> Component.translatable(Blocks.LAVA.getDescriptionId());
            case "water_source", "any_water", "water_flowing" -> Component.translatable(Blocks.WATER.getDescriptionId());
            default -> Component.literal(fluidType);
        };
    }

    public ItemStack getFluidItemStackRepresentation() {
        return switch (fluidType) {
            case "lava_source", "lava_flowing", "any_lava" -> new ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET);
            case "water_source", "water_flowing", "any_water" -> new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Represents the condition part of a fluid interaction.
     */
    public static class InteractionConditionData {
        public static final Codec<InteractionConditionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("type").forGetter(data -> data.type),
                BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(data -> data.block),
                BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(data -> data.fluid),
                Codec.STRING.optionalFieldOf("y_condition").forGetter(data -> data.yCondition)
            ).apply(instance, InteractionConditionData::new)
        );

        private final String type;
        private final Optional<Block> block;
        private final Optional<Fluid> fluid;
        private final Optional<String> yCondition;

        public InteractionConditionData(String type, Optional<Block> block, Optional<Fluid> fluid, Optional<String> yCondition) {
            this.type = type;
            this.block = block;
            this.fluid = fluid;
            this.yCondition = yCondition;
        }

        public boolean test(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState) {
            return switch (type) {
                case "adjacent_block" -> block.map(b -> level.getBlockState(relativePos).is(b)).orElse(false);
                case "adjacent_fluid_with_y" -> {
                    if (fluid.isEmpty()) yield false;
                    FluidState adjacentFluid = level.getFluidState(relativePos);
                    if (!adjacentFluid.is(fluid.get())) yield false;

                    // Check Y condition if present
                    if (yCondition.isPresent()) {
                        yield switch (yCondition.get()) {
                            case "below_zero" -> currentPos.getY() < 0;
                            case "above_zero" -> currentPos.getY() >= 0;
                            default -> true;
                        };
                    }
                    yield true;
                }
                default -> false;
            };
        }

        public String getType() {
            return type;
        }

        public Optional<Block> getBlock() {
            return block;
        }

        public Optional<Fluid> getFluid() {
            return fluid;
        }

        public Optional<String> getYCondition() {
            return yCondition;
        }
    }
}
