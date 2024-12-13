package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.block_entities.IRenderUsingItemTag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.dim_bag.save_datas.bag_data.TankData;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//FIXME: redo rendering
public class TankModuleBlockEntity extends BlockEntity implements IRenderUsingItemTag {

    @RegisterBlockEntity(blocks = "tank_module")
    public static RegistryObject<BlockEntityType<TankModuleBlockEntity>> R_TYPE;

    public TankModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    private LazyOptional<IFluidHandler> tankHandle = null;
    public FluidStack renderStack = FluidStack.EMPTY;

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (tankHandle == null) {
                BagInstance bag = BagsData.getBagHandle(level, getBlockPos(), ()->tankHandle = null);
                if (bag != null) {
                    tankHandle = bag.tankHandle(getBlockPos()).cast();
                    tankHandle.addListener(t -> tankHandle = null);
                }
            }
            if (tankHandle != null)
                return tankHandle.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag out = super.getUpdateTag();
        out.put("render_stack", renderStack.writeToNBT(new CompoundTag()));
        return out;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("render_stack", renderStack.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        renderStack = FluidStack.loadFluidStackFromNBT(tag.getCompound("render_stack"));
    }

    public void tick() {
        if (World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level)
            getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(h->{
                FluidStack real = h.getFluidInTank(0);
                int qty = Mth.clamp((int)Math.ceil(((double)real.getAmount() / (double)h.getTankCapacity(0)) * 1000.), 0, 1000);
                if (!renderStack.isFluidEqual(h.getFluidInTank(0)) || qty != renderStack.getAmount()) {
                    renderStack = h.getFluidInTank(0).copy();
                    if (renderStack.getRawFluid() != Fluids.EMPTY)
                        renderStack.setAmount(qty);
                    setChanged();
                    level.players().forEach(p->p.connection.send(getUpdatePacket()));
                }
            });
    }

    @Override
    public void prepareRender(CompoundTag tag) {
        renderStack = FluidStack.loadFluidStackFromNBT(tag);
        int qty = Mth.clamp((int)Math.ceil(((double)renderStack.getAmount() / (double) TankData.DEFAULT_CAPACITY) * 1000.), 0, 1000);
        if (renderStack.getRawFluid() != Fluids.EMPTY)
            renderStack.setAmount(qty);
    }
}
