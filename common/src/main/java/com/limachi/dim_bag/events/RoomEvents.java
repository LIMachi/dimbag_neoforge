package com.limachi.dim_bag.events;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.utils.Events;
import com.limachi.dim_bag.utils.annotations.RegisterEventListener;

import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RoomEvents {
    @RegisterEventListener(Events.BLOCK_BREAK)
    public static EventResult wallsCannotBeBroken(Level level, BlockPos pos, BlockState state, ServerPlayer player, IntValue xp) {
        if (player != null && !player.isCreative() && DimBag.isWall(level, pos))
            return EventResult.interruptFalse();
        return EventResult.pass();
    }

    @RegisterEventListener(Events.LEFT_CLICK_BLOCK)
    public static EventResult wallsCannotBeMined(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (player != null && !player.isCreative() && DimBag.isWall(player.level(), pos))
            return EventResult.interruptFalse();
        return EventResult.pass();
    }

    @RegisterEventListener(Events.EXPLOSION_DETONATE)
    public static void wallsCannotBeExploded(Level level, Explosion explosion, List<Entity> affectedEntities) {
        explosion.getToBlow().removeIf(p->DimBag.isWall(level, p));
    }
}
