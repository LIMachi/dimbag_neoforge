package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.block_entities.IRenderUsingItemTag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotModuleBlockEntity extends BlockEntity implements IRenderUsingItemTag {

    @RegisterBlockEntity(blocks = "slot_module")
    public static RegistryObject<BlockEntityType<SlotModuleBlockEntity>> R_TYPE;

    public SlotModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    private LazyOptional<IItemHandler> slotHandle = null;
    public ItemStack renderStack = ItemStack.EMPTY;

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (slotHandle == null) {
                BagInstance bag = BagsData.getBagHandle(level, getBlockPos(), ()->slotHandle = null);
                if (bag != null) {
                    slotHandle = bag.slotHandle(getBlockPos());
                    slotHandle.addListener(t -> slotHandle = null);
                }
            }
            if (slotHandle != null)
                return slotHandle.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag out = super.getUpdateTag();
        out.put("render_stack", renderStack.serializeNBT());
        return out;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("render_stack", renderStack.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        renderStack = ItemStack.of(tag.getCompound("render_stack"));
        if (renderStack.getCount() > 64)
            renderStack.setCount(64);
    }

    public void tick() {
        if (World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level)
            getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h->{
                ItemStack stack = h.getStackInSlot(0);
                if (stack != null && (!renderStack.equals(stack, false) || !renderStack.equals(stack.copyWithCount(64), false))) {
                    renderStack = stack.copy();
                    if (renderStack.getCount() > 64)
                        renderStack.setCount(64);
                    setChanged();
                    level.players().forEach(p->p.connection.send(getUpdatePacket()));
                }
            });
    }

    @Override
    public void prepareRender(CompoundTag tag) {
        renderStack = ItemStack.of(tag);
        if (renderStack.getCount() > 64)
            renderStack.setCount(64);
    }
}
