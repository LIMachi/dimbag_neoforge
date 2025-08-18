package com.limachi.dim_bag;

import com.limachi.lim_lib.InstancedMod;
import com.limachi.lim_lib.common.annotations.*;

import com.mojang.serialization.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.*;

public final class DimBag {
    @ModInstance
    public static InstancedMod MOD;

    public static final ResourceKey<Level> BAG_DIM = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dim_bag", "bag"));

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("dim_bag.tab.title"));
        builder.icon(()->new ItemStack(BagItem.R_ITEM));
    }
}
