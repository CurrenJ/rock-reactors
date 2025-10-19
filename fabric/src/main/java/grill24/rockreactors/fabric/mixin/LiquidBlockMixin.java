package grill24.rockreactors.fabric.mixin;

import grill24.rockreactors.FluidInteractionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public class LiquidBlockMixin {

    @Shadow
    private void fizz(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {}

    @Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true, require = 1)
    private void onShouldSpreadLiquid(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (FluidInteractionHandler.handleFluidInteraction(level, pos, state)) {
            this.fizz(level, pos);
            cir.setReturnValue(false);
        }
    }
}
