package com.limachi.dim_bag.items.bag_modes;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.bag_modules.ParadoxModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DefaultMode extends BaseMode {
    public DefaultMode() {
        super("Default", null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (KeyMapController.SNEAK.getState(player)) {
            int bag = BagItem.getBagId(player.getItemInHand(hand));
            if (BagsData.runOnBag(bag, b->{
                if (b.getModeData(SettingsMode.NAME).getBoolean("quick_enter") && !(player.level().dimension().equals(DimBag.BAG_DIM) && b.isInRoom(player.blockPosition()))) {
                    if (!b.isModulePresent(ParadoxModule.PARADOX_KEY))
                        BagItem.unequipBags(player, b.bagId(), player.level(), player.blockPosition(), false);
                    b.enter(player, false);
                    return true;
                }
                return false;
            }, false))
                return InteractionResultHolder.success(player.getItemInHand(hand));
            if (bag > 0) {
                BagItem.unequipBags(player, bag, player.level(), player.blockPosition(), true);
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        BagMenu.open(player, BagItem.getBagId(player.getItemInHand(hand)), 0);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack bag = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        if (player instanceof ServerPlayer && bag.getItem() instanceof BagItem) {
            int id = BagItem.getBagId(bag);
            if (id > 0) {
                BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
                if (KeyMapController.SNEAK.getState(player)) {
                    if (BagsData.runOnBag(bag, b->{
                        if (b.getModeData(SettingsMode.NAME).getBoolean("quick_enter") && !(player.level().dimension().equals(DimBag.BAG_DIM) && b.isInRoom(player.blockPosition()))) {
                            if (!b.isModulePresent(ParadoxModule.PARADOX_KEY))
                                BagItem.unequipBags(player, id, player.level(), at, false);
                            b.enter(player, false);
                            return true;
                        }
                        return false;
                    }, false))
                        return InteractionResult.SUCCESS;
                    if (at.distSqr(player.blockPosition()) <= 9) {
                        BagItem.unequipBags(player, id, player.level(), at, true);
                        return InteractionResult.SUCCESS;
                    }
                }
                BagMenu.open(player, BagItem.getBagId(player.getItemInHand(ctx.getHand())), 0);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
