package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.CmdArg;
import com.limachi.lim_lib.common.annotations.RegisterCommand;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Commands {
    @RegisterCommand(value = "dim_bag tests new_room <size>", OPLevel = 2)
    public static int setRoomSize(CommandContext<CommandSourceStack> ctx, @CmdArg("size") Integer size) {
        int id = Rooms.buildRoom(size);
        ctx.getSource().sendSuccess(()-> Component.literal("Successfully create room " + id + " with size " + size), true);
        return 1;
    }

    @RegisterCommand(value = "dim_bag tests push_wall", OPLevel = 2)
    public static int pushWall(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().location().equals(DimBag.BAG_DIM.location())) {
                int id = Rooms.closestRoomId(player.blockPosition());
                Direction direction = player.getNearestViewDirection();
                Rooms.pushWall(id, direction);
            }
        }
        return 1;
    }

    @RegisterCommand(value = "dim_bag tests required_to_push_wall")
    public static int pushWallQuery(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension().location().equals(DimBag.BAG_DIM.location())) {
                int id = Rooms.closestRoomId(player.blockPosition());
                Direction direction = player.getNearestViewDirection();
                int req = Rooms.requiredToPushWall(id, direction);
                ctx.getSource().sendSuccess(()-> Component.literal("required charges " + req), true);
            }
        }
        return 1;
    }

    @RegisterCommand(value = "dim_bag tests enter <id>", OPLevel = 2, requirePlayer = true)
    public static int enterRoom(CommandContext<CommandSourceStack> ctx, @CmdArg("id") Integer id) {
        Rooms.enter(id, ctx.getSource().getEntity(), true);
        return 1;
    }

    @RegisterCommand(value = "dim_bag tests leave <id>", OPLevel = 2, requirePlayer = true)
    public static int leaveRoom(CommandContext<CommandSourceStack> ctx, @CmdArg("id") Integer id) {
        Rooms.leave(id, ctx.getSource().getEntity());
        return 1;
    }

    @RegisterCommand(value = "dim_bag tests leave", OPLevel = 2, requirePlayer = true)
    public static int leaveRoom(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() != null && ctx.getSource().getLevel().dimension().location().equals(DimBag.BAG_DIM.location()))
            Rooms.leave(Rooms.closestRoomId(ctx.getSource().getEntity().blockPosition()), ctx.getSource().getEntity());
        return 1;
    }
}
