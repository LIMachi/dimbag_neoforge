package com.limachi.dim_bag.save_datas.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.lim_lib.containers.ISlotAccessContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

public class SlotData implements IItemHandlerModifiable, Container {

    public static final Component DEFAULT_SLOT_LABEL = Component.translatable("block.dim_bag.slot_module");
    private LazyOptional<SlotData> handle = LazyOptional.of(()->this);

    public static class SlotEntry implements ISlotAccessContainer {
        private final BlockPos pos;
        private Component label;
        private final ItemStack[] main_stack = new ItemStack[]{ItemStack.EMPTY};
        private int max_size_in_stacks;
        private final SlotAccess sa = new SlotAccess() {
            @Override
            @Nonnull
            public ItemStack get() {
                return main_stack[0];
            }

            @Override
            public boolean set(@Nonnull ItemStack stack) {
                main_stack[0] = stack;
                return true;
            }
        };

        public SlotEntry(CompoundTag data) {
            main_stack[0] = ItemStack.of(data);
            if (data.contains("count_override", Tag.TAG_INT))
                main_stack[0].setCount(data.getInt("count_override"));
            max_size_in_stacks = data.getInt("max_size_in_stacks");
            pos = BlockPos.of(data.getLong(BagInstance.POSITION));
            label = Component.Serializer.fromJson(data.getString("label"));
            if (label == null)
                label = DEFAULT_SLOT_LABEL;
            if (max_size_in_stacks <= 0)
                max_size_in_stacks = 4;
        }

        public CompoundTag serialize() {
            int count_override = main_stack[0].getCount();
            CompoundTag out;
            if (count_override > 127) {
                main_stack[0].setCount(1);
                out = main_stack[0].serializeNBT();
                out.putInt("count_override", count_override);
                main_stack[0].setCount(count_override);
            } else
                out = main_stack[0].serializeNBT();
            out.putInt("max_size_in_stacks", max_size_in_stacks);
            out.putLong(BagInstance.POSITION, pos.asLong());
            out.putString("label", Component.Serializer.toJson(label));
            return out;
        }

        @Override
        public SlotAccess getSlotAccess(int i) {
            return sa;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return main_stack[0].getMaxStackSize() * max_size_in_stacks;
        }

        public int getMaxSizeInStacks() {
            return max_size_in_stacks;
        }

        public void setMaxSizeInStacks(int stacks) {
            max_size_in_stacks = stacks;
            setChanged();
        }
    }

    private final int bag;
    private final ArrayList<SlotEntry> stacks = new ArrayList<>();
    private final HashMap<BlockPos, LazyOptional<SlotEntry>> handles = new HashMap<>();

    public int getSlot(BlockPos slot) {
        for (int i = 0; i < stacks.size(); ++i)
            if (stacks.get(i).pos.equals(slot))
                return i;
        return -1;
    }

    public BlockPos getSlot(int slot) {
        if (slot < 0 || slot >= stacks.size()) return null;
        return stacks.get(slot).pos;
    }

    public LazyOptional<IItemHandler> getSlotHandle(BlockPos pos) {
        if (pos == null)
            return null;
        return handles.computeIfAbsent(pos, k->{
            final int slot = getSlot(pos);
            return slot != -1 ? LazyOptional.of(()->stacks.get(slot)) : LazyOptional.empty();
        }).cast();
    }

    public Component getSlotLabel(BlockPos pos) {
        int slot = getSlot(pos);
        if (slot != -1)
            return stacks.get(slot).label;
        return DEFAULT_SLOT_LABEL;
    }

    public void setSlotLabel(BlockPos pos, Component label) {
        int slot = getSlot(pos);
        if (slot != -1) {
            stacks.get(slot).label = label;
            handles.remove(pos).invalidate();
        }
    }

    public CompoundTag uninstallSlot(BlockPos pos) {
        int i = getSlot(pos);
        if (i != -1) {
            handles.remove(pos).invalidate();
            CompoundTag out = stacks.remove(i).serialize();
            out.remove(BagInstance.POSITION);
            invalidate();
            return out;
        }
        return new CompoundTag();
    }

    public void installSlot(BlockPos pos, CompoundTag data) {
        if (handles.containsKey(pos)) { //should never happen
            LazyOptional<SlotEntry> prev = handles.remove(pos);
            stacks.remove(getSlot(pos));
            prev.invalidate();
        }
        data.putLong(BagInstance.POSITION, pos.asLong());
        stacks.add(new SlotEntry(data));
        if (handle != null)
            handle.invalidate(); //we invalidate the global handle to force all global inventories to reload
        handle = null;
    }

    public void invalidate() {
        for (LazyOptional<SlotEntry> handle : handles.values())
            handle.invalidate();
        handles.clear();
        if (handle != null)
            handle.invalidate();
        handle = null;
    }

    public LazyOptional<SlotData> getHandle() {
        if (handle == null)
            handle = LazyOptional.of(()->this);
        return handle;
    }

    protected SlotData(int bag, ListTag slots) {
        this.bag = bag;
        for (int i = 0; i < slots.size(); ++i)
            stacks.add(new SlotEntry(slots.getCompound(i)));
    }

    protected ListTag serialize() {
        ListTag out = new ListTag();
        for (SlotEntry entry : stacks)
            out.add(entry.serialize());
        return out;
    }

    @Override
    public int getSlots() { return stacks.size(); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot >= 0 && slot < stacks.size())
            stacks.get(slot).setStackInSlot(0, stack);
    }

    @Override
    public @Nonnull ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < stacks.size() ? stacks.get(slot).getStackInSlot(0) : ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || slot < 0 || slot >= stacks.size()) return stack;
        return stacks.get(slot).insertItem(0, stack, simulate);
    }

    @Override
    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot < 0 || slot >= stacks.size()) return ItemStack.EMPTY;
        return stacks.get(slot).extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof BagItem) || BagItem.getBagId(stack) != bag;
    }

    @Override
    public int getContainerSize() { return getSlots(); }

    @Override
    public boolean isEmpty() { return stacks.stream().allMatch(ISlotAccessContainer::isEmpty); }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) { return getStackInSlot(slot); }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) { return extractItem(slot, amount, false); }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) { return extractItem(slot, Integer.MAX_VALUE, false); }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) { setStackInSlot(slot, stack); }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(@Nonnull Player player) { return DimBag.getBagAccess(player, bag, false, true, true, true) == bag; }

    @Override
    public void clearContent() {
        stacks.clear();
        invalidate();
    }
}
