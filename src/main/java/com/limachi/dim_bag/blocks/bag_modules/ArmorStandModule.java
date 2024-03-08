package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.block_entities.bag_modules.ArmorStandModuleBlockEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ArmorStandModule extends BaseModule implements EntityBlock {

    @Configs.Config(cmt = "bags (virtual or not) are always blacklisted")
    public static String[] BLACK_LIST_INSERTION = {};

    @Configs.Config(cmt = "bags (virtual or not) are always blacklisted")
    public static String[] BLACK_LIST_EXTRACTION = {};

    public static boolean canInsert(ItemStack stack) {
        if (stack.getItem() instanceof BagItem)
            return false;
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (item != null) {
            String test = item.toString();
            return Arrays.stream(BLACK_LIST_INSERTION).noneMatch(test::matches);
        }
        return true;
    }

    public static boolean canExtract(ItemStack stack) {
        if (stack.getItem() instanceof BagItem)
            return false;
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (item != null) {
            String test = item.toString();
            return Arrays.stream(BLACK_LIST_EXTRACTION).noneMatch(test::matches);
        }
        return true;
    }

    public static final String NAME = "armor_stand";

    @RegisterBlock
    public static RegistryObject<ArmorStandModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public ArmorStandModule() { super(NAME); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ArmorStandModuleBlockEntity(pos, state);
    }
}
