package com.limachi.dim_bag.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public class FluidUtils {
    public static class AirFluidBlockWrapper implements IFluidHandler {
        protected final Level level;
        protected final BlockPos pos;
        public AirFluidBlockWrapper(Level level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        @Override
        public int getTanks() { return level.getBlockState(pos).getBlock() instanceof AirBlock ? 1 : 0; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

        @Override
        public int getTankCapacity(int tank) { return level.getBlockState(pos).getBlock() instanceof AirBlock ? FluidType.BUCKET_VOLUME : 0; }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return level.getBlockState(pos).getBlock() instanceof AirBlock; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getAmount() < FluidType.BUCKET_VOLUME)
                return 0;
            if (action.execute()) {
                Fluid fluid = resource.getFluid();
                BlockState state = fluid.getFluidType().getBlockForFluidState(level, pos, fluid.defaultFluidState());
                FluidUtil.destroyBlockOnFluidPlacement(level, pos);
                level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            }
            return FluidType.BUCKET_VOLUME;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    public static Optional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        if (level == null || pos == null) return Optional.empty();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidBlock fluidBlock)
            return Optional.of(new FluidBlockWrapper(fluidBlock, level, pos));
        if (block instanceof BucketPickup pickup)
            return Optional.of(new BucketPickupHandlerWrapper(pickup, level, pos));
        if (block instanceof AirBlock)
            return Optional.of(new AirFluidBlockWrapper(level, pos));
        if (state.hasBlockEntity()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null)
                return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).resolve();
        }
        return Optional.empty();
    }
}
