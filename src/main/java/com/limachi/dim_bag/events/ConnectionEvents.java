package com.limachi.dim_bag.events;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.other.BagVisibilities;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class ConnectionEvents {

    @SubscribeEvent
    public static void playerConnectionSync(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BagsData.sendRoomSizes(player);
            BagVisibilities.syncAllVisibilityToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void clientSideCleanupOnDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            DimBag.CLIENT_SIDE_ROOM_SIZES.clear();
            BagVisibilities.clearBagVisibilities();
        }
    }
}
