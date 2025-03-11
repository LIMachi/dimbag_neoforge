package com.limachi.dim_bag;

import com.limachi.dim_bag.utils.ModBase;
import com.limachi.dim_bag.utils.annotations.Mod;
import com.limachi.dim_bag.utils.annotations.RegisterTab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mod("dim_bag")
public final class DimBag extends ModBase {

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("dim_bag.tab.title"));
        builder.icon(()->new ItemStack(Items.COMPARATOR));
    }

    public DimBag() {}
}
