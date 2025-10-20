package grill24.rockreactors;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Common logic for handling fluid interactions across both Fabric and NeoForge.
 */
public class FluidInteractionHandler {

    /**
     * Checks if a custom block should be generated when fluids interact.
     *
     * @param level The level/world
     * @param pos The position where the fluid is
     * @param state The block state at the position
     * @return true if a custom block was generated and placed, false to continue with vanilla behavior
     */
    public static boolean handleFluidInteraction(Level level, BlockPos pos, BlockState state) {
        FluidState fluidState = level.getFluidState(pos);

        if (fluidState.is(FluidTags.LAVA)) {
            FluidInteractionManager.InteractionResult result = FluidInteractionManager.findInteractionWithPosition(level, pos, fluidState);

            if (result != null) {
                BlockState generatedBlock = result.getInteraction().getResult();

                if (generatedBlock != null) {
                    int replaceRadius = result.getInteraction().getReplaceRadius();

                    if (result.getInteraction().shouldReplaceAdjacent() && replaceRadius > 1) {
                        // Replace multiple blocks in a Manhattan distance radius around the fluid
                        int blocksReplaced = 0;
                        for (int dx = -replaceRadius; dx <= replaceRadius; dx++) {
                            for (int dy = -replaceRadius; dy <= replaceRadius; dy++) {
                                for (int dz = -replaceRadius; dz <= replaceRadius; dz++) {
                                    // Calculate Manhattan distance
                                    int manhattanDistance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                                    if (manhattanDistance <= replaceRadius && manhattanDistance > 0) {
                                        BlockPos targetPos = pos.offset(dx, dy, dz);
                                        BlockState targetState = level.getBlockState(targetPos);

                                        // Check if this position matches the interaction condition
                                        if (result.getInteraction().shouldInteract(level, pos, targetPos, fluidState)) {
                                            level.setBlockAndUpdate(targetPos, generatedBlock);
                                            blocksReplaced++;
                                        }
                                    }
                                }
                            }
                        }

                        // Handle fluid consumption based on consume_chance
                        float consumeChance = result.getInteraction().getConsumeChance();
                        if (blocksReplaced > 0 && consumeChance > 0.0f && level.getRandom().nextFloat() < consumeChance) {
                            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                        }

                        return blocksReplaced > 0;
                    } else {
                        // Standard behavior: replace single position
                        BlockPos targetPos;
                        BlockPos consumePos;

                        if (result.getInteraction().shouldReplaceAdjacent()) {
                            // Replace the adjacent block with the result
                            targetPos = result.getAdjacentPos();
                            consumePos = pos; // Optionally consume the fluid position
                        } else {
                            // Replace the fluid with the result (default behavior)
                            targetPos = pos;
                            consumePos = result.getAdjacentPos(); // Optionally consume the adjacent block
                        }

                        // Place the generated block at the target position
                        level.setBlockAndUpdate(targetPos, generatedBlock);

                        // Handle consumption based on consume_chance
                        float consumeChance = result.getInteraction().getConsumeChance();
                        if (consumeChance > 0.0f && level.getRandom().nextFloat() < consumeChance) {
                            BlockState consumeState = level.getBlockState(consumePos);
                            FluidState consumeFluid = level.getFluidState(consumePos);

                            // Remove the block or fluid at the consume position
                            if (!consumeFluid.isEmpty()) {
                                // If there's a fluid, remove it by setting to air
                                level.setBlockAndUpdate(consumePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                            } else if (!consumeState.isAir()) {
                                // If there's a block (and no fluid), remove it
                                level.setBlockAndUpdate(consumePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                            }
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if a custom block should be generated when fluids interact.
     * This overload accepts LevelAccessor for compatibility with NeoForge events.
     *
     * @param level The level accessor
     * @param pos The position where the fluid is
     * @param state The block state at the position
     * @return true if a custom block was generated and placed, false to continue with vanilla behavior
     */
    public static boolean handleFluidInteraction(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level worldLevel) {
            return handleFluidInteraction(worldLevel, pos, state);
        }
        return false;
    }
}
