package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.capabilities.entities.BagMode;
import com.limachi.dim_bag.items.bag_modes.SettingsMode;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class RespawnModule extends RespawnAnchorBlock implements IBagModule {

    public static final String NAME = "respawn";

    @RegisterBlock
    public static RegistryObject<RespawnModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public RespawnModule() {
        super(BaseModule.DEFAULT_PROPERTIES);
    }

    /**
     * limit placement to inside a bag only
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (!BagsData.runOnBag(ctx.getLevel(), ctx.getClickedPos(), this::canInstall, false)) return null;
        return super.getStateForPlacement(ctx);
    }

    @Override
    @Nonnull
    final public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide) return super.use(state, level, pos, player, hand, hit);
        if (BagsData.runOnBag(player.getItemInHand(hand), b-> SettingsMode.NAME.equals(Cap.run(player, BagMode.TOKEN, c->c.getMode(b.bagId()), "")) && wrench(b, player, level, pos, player.getItemInHand(hand), hit), false))
            return InteractionResult.SUCCESS;
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {}

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {}
}
