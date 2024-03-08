package com.limachi.dim_bag.blocks;

import com.limachi.dim_bag.block_entities.ProxyBlockEntity;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProxyBlock extends Block implements EntityBlock {

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<Item> R_ITEM;

    public ProxyBlock() { super(Properties.copy(Blocks.IRON_BLOCK)); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProxyBlockEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
        return super.getDrops(p_287732_, p_287596_);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof ProxyBlockEntity proxy) {
            if (proxy.use(player, player.getItemInHand(hand), hit.getDirection())) {
                ItemStack hold = player.getItemInHand(hand);
                hold.shrink(1);
                player.setItemInHand(hand, hold);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof ProxyBlockEntity proxy) {
            proxy.drop(player.getDirection().getOpposite());
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public void onRemove(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean p_60519_) {
        if (!newState.is(oldState.getBlock()) && level.getBlockEntity(pos) instanceof ProxyBlockEntity proxy) {
            proxy.drop(null);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(newState, level, pos, oldState, p_60519_);
    }
}
