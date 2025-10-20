package grill24.rockreactors;

import grill24.rockreactors.data.FluidInteractionData;
import grill24.rockreactors.registry.FluidInteractionRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all fluid interaction rules for the mod.
 * Now uses a datapack-driven registry instead of hardcoded interactions.
 */
public class FluidInteractionManager {

    /**
     * Result of a fluid interaction check, containing the interaction data and adjacent position.
     */
    public static class InteractionResult {
        private final FluidInteractionData interaction;
        private final net.minecraft.core.BlockPos adjacentPos;

        public InteractionResult(FluidInteractionData interaction, net.minecraft.core.BlockPos adjacentPos) {
            this.interaction = interaction;
            this.adjacentPos = adjacentPos;
        }

        public FluidInteractionData getInteraction() {
            return interaction;
        }

        public net.minecraft.core.BlockPos getAdjacentPos() {
            return adjacentPos;
        }
    }

    /**
     * Gets all registered fluid interactions from the registry.
     *
     * @param registryAccess The registry access to get the fluid interactions from
     * @return List of all enabled fluid interactions
     */
    public static List<FluidInteractionData> getInteractions(RegistryAccess registryAccess) {
        Registry<FluidInteractionData> registry = registryAccess.registryOrThrow(FluidInteractionRegistry.FLUID_INTERACTION_REGISTRY_KEY);
        List<FluidInteractionData> interactions = new ArrayList<>();
        registry.forEach(interaction -> {
            if (!interaction.isDisabled()) {
                interactions.add(interaction);
            }
        });
        return interactions;
    }

    /**
     * Finds the first interaction that should occur at the given position.
     * Returns null if no interaction should occur.
     */
    public static InteractionResult findInteractionWithPosition(net.minecraft.world.level.Level level,
                                                  net.minecraft.core.BlockPos currentPos,
                                                  net.minecraft.world.level.material.FluidState currentState) {
        List<FluidInteractionData> interactions = getInteractions(level.registryAccess());

        if (currentState.is(net.minecraft.tags.FluidTags.LAVA)) {
            for (net.minecraft.core.Direction direction : net.minecraft.world.level.block.LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
                net.minecraft.core.BlockPos adjacentPos = currentPos.relative(direction.getOpposite());

                for (FluidInteractionData interaction : interactions) {
                    if (interaction.shouldInteract(level, currentPos, adjacentPos, currentState)) {
                        return new InteractionResult(interaction, adjacentPos);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Finds the first interaction that should occur at the given position.
     * Returns null if no interaction should occur.
     * @deprecated Use {@link #findInteractionWithPosition(net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.level.material.FluidState)} instead
     */
    @Deprecated
    public static FluidInteractionData findInteraction(net.minecraft.world.level.Level level,
                                                  net.minecraft.core.BlockPos currentPos,
                                                  net.minecraft.core.BlockPos relativePos,
                                                  net.minecraft.world.level.material.FluidState currentState) {
        List<FluidInteractionData> interactions = getInteractions(level.registryAccess());
        for (FluidInteractionData interaction : interactions) {
            if (interaction.shouldInteract(level, currentPos, relativePos, currentState)) {
                return interaction;
            }
        }
        return null;
    }

    /**
     * Gets the generated block for a fluid interaction, or null if no interaction should occur.
     * This method maintains compatibility with the existing BlockGenerator interface.
     */
    public static net.minecraft.world.level.block.state.BlockState getGeneratedBlock(net.minecraft.world.level.Level level,
                                                                                     net.minecraft.core.BlockPos pos,
                                                                                     net.minecraft.world.level.block.state.BlockState adjacentState) {
        net.minecraft.world.level.material.FluidState fluidState = level.getFluidState(pos);

        if (fluidState.is(net.minecraft.tags.FluidTags.LAVA)) {
            for (net.minecraft.core.Direction direction : net.minecraft.world.level.block.LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
                net.minecraft.core.BlockPos adjacentPos = pos.relative(direction.getOpposite());

                FluidInteractionData interaction =  findInteraction(level, pos, adjacentPos, fluidState);
                if (interaction != null) {
                    return interaction.getResult();
                }
            }
        }

        return null;
    }
}
