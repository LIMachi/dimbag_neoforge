package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.RegistryObject;

public class ParadoxModule extends BaseSingletonModule {

    public static final String PARADOX_KEY = "paradox";

    @RegisterBlock
    public static RegistryObject<ParadoxModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public ParadoxModule() { super(PARADOX_KEY); }

    public static boolean isParadoxCompatible(BagInstance bag) { return bag.isModulePresent(PARADOX_KEY); }
}
