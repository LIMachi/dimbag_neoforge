package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.capabilities.entities.BagMode;
import com.limachi.dim_bag.items.bag_modes.SettingsMode;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.capabilities.Cap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID, value = Dist.CLIENT)
public abstract class BaseModule extends Block implements IBagModule {

    public final String name;
    public static final Properties DEFAULT_PROPERTIES = Properties.copy(Blocks.BEDROCK).isValidSpawn((s, b, p, e)->false).isSuffocating((s, b, p)->false).noOcclusion().noLootTable().isViewBlocking((s, l, p)->false);

    public BaseModule(String name) {
        super(DEFAULT_PROPERTIES);
        this.name = name;
        init();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return pos.distManhattan(DimBag.INVALID_POS) < 1000 ? 15 : super.getLightEmission(state, level, pos);
    }

    @SubscribeEvent
    public static void addLabelTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BaseModule) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("label", Tag.TAG_STRING)) {
                Component label = Component.Serializer.fromJson(tag.getString("label"));
                if (!event.getItemStack().getHoverName().equals(label) && !event.getToolTip().contains(label))
                    event.getToolTip().add(1, label);
            }
        }
    }

    @Override
    @Nonnull
    final public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (BagsData.runOnBag(player.getItemInHand(hand), b-> SettingsMode.NAME.equals(Cap.run(player, BagMode.TOKEN, c->c.getMode(b.bagId()), "")) && wrench(b, player, level, pos, player.getItemInHand(hand), hit), false))
            return InteractionResult.SUCCESS;
        if (BagsData.runOnBag(level, pos, b->use(b, player, level, pos, hand), false))
            return InteractionResult.SUCCESS;
        return super.use(state, level, pos, player, hand, hit);
    }

    /**
     * catch manual placement of module inside bag
     */
    @Override
    public final void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack itemStack) {
        if (!(entity instanceof Player player)) return;
        BagsData.runOnBag(level, pos, b->install(b, player, level, pos, itemStack));
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

    /**
     * module might have been removed by creative break instead of attack, fix this
     */
    @Override
    public final boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (player.isCreative() && state.getBlock() instanceof BaseModule module) {
            if (level.dimension().equals(DimBag.BAG_DIM)) {
                ItemStack item = getCloneItemStack(level, pos, state);
                BagsData.runOnBag(level, pos, b->module.uninstall(b, player, level, pos, item));
                player.drop(item, false);
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
