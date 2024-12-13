package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface IBagModule {
    String name();

    default boolean canInstall(BagInstance bag) { return true; }

    default void init() {}

    /**
     * called when a module is breaking (removed from bag), use this to add removal logic
     */
    default void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.uninstallModule(name(), pos);
    }

    /**
     * called when a module was installed (place inside the bag), use this to add placement logic
     */
    default void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.installModule(name(), pos, new CompoundTag());
    }

    /**
     * called when a module is right-clicked by a bag in settings/wrench mode
     * return true to cancel the right click (consume)
     */
    default boolean wrench(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack, BlockHitResult hit) { return false; }

    default boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        return false;
    }
}
