package com.limachi.dim_bag.menus.slots;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.SlotData;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.menus.slots.BigSlotIH;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public class BagSlot extends BigSlotIH {
    public int bag;
    public BlockPos slot;
    protected Function<BagSlot, Boolean> isActive;
    LazyOptional<SlotData.SlotEntry> slotAccess = null;
    private Optional<Integer> client_color = Optional.empty(); //client side only
    private ItemStack client_filter = ItemStack.EMPTY; //client side only
    private int client_max_stacks = 1; //client side only
    private final Player listener; //server side only
    private boolean sync_on_first_get = true; //server side only

    public BagSlot(int bag, Player listener, BlockPos slot, int xPosition, int yPosition, Function<BagSlot, Boolean> isActive) {
        super((IItemHandlerModifiable)EmptyHandler.INSTANCE, 0, xPosition, yPosition);
        this.listener = listener;
        this.bag = bag;
        this.slot = slot;
        this.isActive = isActive;
        BagsData.runOnBag(bag, b->slotAccess = b.slotHandle(slot));
    }

    public BagSlot(int xPosition, int yPosition, Function<BagSlot, Boolean> isActive) {
        super(new InvWrapper(new SimpleContainer(1) {
            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE; //FIXME: should be set from remote
            }
        }), 0, xPosition, yPosition);
        this.listener = null;
        this.bag = 0;
        this.slot = null;
        this.isActive = isActive;
    }

    @Override
    public IItemHandler getItemHandler() {
        if (bag > 0 && (slotAccess == null || !slotAccess.isPresent()))
            BagsData.runOnBag(bag, b->slotAccess = b.slotHandle(slot));
        if (slotAccess != null)
            return slotAccess.orElse(SlotData.SlotEntry.EMPTY);
        return bag == 0 ? super.getItemHandler() : SlotData.SlotEntry.EMPTY;
    }

    @Override
    public boolean isActive() { return isActive.apply(this); }

    public void changeSlotServerSide(BlockPos slot) {
        this.slot = slot;
        slotAccess = null;
    }

    //color overlay
    public Optional<Integer> color() {
        return isActive.apply(this) ? Sides.isLogicalClient() ? client_color : slotAccess.orElse(SlotData.SlotEntry.EMPTY).getColor() : Optional.empty();
    }

    //render transparent ghost item in bag interface
    public ItemStack getFilter() {
        return isActive.apply(this) ? Sides.isLogicalClient() ? client_filter : slotAccess.orElse(SlotData.SlotEntry.EMPTY).getFilter() : ItemStack.EMPTY;
    }

    @Override
    public boolean isHighlightable() { return isActive(); }

    protected boolean validStack(ItemStack stack) {
        return !(stack.getItem() instanceof BagItem) || BagItem.getBagId(stack) != bag;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (!isActive() || stack.isEmpty() || !validStack(stack))
            return false;
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(@Nonnull Player playerIn) {
        return isActive() && validStack(super.getItem()) && super.mayPickup(playerIn);
    }

    @Override
    public int maxSizeInStacks() {
        return isActive() ? Sides.isLogicalClient() ? client_max_stacks : super.maxSizeInStacks() : 0;
    }

    @Override
    public @Nonnull ItemStack remove(int amount) {
        return isActive() && validStack(super.getItem()) ? super.remove(amount) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getItem() {
        ItemStack out = super.getItem();
        if (sync_on_first_get) {
            sync_on_first_get = false;
            setChanged();
        }
        return out;
    }

    @RegisterMsg
    public record BagSlotInfoMsg(int slotIndex, int color, ItemStack filter, int stacks) implements IRecordMsg {
        @Override
        public void clientWork(Player player) {
            if (slotIndex < player.containerMenu.slots.size() && player.containerMenu.slots.get(slotIndex) instanceof BagSlot slot) {
                slot.client_max_stacks = stacks;
                slot.client_filter = filter;
                slot.client_color = color == 0 ? Optional.empty() : Optional.of(color);
            }
        }
    }

    @Override
    public void setChanged() {
        if (listener instanceof ServerPlayer player) {
            int index = player.containerMenu.slots.indexOf(this);
            if (index != -1)
                NetworkManager.toClient(player, new BagSlotInfoMsg(index, color().orElse(0), getFilter(), maxSizeInStacks()));
        }
        super.setChanged();
    }
}
