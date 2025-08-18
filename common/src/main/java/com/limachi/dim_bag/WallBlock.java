package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.RegisterBlock;
import com.limachi.lim_lib.common.annotations.RegisterBlockItem;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class WallBlock extends Block {
    @RegisterBlockItem
    public static RegistrySupplier<Item> R_ITEM;
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    public WallBlock() {
        super(Properties
                .ofFullCopy(Blocks.BEDROCK)
                .isViewBlocking((s, l, p)->false)
                .isSuffocating((s, l, p)->false));
    }
}
