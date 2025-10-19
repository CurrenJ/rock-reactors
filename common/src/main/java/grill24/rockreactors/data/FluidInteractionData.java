package grill24.rockreactors.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
            Codec.STRING.fieldOf("result").forGetter(data -> data.resultString)
        ).apply(instance, FluidInteractionData::new)
    );

    private final String fluidType;
    private final InteractionConditionData condition;
    private final String resultString;
    private final Block resultBlock;
    private final boolean disabled;

    public FluidInteractionData(String fluidType, InteractionConditionData condition, String resultString) {
        this.fluidType = fluidType;
        this.condition = condition;
        this.resultString = resultString;

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
