package com.limachi.dim_bag.other;

import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@StaticInit
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagVisibilities {
    private static final HashMap<ServerPlayer, Integer> PLAYER_HAS_BAG_TICKING = new HashMap<>();
    private static final HashSet<UUID> CLIENT_SIDE_PLAYERS_WITH_BAG = new HashSet<>();

    @RegisterMsg
    public record UpdateClientSideBagRenders(UUID playerId, boolean visible) implements IRecordMsg {
        @Override
        public void clientWork(Player player) {
            if (visible)
                CLIENT_SIDE_PLAYERS_WITH_BAG.add(playerId);
            else
                CLIENT_SIDE_PLAYERS_WITH_BAG.remove(playerId);
        }
    }

    public static boolean shouldShowBag(Player player) {
        return CLIENT_SIDE_PLAYERS_WITH_BAG.contains(player.getUUID());
    }

    public static void clearBagVisibilities() {
        CLIENT_SIDE_PLAYERS_WITH_BAG.clear();
    }

    public static void syncAllVisibilityToPlayer(ServerPlayer player) {
        for (Map.Entry<ServerPlayer, Integer> entry : PLAYER_HAS_BAG_TICKING.entrySet()) {
            if (entry.getValue() > 0)
                NetworkManager.toClient(player, new UpdateClientSideBagRenders(entry.getKey().getUUID(), true));
        }
    }

    public static void bagTick(ServerPlayer player) {
        Integer prev = PLAYER_HAS_BAG_TICKING.put(player, 10);
        if (prev == null || prev <= 0)
            NetworkManager.toClients(new UpdateClientSideBagRenders(player.getUUID(), true));
    }

    @SubscribeEvent
    public static void playerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PLAYER_HAS_BAG_TICKING.compute(player, (k, v)->{
                if (v == null)
                    v = 0;
                if (v > 0) {
                    --v;
                    if (v == 0)
                        NetworkManager.toClients(new UpdateClientSideBagRenders(player.getUUID(), false));
                }
                return v;
            });
        }
    }
}
