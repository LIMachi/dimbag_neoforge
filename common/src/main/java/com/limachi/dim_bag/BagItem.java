package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.RegisterItem;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

public class BagItem extends Item {
    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public BagItem(Properties props) {
        super(props.stacksTo(1).fireResistant());
    }
}
