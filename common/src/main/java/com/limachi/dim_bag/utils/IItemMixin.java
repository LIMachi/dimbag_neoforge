package com.limachi.dim_bag.utils;

import net.minecraft.world.item.ItemStack;

public interface IItemMixin {
    default boolean shouldCauseReequipAnimation(ItemStack newStack, ItemStack oldStack, int slot) {
        return newStack != oldStack;
    }
}
