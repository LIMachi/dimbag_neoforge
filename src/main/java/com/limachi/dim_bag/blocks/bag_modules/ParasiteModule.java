package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.block_entities.bag_modules.ParasiteModuleBlockEntity;
import com.limachi.dim_bag.menus.ParasiteMenu;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ParasiteModule extends BaseModule implements EntityBlock {

    public static final String NAME = "parasite";

    @RegisterBlock
    public static RegistryObject<ParasiteModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public ParasiteModule() { super(NAME); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public void init() {
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_60513_, boolean p_60514_) {
        boolean on = level.getBestNeighborSignal(pos) > 0;
        if (on && !state.getValue(BlockStateProperties.POWERED)) {
            if (level.getBlockEntity(pos) instanceof ParasiteModuleBlockEntity be)
                be.run(true);
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, true), 3);
        } else if (!on && state.getValue(BlockStateProperties.POWERED)) {
            if (level.getBlockEntity(pos) instanceof ParasiteModuleBlockEntity be)
                be.run(false);
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, false), 3);
        }
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.installModule(name, pos, new CompoundTag());
        if (level.getBlockEntity(pos) instanceof ParasiteModuleBlockEntity be)
            be.install(bag, stack.getOrCreateTag().getCompound(NAME));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.uninstallModule(name, pos);
        if (level.getBlockEntity(pos) instanceof ParasiteModuleBlockEntity be)
            stack.getOrCreateTag().put(NAME, be.uninstall());
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        ParasiteMenu.open(player, pos);
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ParasiteModuleBlockEntity.R_TYPE.get().create(pos, state);
    }
}
