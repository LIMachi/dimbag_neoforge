package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.block_entities.bag_modules.ObserverModuleBlockEntity;
import com.limachi.dim_bag.menus.ObserverMenu;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ObserverModule extends BaseModule implements EntityBlock {

    public static final String COMMAND_KEY = "command";

    @RegisterBlock
    public static RegistryObject<ObserverModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public ObserverModule() { super(COMMAND_KEY); }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWER);
    }

    @Override
    public void init() { registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWER, 0)); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ObserverModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof ObserverModuleBlockEntity be)
            be.install(bag, stack.getOrCreateTag().getCompound(COMMAND_KEY));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof ObserverModuleBlockEntity be)
            stack.getOrCreateTag().put(COMMAND_KEY, be.uninstall());
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        ObserverMenu.open(player, pos);
        return true;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level unusedLevel, @Nonnull BlockState unusedState, @Nonnull BlockEntityType<T> unusedType) {
        return (level, pos, state, be) -> {
            if (be instanceof ObserverModuleBlockEntity o)
                o.tick(state);
        };
    }

    @Override
    public int getSignal(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Direction dir) {
        return state.getValue(BlockStateProperties.POWER);
    }

    @Override
    public boolean isSignalSource(BlockState state) { return state.getBlock() instanceof ObserverModule; }
}
