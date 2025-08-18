package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.Config;
import com.limachi.lim_lib.common.annotations.RegisterData;
import com.limachi.lim_lib.common.codec.CodecUtils;
import com.limachi.lim_lib.common.codec.Codecs;
import com.limachi.lim_lib.common.dataStorage.DataField;
import com.limachi.lim_lib.common.utils.Game;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public class Rooms {
    @Config(path = "rooms", min = "1024", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS! (the default value of 2048 is enough for ~15 bags per player for ~1000 players)", reload = false)
    public static int ROOM_SPACING = 2048;

    @Config(path = "rooms", min = "1", max = "126", cmt = "Maximum inner radius of a room (not counting walls or center block, a radius of 63 will be an inner room of 127, and a total size of 129 blocks with walls)")
    public static int MAX_ROOM_RADIUS = 63;

    @Config(path = "rooms", min = "1", max = "126", cmt = "Starting/minimum inner radius of a room (will be the size of rooms on first interaction with a bag)")
    public static int STARTING_ROOM_RADIUS = 2;

    @RegisterData(file = "dim_bag_rooms", syncToClient = true)
    public static DataField<ArrayList<Pair<BlockPos, BlockPos>>> ROOM_BOUNDARIES = new DataField<>(new ArrayList<>(), CodecUtils.collectionCodec(ArrayList::new, CodecUtils.pairCodec(Codecs.POS, Codecs.POS)));

    @RegisterData(file = "dim_bag_positions")
    public static DataField<HashMap<UUID, HashMap<Integer, Pair<BlockPos, Level>>>> ENTERED_FROM = new DataField<>(new HashMap<>(), CodecUtils.mapCodec(HashMap::new, Codecs.UUID, CodecUtils.mapCodec(HashMap::new, Codecs.INT, CodecUtils.pairCodec(Codecs.POS, Codecs.LEVEL))));

    @RegisterData(file = "dim_bag_positions")
    public static DataField<ArrayList<Pair<BlockPos, Level>>> BAGS = new DataField<>(new ArrayList<>(), CodecUtils.collectionCodec(ArrayList::new, CodecUtils.pairCodec(Codecs.POS, Codecs.LEVEL)));

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + id * ROOM_SPACING, 128, 8); }

    public static int closestRoomId(BlockPos pos) { return Mth.clamp((pos.getX() - 8 + ROOM_SPACING / 2) / ROOM_SPACING, 0, 128); }

    public static int buildRoom() { return buildRoom(STARTING_ROOM_RADIUS); }

    public static int buildRoom(int size) {
        var boundaries = ROOM_BOUNDARIES.get();
        if (boundaries != null) {
            int id = boundaries.size();
            if (Game.getLevel(DimBag.BAG_DIM.location()) instanceof ServerLevel sl) {
                var block = WallBlock.R_BLOCK.get().defaultBlockState();
                BlockPos center = roomCenter(id);
                for (int i = -size + 1; i <= size - 1; ++i)
                    for (int j = -size + 1; j <= size - 1; ++j) {
                        sl.setBlock(center.offset(i, j, size), block, 3);
                        sl.setBlock(center.offset(i, j, -size), block, 3);
                        sl.setBlock(center.offset(i, size, j), block, 3);
                        sl.setBlock(center.offset(i, -size, j), block, 3);
                        sl.setBlock(center.offset(size, i, j), block, 3);
                        sl.setBlock(center.offset(-size, i, j), block, 3);
                    }
                BlockPos low = center.offset(-size, -size, -size);
                BlockPos high = center.offset(size, size, size);
                boundaries.add(new Pair<>(low, high));
                ROOM_BOUNDARIES.setDirty();
                return id;
            }
        }
        return -1;
    }

    public static boolean canPushWall(int id, Direction direction) {
        if (id < 0)
            return false;
        var boundaries = ROOM_BOUNDARIES.get();
        if (boundaries != null && id < boundaries.size()) {
            var room = boundaries.get(id);
            BlockPos center = roomCenter(id);
            return switch (direction) {
                case WEST -> center.getX() - room.getFirst().getX() < MAX_ROOM_RADIUS;
                case EAST -> room.getSecond().getX() - center.getX() < MAX_ROOM_RADIUS;
                case DOWN -> center.getY() - room.getFirst().getY() < MAX_ROOM_RADIUS;
                case UP -> room.getSecond().getY() - center.getY() < MAX_ROOM_RADIUS;
                case NORTH -> center.getZ() - room.getFirst().getZ() < MAX_ROOM_RADIUS;
                case SOUTH -> room.getSecond().getZ() - center.getZ() < MAX_ROOM_RADIUS;
            };
        }
        return false;
    }

    /**
     * calculate the amount (of walls to add) that would be required to push a wall
     * @return -1 if wall can't be pushed no matter what (wrong id, maximum size reached, etc...)
     */
    public static int requiredToPushWall(int id, Direction direction) {
        if (id < 0)
            return -1;
        var boundaries = ROOM_BOUNDARIES.get();
        if (boundaries != null && id < boundaries.size()) {
            var room = boundaries.get(id);
            return switch (direction.getAxis()) {
                case X -> ((room.getSecond().getY() - room.getFirst().getY() - 1) + (room.getSecond().getZ() - room.getFirst().getZ() - 1)) * 2;
                case Y -> ((room.getSecond().getX() - room.getFirst().getX() - 1) + (room.getSecond().getZ() - room.getFirst().getZ() - 1)) * 2;
                case Z -> ((room.getSecond().getX() - room.getFirst().getX() - 1) + (room.getSecond().getY() - room.getFirst().getY() - 1)) * 2;
            };
        }
        return -1;
    }

    public static boolean enter(int id, Entity entity, boolean store) {
        if (id >= 0 && Game.getLevel(DimBag.BAG_DIM.location()) instanceof ServerLevel sl) {
            if (store && sl.equals(entity.level()) && closestRoomId(entity.blockPosition()) == id) //trying to enter the bag from inside, do not store the position (potential loop when trying to leave)
                store = false;
            if (store) {
                var enter = ENTERED_FROM.get();
                if (enter != null) {
                    if (!enter.containsKey(entity.getUUID()))
                        enter.put(entity.getUUID(), new HashMap<>());
                    var re = enter.get(entity.getUUID());
                    re.put(id, new Pair<>(entity.blockPosition(), entity.level()));
                    ENTERED_FROM.setDirty();
                }
            }
            BlockPos center = roomCenter(id);
            return entity.teleportTo(sl, center.getX(), center.getY(), center.getZ(), Set.of(), entity.getYRot(), entity.getXRot());
        }
        return false;
    }

    public static boolean leave(int id, Entity entity) {
        if (id < 0)
            return false;
        var enter = ENTERED_FROM.get();
        if (enter != null) {
            var re = enter.get(entity.getUUID());
            if (re != null) {
                var p = re.remove(id);
                if (p != null && p.getSecond() instanceof ServerLevel sl)
                    return entity.teleportTo(sl, p.getFirst().getX(), p.getFirst().getY(), p.getFirst().getZ(), Set.of(), entity.getYRot(), entity.getXRot());
            }
        }
        var bags = BAGS.get();
        if (bags != null && id < bags.size()) {
            var b = bags.get(id);
            if (b != null && b.getSecond() instanceof ServerLevel sl)
                return entity.teleportTo(sl, b.getFirst().getX(), b.getFirst().getY(), b.getFirst().getZ(), Set.of(), entity.getYRot(), entity.getXRot());
        }
        return false;
    }

    public static void pushWall(int id, Direction direction) {
        if (id < 0)
            return;
        var boundaries = ROOM_BOUNDARIES.get();
        if (boundaries != null && id < boundaries.size()) {
            if (Game.getLevel(DimBag.BAG_DIM.location()) instanceof ServerLevel sl) {
                BlockState air = Blocks.AIR.defaultBlockState();
                BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
                var room = boundaries.get(id);
                int iStart = 0, iEnd = 0, jStart = 0, jEnd = 0;
                BiFunction<Integer, Integer, BlockPos> pos = (i, j)->new BlockPos(0, 0, 0);
                switch (direction) {
                    case NORTH -> {
                        //-z
                        iStart = room.getFirst().getX();
                        iEnd = room.getSecond().getX();
                        jStart = room.getFirst().getY();
                        jEnd = room.getSecond().getY();
                        pos = (i, j)->new BlockPos(i, j, room.getFirst().getZ());
                    }
                    case SOUTH -> {
                        //+z
                        iStart = room.getFirst().getX();
                        iEnd = room.getSecond().getX();
                        jStart = room.getFirst().getY();
                        jEnd = room.getSecond().getY();
                        pos = (i, j)->new BlockPos(i, j, room.getSecond().getZ());
                    }
                    case EAST -> {
                        //+x
                        iStart = room.getFirst().getY();
                        iEnd = room.getSecond().getY();
                        jStart = room.getFirst().getZ();
                        jEnd = room.getSecond().getZ();
                        pos = (i, j)->new BlockPos(room.getSecond().getX(), i, j);
                    }
                    case WEST -> {
                        //-x
                        iStart = room.getFirst().getY();
                        iEnd = room.getSecond().getY();
                        jStart = room.getFirst().getZ();
                        jEnd = room.getSecond().getZ();
                        pos = (i, j)->new BlockPos(room.getFirst().getX(), i, j);
                    }
                    case UP -> {
                        //+y
                        iStart = room.getFirst().getX();
                        iEnd = room.getSecond().getX();
                        jStart = room.getFirst().getZ();
                        jEnd = room.getSecond().getZ();
                        pos = (i, j)->new BlockPos(i, room.getSecond().getY(), j);
                    }
                    case DOWN -> {
                        //-y
                        iStart = room.getFirst().getX();
                        iEnd = room.getSecond().getX();
                        jStart = room.getFirst().getZ();
                        jEnd = room.getSecond().getZ();
                        pos = (i, j)->new BlockPos(i, room.getFirst().getY(), j);
                    }
                }
                for (int i = iStart; i <= iEnd; ++i)
                    for (int j = jStart; j <= jEnd; ++j) {
                        BlockPos p = pos.apply(i, j);
                        if ((i != iStart && i != iEnd) && (j != jStart && j != jEnd)) {
                            BlockState prev = sl.getBlockState(p);
                            sl.setBlock(p, air, 3);
                            sl.setBlock(p.offset(direction.getNormal()), prev, 3);
                        } else if ((i == iStart || i == iEnd) && (j == jStart || j == jEnd))
                            sl.setBlock(p, air, 3);
                        else
                            sl.setBlock(p, wall, 3);
                    }
                switch (direction) {
                    case NORTH -> boundaries.set(id, room.mapFirst(BlockPos::north));
                    case SOUTH -> boundaries.set(id, room.mapSecond(BlockPos::south));
                    case EAST -> boundaries.set(id, room.mapSecond(BlockPos::east));
                    case WEST -> boundaries.set(id, room.mapFirst(BlockPos::west));
                    case UP -> boundaries.set(id, room.mapSecond(BlockPos::above));
                    case DOWN -> boundaries.set(id, room.mapFirst(BlockPos::below));
                }
                ROOM_BOUNDARIES.setDirty();
            }
        }
    }
}
