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
                        // Check success chance before doing area replacement
                        float successChance = result.getInteraction().getSuccessChance();
                        if (successChance < 1.0f && level.getRandom().nextFloat() >= successChance) {
                            // Interaction failed - play effects but don't place blocks
                            playFailureEffects(level, pos);
                            return true; // Return true to prevent vanilla behavior
                        }

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
                        BlockPos consumePos = result.getAdjacentPos();

                        if (result.getInteraction().shouldReplaceAdjacent()) {
                            // Replace the adjacent block with the result
                            targetPos = result.getAdjacentPos();
                        } else {
                            // Replace the fluid with the result (default behavior)
                            targetPos = pos;
                        }

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
                                level.destroyBlock(consumePos, true);
                            }
                        }

                        // Check success chance for single block replacement
                        float successChance = result.getInteraction().getSuccessChance();
                        if (successChance < 1.0f && level.getRandom().nextFloat() >= successChance) {
                            // Interaction failed - play effects but don't place blocks
                            playFailureEffects(level, pos);
                        }  else {
                            // Place the generated block at the target position
                            level.setBlockAndUpdate(targetPos, generatedBlock);
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

    /**
     * Plays particle effects and sound when a fluid interaction fails due to success_chance.
     */
    private static void playFailureEffects(Level level, BlockPos pos) {
        // Play fizz sound effect
        level.levelEvent(1501, pos, 0);

        // Spawn smoke particles
        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8;
            double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8;
            double dx = (level.getRandom().nextDouble() - 0.5) * 0.2;
            double dy = level.getRandom().nextDouble() * 0.1;
            double dz = (level.getRandom().nextDouble() - 0.5) * 0.2;

            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, dx, dy, dz);
        }
    }
}
