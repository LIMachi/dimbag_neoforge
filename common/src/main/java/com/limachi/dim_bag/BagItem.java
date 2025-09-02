package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.RegisterItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class BagItem extends Item {
    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public static final ItemLore ID_NOT_SET = new ItemLore(List.of(Component.translatable("item.lore.dim_bag.id_not_set")));

    public static ItemLore roomIdLore(int id) {
        return new ItemLore(List.of(Component.translatable("item.lore.dim_bag.room_id_lore", id)));
    }

    public static boolean isRealBag(ItemStack stack) { return stack.is(R_ITEM); }

    public BagItem(Properties props) { super(props
            .stacksTo(1)
            .fireResistant()
            .component(ItemComponents.ROOM_ID.get(), -1)
            .component(DataComponents.LORE, ID_NOT_SET)
    ); }

    public static ItemStack withRoomId(int roomId) {
        ItemStack out = new ItemStack(R_ITEM);
        out.set(ItemComponents.ROOM_ID.get(), roomId);
        out.set(DataComponents.LORE, roomIdLore(roomId));
        return out;
    }

    public static int getRoomId(ItemStack bag) {
        if (bag.has(ItemComponents.ROOM_ID.get()))
            return bag.get(ItemComponents.ROOM_ID.get());
        return -1;
    }

    public static void setRoomId(ItemStack bag, int roomId) {

    }

    //behavior:
    //configurable in bag (you can set what the bag does on (shift) left/right click air/block/fluid/entity)
    //note that action to open the bag menu MUST be bound to validate the setting (by default, any simple right click)
    //this gives 4 main actions with up to 4 variations for each (right click, shift right click, left click, shift left click for air/block/fluid/entity)
    //other key behaviors: bag can be used in inventory by using the virtual bag key on hover
    //note: there is also 4 actions for the bag entity itself (right click, shift right click, left click, shift left click for air/block/fluid/entity)
    //same as for the bag item, at least one of the 4 actions MUST be bound to the menu, the other 3 can be used to enter the dimension, equip the bag or else

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        return super.useOn(useOnContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide) {
            if (itemStack.get(ItemComponents.ROOM_ID.get()) instanceof Integer room) {
                if (room < 0) {
                    //invalid/default id, generate new room first
                    room = Rooms.buildRoom();
                    itemStack.set(ItemComponents.ROOM_ID.get(), room);
                    itemStack.set(DataComponents.LORE, roomIdLore(room));
                }
                int in = Rooms.exactRoom(level, player.blockPosition());
                if (in >= 0 && in == room)
                    Rooms.leave(room, player); //bag used inside of itself, leaving
                else
                    Rooms.enter(room, player, true); //store for now, final iner working will be to use store only in paradox mode
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }
}
