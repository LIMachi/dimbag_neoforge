package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.RegistryObject;

public class CompressionModule extends BaseModule {

    public static final String NAME = "compression";

    @RegisterBlock
    public static RegistryObject<CompressionModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public CompressionModule() { super(NAME); }
}
