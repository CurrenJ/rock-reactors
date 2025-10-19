package grill24.rockreactors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
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
            for (Direction direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
                BlockPos adjacentPos = pos.relative(direction.getOpposite());
                BlockState adjacentState = level.getBlockState(adjacentPos);

                BlockState generatedBlock = FluidInteractionManager.getGeneratedBlock(level, pos, adjacentState);

                if (generatedBlock != null) {
                    level.setBlockAndUpdate(pos, generatedBlock);
                    return true;
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
