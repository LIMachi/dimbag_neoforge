package com.limachi.dim_bag.mixin;

import com.limachi.dim_bag.DimBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Inject(method = "isPushable", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 4, shift = At.Shift.BEFORE))
    private static void preventVanillaMovementOfWalls(BlockState state, Level level, BlockPos pos, Direction dir, boolean destroy, Direction restrict, CallbackInfoReturnable<Boolean> cir) {
        if (DimBag.isWall(level, pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
