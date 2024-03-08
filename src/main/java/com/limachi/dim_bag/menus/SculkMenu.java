package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.block_entities.bag_modules.SculkModuleBlockEntity;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class SculkMenu extends AbstractContainerMenu {

    @RegisterMenu
    public static RegistryObject<MenuType<SculkMenu>> R_TYPE;

    public final SculkModuleBlockEntity be;

    public static void open(Player player, BlockPos pos) {
        if (!player.level().isClientSide && player.level().getBlockEntity(pos) instanceof SculkModuleBlockEntity be)
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p)->new SculkMenu(id, inv, be), Component.translatable("screen.observer.title")), b->b.writeBlockPos(pos));
    }

    public SculkMenu(int id, Inventory playerInventory, SculkModuleBlockEntity be) {
        super(R_TYPE.get(), id);
        this.be = be;

        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 3; ++x)
                addSlot(new Slot(be, x + y * 3, 17 + 18 * x, 17 + 18 * y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return be.validInsert(stack);
                    }
                });

        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 3; ++x)
                addSlot(new Slot(be, 9 + x + y * 3, 107 + 18 * x, 17 + 18 * y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });

        for(int y = 0; y < 3; ++y)
            for(int x = 0; x < 9; ++x)
                addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));

        for(int x = 0; x < 9; ++x)
            addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
    }

    public SculkMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, (SculkModuleBlockEntity)playerInventory.player.level().getBlockEntity(buff.readBlockPos()));
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < be.getContainerSize()) {
                if (!moveItemStackTo(itemstack1, be.getContainerSize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 0, be.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return be.stillValid(player);
    }
}
