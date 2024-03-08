package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class BaseSingletonModule extends BaseModule {
    public BaseSingletonModule(String name) { super(name); }

    @Override
    public boolean canInstall(BagInstance bag) { return !bag.isModulePresent(name); }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.installModule(name, pos, new CompoundTag()); }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.uninstallModule(name, pos); }
}
