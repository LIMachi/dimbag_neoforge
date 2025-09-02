package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.RegisterItem;
import com.mojang.datafixers.util.Pair;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.ArrayList;
import java.util.List;

public class WallPusherItem extends Item {
    public static final int MAX_DAMAGE = 256;

    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public WallPusherItem(Properties properties) { super(properties.durability(MAX_DAMAGE)); }

    public static Pair<Integer, List<ItemStack>> getUsableDamage(Player player, InteractionHand hand) {
        ArrayList<ItemStack> out = new ArrayList<>();
        int count = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof WallPusherItem) {
                count += MAX_DAMAGE - stack.getDamageValue();
                out.add(stack);
            }
        }
        out.remove(player.getItemInHand(hand));
        out.add(player.getItemInHand(hand)); //make sure the hand item is the last in the list
        return new Pair<>(count, out);
    }

    public static void applyConsumption(int consume, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            int uses = MAX_DAMAGE - stack.getDamageValue();
            if (uses > consume)
                stack.setDamageValue(MAX_DAMAGE - (uses - consume));
            else {
                stack.setDamageValue(MAX_DAMAGE);
                stack.setCount(0);
            }
            consume -= uses;
            if (consume <= 0)
                break;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        int room = Rooms.isWallOfRoom(useOnContext.getLevel(), useOnContext.getClickedPos());
        if (room >= 0) {
            Direction dir = useOnContext.getClickedFace().getOpposite();
            if (useOnContext.getPlayer() instanceof Player player) {
                if (Rooms.canPushWall(room, dir)) {
                    if (!player.isCreative()) {
                        int required = Rooms.requiredToPushWall(room, dir);
                        var usable = getUsableDamage(player, useOnContext.getHand());
                        if (usable.getFirst() >= required) {
                            applyConsumption(required, usable.getSecond());
                            Rooms.pushWall(room, dir);
                        } else
                            player.displayClientMessage(Component.translatable("item.dim_bag.wall_pusher_item.missing_charges.message", required, usable.getFirst()), true);
                    } else
                        Rooms.pushWall(room, dir);
                } else
                    player.displayClientMessage(Component.translatable("item.dim_bag.wall_pusher_item.max_size_reached.message"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
