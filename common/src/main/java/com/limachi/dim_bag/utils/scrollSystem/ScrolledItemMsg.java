package com.limachi.dim_bag.utils.scrollSystem;

import com.limachi.dim_bag.utils.network.IC2SMsg;
import com.limachi.dim_bag.utils.annotations.RegisterMsg;

import dev.architectury.networking.NetworkManager;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

@RegisterMsg
public record ScrolledItemMsg(int slot, int delta) implements IC2SMsg<ScrolledItemMsg> {
    @Override
    public void run(NetworkManager.PacketContext ctx) {
        Player player = ctx.getPlayer();
        Item item = player.getInventory().getItem(slot).getItem();
        if (item instanceof IScrollItem i)
            i.scroll(player, slot, delta);
    }
}