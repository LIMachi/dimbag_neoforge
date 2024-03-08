package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.block_entities.bag_modules.ParasiteModuleBlockEntity;
import com.limachi.dim_bag.blocks.bag_modules.ParasiteModule;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ParasiteMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<ParasiteMenu>> R_TYPE;

    public final ParasiteModuleBlockEntity be;
    public String command;

    public static void open(Player player, BlockPos pos) {
        if (!player.level().isClientSide && player.level().getBlockEntity(pos) instanceof ParasiteModuleBlockEntity be)
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p)->new ParasiteMenu(id, inv, be, be.saveWithoutMetadata()), Component.translatable("screen.observer.title")), b->b.writeBlockPos(pos).writeNbt(be.saveWithoutMetadata()));
    }

    public ParasiteMenu(int id, Inventory playerInventory, ParasiteModuleBlockEntity be, CompoundTag bed) {
        super(R_TYPE.get(), id);
        this.be = be;
        this.command = bed.getString(ParasiteModule.NAME);
    }

    public ParasiteMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, (ParasiteModuleBlockEntity)playerInventory.player.level().getBlockEntity(buff.readBlockPos()), buff.readNbt());
    }

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
        be.replaceCommand(compoundTag);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return be != null && !be.isRemoved() && player.blockPosition().distSqr(be.getBlockPos()) <= 36; }
}
