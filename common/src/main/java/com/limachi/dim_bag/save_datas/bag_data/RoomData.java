package com.limachi.dim_bag.save_datas.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.blocks.bag_modules.IBagModule;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.utils.Tags;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RoomData {
    private BlockPos minWalls;
    private BlockPos maxWalls;
    private ServerLevel level;
    private final BagInstance bag;

    public BlockPos minWalls() { return minWalls; }

    public BlockPos maxWalls() { return maxWalls; }

    @Configs.Config(cmt = "default modules, use resource style notation of module blocks ex: \"dim_bag:compression_module\", if you want multiples of the same module, prepend <value>*, ex: \"9*dim_bag:slot_module\"")
    public static String[] DEFAULT_MODULES = { "9*dim_bag:slot_module" };

    public RoomData(ServerLevel level, BagInstance bag, CompoundTag rawData) {
        this.bag = bag;
        this.level = level;
        if (!rawData.contains("room"))
            buildRoom(rawData);
        else {
            CompoundTag room = rawData.getCompound("room");
            if (room.contains("min_walls", Tag.TAG_LONG) && room.contains("max_walls", Tag.TAG_LONG)) {
                minWalls = BlockPos.of(room.getLong("min_walls"));
                maxWalls = BlockPos.of(room.getLong("max_walls"));
            } else
                buildRoom(rawData);
        }
    }

    public Pair<BlockPos, BlockPos> getWalls() { return new Pair<>(minWalls, maxWalls); }

    public ServerLevel bagLevel() {
        if (level == null && World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel tl)
            level = tl;
        return level;
    }

    public BlockPos getAnyInstallPosition() {
        BlockPos offMinWalls = minWalls.offset(1, 1, 1);
        BlockPos offMaxWalls = maxWalls.offset(-1, -1, -1);
        for (int y = offMinWalls.getY(); y <= offMaxWalls.getY(); ++y)
            for (int x = offMinWalls.getX(); x <= offMaxWalls.getX(); ++x)
                for (int z = offMinWalls.getZ(); z <= offMaxWalls.getZ(); ++z) {
                    BlockPos test = new BlockPos(x, y, z);
                    if (isWall(test, offMinWalls, offMaxWalls) && level.getBlockState(test).is(Blocks.AIR))
                        return test;
                }
        return null;
    }

    public void loadRoom(boolean add) {
        BlockPos chunkLoader = DimBag.roomCenter(bag.bagId()).atY(256);
        ChunkPos min = new ChunkPos(minWalls);
        ChunkPos max = new ChunkPos(maxWalls);
        for (int x = min.x; x <= max.x; ++x)
            for (int z = min.z; z <= max.z; ++z)
                ForgeChunkManager.forceChunk(level, DimBag.MOD_ID, chunkLoader, x, z, add, true);
    }

    protected void buildRoom(CompoundTag rawData) {
        CompoundTag room = Tags.getOrCreateCompound(rawData, "room", CompoundTag::new);
        BlockPos center = DimBag.roomCenter(bag.bagId());
        minWalls = center.offset(-BagsData.DEFAULT_ROOM_RADIUS, -BagsData.DEFAULT_ROOM_RADIUS, -BagsData.DEFAULT_ROOM_RADIUS);
        maxWalls = center.offset(BagsData.DEFAULT_ROOM_RADIUS, BagsData.DEFAULT_ROOM_RADIUS, BagsData.DEFAULT_ROOM_RADIUS);
        BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
        if (bagLevel() == null)
            return;
        loadRoom(true);
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
                BlockPos topPos = new BlockPos(x, maxWalls.getY(), z);
                BlockPos downPos = new BlockPos(x, minWalls.getY(), z);
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(x, y, maxWalls.getZ());
                BlockPos downPos = new BlockPos(x, y, minWalls.getZ());
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
            for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(maxWalls.getX(), y, z);
                BlockPos downPos = new BlockPos(minWalls.getX(), y, z);
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
        room.putLong("min_walls", minWalls.asLong());
        room.putLong("max_walls", maxWalls.asLong());
        NetworkManager.toClients(new DimBag.ClientSideRoomSizeUpdate(bag.bagId(), minWalls, maxWalls));
        for (String cm : DEFAULT_MODULES) {
            String[] tm = cm.split("[*]");
            int qty = 1;
            if (tm.length == 2) {
                try {
                    qty = Integer.parseInt(tm[0]);
                } catch (NumberFormatException e) {
                    Log.error(e);
                }
                cm = tm[1];
            }
            if (qty > 0) {
                String block = cm;
                tm = cm.split(":");
                String mod = "minecraft";
                if (tm.length == 2) {
                    mod = tm[0];
                    block = tm[1];
                }
                RegistryObject<Block> rb = Registries.getRegistryObject(mod, Block.class, block);
                if (rb != null && rb.get() instanceof IBagModule module) {
                    for (int i = 0; i < qty; ++i) {
                        if (!module.canInstall(bag))
                            break;
                        BlockPos pos = getAnyInstallPosition();
                        if (pos != null) {
                            level.setBlockAndUpdate(pos, rb.get().defaultBlockState());
                            module.install(bag, null, level, pos, ItemStack.EMPTY);
                        } else
                            break;
                    }
                }
            }
        }
    }

    public List<ChunkPos> allChunkPos() {
        ArrayList<ChunkPos> out = new ArrayList<>();
        ChunkPos min = new ChunkPos(minWalls);
        ChunkPos max = new ChunkPos(maxWalls);
        for (int z = min.z; z <= max.z; ++z)
            for (int x = min.x; x <= max.x; ++x)
                out.add(new ChunkPos(x, z));
        return out;
    }

    public Stream<LevelChunk> iterChunks() {
        if (bagLevel() == null)
            return Stream.empty();
        return allChunkPos().stream().map(p->level.getChunk(p.x, p.z));
    }

    public Stream<BlockEntity> iterBlockEntities() {
        return iterChunks().flatMap(c->c.getBlockEntities().entrySet().stream().filter(e-> isInside(e.getKey())).map(Map.Entry::getValue));
    }

    public List<MobEffectInstance> beaconEffects(BeaconBlockEntity be) {
        ArrayList<MobEffectInstance> out = new ArrayList<>();
        if (be.primaryPower != null) {
            int power = 0;
            if (be.primaryPower == be.secondaryPower)
                power = 1;
            out.add(new MobEffectInstance(be.primaryPower, 260, power, true, true));
            if (be.levels >= 4 && be.primaryPower != be.secondaryPower && be.secondaryPower != null)
                out.add(new MobEffectInstance(be.secondaryPower, 260, 0, true, true));
        }
        return out;
    }

    protected static boolean inRange(int v, int min, int max) { return v >= min && v <= max; }

    public static boolean isWall(BlockPos pos, BlockPos minWalls, BlockPos maxWalls) {
        return (((pos.getX() == minWalls.getX() || pos.getX() == maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getY() == minWalls.getY() || pos.getY() == maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getZ() == minWalls.getZ() || pos.getZ() == maxWalls.getZ()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX())));
    }

    public boolean isWall(BlockPos pos) { return isWall(pos, minWalls, maxWalls); }

    public static boolean isInside(BlockPos pos, BlockPos minWalls, BlockPos maxWalls) {
        return inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ());
    }

    public boolean isInside(BlockPos pos) { return isInside(pos, minWalls, maxWalls); }

    private Direction wallDirection(BlockPos wallPos) {
        if (wallPos.getX() == minWalls.getX()) return Direction.WEST;
        if (wallPos.getX() == maxWalls.getX()) return Direction.EAST;
        if (wallPos.getY() == minWalls.getY()) return Direction.DOWN;
        if (wallPos.getY() == maxWalls.getY()) return Direction.UP;
        if (wallPos.getZ() == minWalls.getZ()) return Direction.NORTH;
        if (wallPos.getZ() == maxWalls.getZ()) return Direction.SOUTH;
        return null;
    }

    private void iterateWall(Direction wall, int offset, Consumer<BlockPos> run) {
        BlockPos start = switch (wall) {
            case UP -> new BlockPos(minWalls.getX() - offset, maxWalls.getY(), minWalls.getZ() - offset);
            case DOWN -> new BlockPos(minWalls.getX() - offset, minWalls.getY(), minWalls.getZ() - offset);
            case NORTH -> new BlockPos(minWalls.getX() - offset, minWalls.getY() - offset, minWalls.getZ());
            case SOUTH -> new BlockPos(minWalls.getX() - offset, minWalls.getY() - offset, maxWalls.getZ());
            case EAST -> new BlockPos(maxWalls.getX(), minWalls.getY() - offset, minWalls.getZ() - offset);
            case WEST -> new BlockPos(minWalls.getX(), minWalls.getY() - offset, minWalls.getZ() - offset);
        };
        BlockPos end = switch (wall) {
            case UP -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY(), maxWalls.getZ() + offset);
            case DOWN -> new BlockPos(maxWalls.getX() + offset, minWalls.getY(), maxWalls.getZ() + offset);
            case NORTH -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY() + offset, minWalls.getZ());
            case SOUTH -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY() + offset, maxWalls.getZ());
            case EAST -> new BlockPos(maxWalls.getX(), maxWalls.getY() + offset, maxWalls.getZ() + offset);
            case WEST -> new BlockPos(minWalls.getX(), maxWalls.getY() + offset, maxWalls.getZ() + offset);
        };
        for (int x = start.getX(); x <= end.getX(); ++x)
            for (int y = start.getY(); y <= end.getY(); ++y)
                for (int z = start.getZ(); z <= end.getZ(); ++z)
                    run.accept(new BlockPos(x, y, z));
    }

    public boolean pushWall(BlockPos wallPos) {
        if (!isWall(wallPos)) return false;
        Direction pushDirection = wallDirection(wallPos);
        if (pushDirection == null) return false;
        BlockPos center = DimBag.roomCenter(bag.bagId());
        BlockPos delta;
        if (pushDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            delta = maxWalls.relative(pushDirection).subtract(center);
        else
            delta = center.subtract(minWalls.relative(pushDirection));
        if (delta.getX() > BagsData.MAXIMUM_ROOM_RADIUS || delta.getY() > BagsData.MAXIMUM_ROOM_RADIUS || delta.getZ() > BagsData.MAXIMUM_ROOM_RADIUS) //should probably use a check for maximum world size (Y) to uncap the hard 126 block limit
            return false;
        if (bagLevel() == null)
            return false;
        BlockState air = Blocks.AIR.defaultBlockState();
        iterateWall(pushDirection, 0, p->level.setBlockAndUpdate(p.relative(pushDirection), level.getBlockState(p)));
        iterateWall(pushDirection, -1, p->level.setBlockAndUpdate(p, air));
        if (pushDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            maxWalls = maxWalls.relative(pushDirection);
        else
            minWalls = minWalls.relative(pushDirection);
        CompoundTag room = Tags.getOrCreateCompound(bag.unsafeRawAccess(), "room", CompoundTag::new);
        room.putLong("min_walls", minWalls.asLong());
        room.putLong("max_walls", maxWalls.asLong());
        loadRoom(true);
        NetworkManager.toClients(new DimBag.ClientSideRoomSizeUpdate(bag.bagId(), minWalls, maxWalls));
        return true;
    }

    public void temporaryChunkLoad(boolean offset) {
        if (bagLevel() == null)
            return;
        for (int x = minWalls.getX(); x < maxWalls.getX(); x += 16)
            for (int z = minWalls.getZ(); z < maxWalls.getZ(); z += 16)
                World.temporaryChunkLoad(level, new BlockPos(x, offset ? 129 : 128, z));
    }
}
