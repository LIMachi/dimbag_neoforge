package com.limachi.dim_bag.items.bag_modes;

import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class BaseMode implements IScrollItem {

    public final String name;
    @Nullable
    public final String requiredModule;

    public BaseMode(String name, @Nullable String requiredModule) {
        this.name = name;
        this.requiredModule = requiredModule;
    }

    public boolean canDisable() { return !name.equals(ModesRegistry.DEFAULT.name()); }

    public CompoundTag initialData(BagInstance bag) { return new CompoundTag(); }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {}

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) { return false; }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { return InteractionResultHolder.pass(player.getItemInHand(hand)); }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) { return InteractionResult.PASS; }

    public boolean onLeftClickBlock(ItemStack stack, Player player, BlockPos pos) { return false; }
    public boolean onLeftClickEmpty(ItemStack stack, Player player) { return false; }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) { return false; }

    public InteractionResult useOn(UseOnContext ctx) { return InteractionResult.PASS; }

    @Override
    public void scroll(Player player, int i, int i1) {}

    @Override
    public void scrollFeedBack(Player player, int i, int i1) {}

    @Override
    public boolean canScroll(Player player, int i) { return false; }
}
