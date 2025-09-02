package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.RegisterItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.world.item.Item;

public class GhostBagItem extends BagItem {
    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public GhostBagItem(Properties props) {
        super(props);
    }
}
