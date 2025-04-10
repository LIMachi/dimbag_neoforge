package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.dim_bag.utils.Events;
import com.limachi.dim_bag.utils.Game;
import com.limachi.dim_bag.utils.annotations.Config;
import com.limachi.dim_bag.utils.annotations.RegisterEventListener;
import com.limachi.dim_bag.utils.annotations.RegisterMsg;
import com.limachi.dim_bag.utils.network.IS2CMsg;

import com.mojang.datafixers.util.Pair;

import dev.architectury.networking.NetworkManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class BagsData extends SavedData {
    @Config(path = "rooms", min = "3", max = "126", cmt = "Initial size of a new bag (in blocks, including walls)")
    public static int DEFAULT_ROOM_RADIUS = 3;

    @Config(path = "rooms", min = "512", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    public static int ROOM_SPACING = 1024;

    @Config(path = "rooms", min = "3", max = "126", cmt = "Maximum size of a bag (in blocks, including walls)")
    public static int MAXIMUM_ROOM_RADIUS = 64;

    private static BagsData INSTANCE = null;
    @RegisterMsg
    public record ClientSideRoomSizeUpdateBatch(BlockPos[] mins, BlockPos[] maxs) implements IS2CMsg<ClientSideRoomSizeUpdateBatch> {

        @Override
        public void run(NetworkManager.PacketContext ctx) {
            for (int i = 0; i < mins.length; ++i) {
                if (i < DimBag.CLIENT_SIDE_ROOM_SIZES.size())
                    DimBag.CLIENT_SIDE_ROOM_SIZES.set(i, new Pair<>(mins[i], maxs[i]));
                else
                    DimBag.CLIENT_SIDE_ROOM_SIZES.add(new Pair<>(mins[i], maxs[i]));
            }
        }
    }

    public static void sendRoomSizes(ServerPlayer player) {
        if (INSTANCE == null || INSTANCE.instances.isEmpty())
            return;
        BlockPos[] mins = new BlockPos[INSTANCE.instances.size()];
        BlockPos[] maxs = new BlockPos[INSTANCE.instances.size()];
        for (int i = 0; i < INSTANCE.instances.size(); ++i) {
            Pair<BlockPos, BlockPos> p = INSTANCE.instances.get(i).getRoom().getWalls();
            mins[i] = p.getFirst();
            maxs[i] = p.getSecond();
        }
        new ClientSideRoomSizeUpdateBatch(mins, maxs).sendToClient(player);
    }

    public static BagsData getInstance() {
        if (!Game.isLogicalClient())
            return INSTANCE;
        return null;
    }

    public static int max() {
        if (INSTANCE == null)
            return 0;
        return INSTANCE.instances.size();
    }
    private static LinkedList<Runnable> INVALIDATORS = new LinkedList<>();

    ListTag raw;
    private final ArrayList<BagInstance> instances = new ArrayList<>();
    public final ServerLevel level;

    private static BagInstance roomAt(Level level, BlockPos pos) {
        if (level instanceof ServerLevel && level.dimension().equals(DimBag.BAG_DIM)) {
            int id = DimBag.closestRoomId(pos);
            if (id != 0) {
                BagInstance bag = INSTANCE.instances.get(id - 1);
                if (bag.getRoom().isInside(pos))
                    return bag;
            }
        }
        return null;
    }

    /**
     * <pre>
     * Get a handle that will be valid for more than an instant, but require to be invalidated remotely
     * The invalidator CAN be null, but then you have to make sure to release the handle at the end of the calling function
     * example (99% of the usages will have this form):
     * {@code
     *      class tileEntityThing extends BlockEntity {
     *          private IBagInstance bag = null;
     *          private int bagId = 1;
     *
     *          ...
     *
     *          public IBagInstance getBag() {
     *              if (bag == null)
     *                  bag = BagsData.getBagHandle(bagId, ()->this.bag = null);
     *              return bag;
     *          }
     *      }
     * }
     * </pre>
     */
    public static BagInstance getBagHandle(int id, Runnable invalidator) {
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size()) {
            if (invalidator != null)
                INVALIDATORS.add(invalidator);
            return INSTANCE.instances.get(id - 1);
        }
        if (invalidator != null)
            invalidator.run();
        return null;
    }

    public static BagInstance getBagHandle(Level level, BlockPos pos, Runnable invalidator) {
        BagInstance out = roomAt(level, pos);
        if (out != null) {
            if (invalidator != null)
                INVALIDATORS.add(invalidator);
            return out;
        }
        if (invalidator != null)
            invalidator.run();
        return null;
    }

    public static BagInstance getBagHandle(ItemStack bag, Runnable invalidator) {
        return getBagHandle(BagItem.getBagId(bag), invalidator);
    }

    public static BagInstance getBagHandle(BagEntity bag, Runnable invalidator) {
        return getBagHandle(bag.getBagId(), invalidator);
    }

    public static BagInstance getBagHandle(BagItemEntity bag, Runnable invalidator) {
        return getBagHandle(bag.getBagId(), invalidator);
    }

    public static void onEach(Consumer<BagInstance> run) {
        if (getInstance() != null)
            INSTANCE.instances.forEach(run);
    }

    /**
     * Alternative to {@link BagsData#getBagHandle} to run something on a bag immediately without keeping a handle
     */
    public static <T> T runOnBag(int id, Function<BagInstance, T> run, T onFail) {
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size())
            return run.apply(INSTANCE.instances.get(id - 1));
        return onFail;
    }

    public static boolean runOnBag(int id, Consumer<BagInstance> run) {
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size()) {
            run.accept(INSTANCE.instances.get(id - 1));
            return true;
        }
        return false;
    }

    public static <T> T runOnBag(Level level, BlockPos pos, Function<BagInstance, T> run, T onFail) {
        BagInstance out = roomAt(level, pos);
        if (out != null)
            return run.apply(out);
        return onFail;
    }

    public static boolean runOnBag(Level level, BlockPos pos, Consumer<BagInstance> run) {
        BagInstance out = roomAt(level, pos);
        if (out != null) {
            run.accept(out);
            return true;
        }
        return false;
    }

    public static <T> T runOnBag(ItemStack bag, Function<BagInstance, T> run, T onFail) {
        int id = BagItem.getBagId(bag);
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size())
            return run.apply(INSTANCE.instances.get(id - 1));
        return onFail;
    }

    public static boolean runOnBag(ItemStack bag, Consumer<BagInstance> run) {
        int id = BagItem.getBagId(bag);
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size()) {
            run.accept(INSTANCE.instances.get(id - 1));
            return true;
        }
        return false;
    }

    public static <T> T runOnBag(BagEntity bag, Function<BagInstance, T> run, T onFail) {
        int id = bag.getBagId();
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size())
            return run.apply(INSTANCE.instances.get(id - 1));
        return onFail;
    }

    public static boolean runOnBag(BagEntity bag, Consumer<BagInstance> run) {
        int id = bag.getBagId();
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size()) {
            run.accept(INSTANCE.instances.get(id - 1));
            return true;
        }
        return false;
    }

    public static <T> T runOnBag(BagItemEntity bag, Function<BagInstance, T> run, T onFail) {
        int id = bag.getBagId();
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size())
            return run.apply(INSTANCE.instances.get(id - 1));
        return onFail;
    }

    public static boolean runOnBag(BagItemEntity bag, Consumer<BagInstance> run) {
        int id = bag.getBagId();
        if (id > 0 && getInstance() != null && id <= INSTANCE.instances.size()) {
            run.accept(INSTANCE.instances.get(id - 1));
            return true;
        }
        return false;
    }

    public static int newBagId() {
        if (getInstance() != null) {
            CompoundTag rawBag = new CompoundTag();
            INSTANCE.raw.add(rawBag);
            int id = INSTANCE.raw.size();
            INSTANCE.instances.add(new BagInstance(INSTANCE.level, id, rawBag));
            return id;
        }
        return 0;
    }

    public static int maxBagId() {
        if (getInstance() != null)
            return INSTANCE.instances.size();
        return 0;
    }

    public static Factory<BagsData> factory(ServerLevel bagLevel) {
        return new Factory<>(()->new BagsData(bagLevel), (t, p)->new BagsData(bagLevel, t), null);
    }

    private BagsData(ServerLevel level) {
        this(level, new CompoundTag());
    }
    private BagsData(ServerLevel level, CompoundTag data) {
        this.level = level;
        raw = data.getList("bags", Tag.TAG_COMPOUND);
        for (int i = 0; i < raw.size(); ++i)
            instances.add(new BagInstance(level, i + 1, raw.getCompound(i)));
    }

    private static void invalidate() {
        if (getInstance() != null)
            for (BagInstance instance : INSTANCE.instances)
                instance.invalidate();
        INSTANCE = null;
        for (Runnable invalidator : INVALIDATORS)
            invalidator.run();
        INVALIDATORS.clear();
    }

    private static boolean ready = false;

    @RegisterEventListener(Events.SERVER_LEVEL_LOAD)
    public static void onWorldLoad(ServerLevel level) {
        if (level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(DimBag.BAG_DIM)) {
            if (ready) {
                invalidate();
                if (level.dimension().equals(Level.OVERWORLD) && Game.getLevel(DimBag.BAG_DIM) instanceof ServerLevel bagLevel)
                    INSTANCE = level.getDataStorage().computeIfAbsent(factory(bagLevel), "bags");
                if (level.dimension().equals(DimBag.BAG_DIM) && Game.getLevel(Level.OVERWORLD) instanceof ServerLevel overwrold)
                    INSTANCE = overwrold.getDataStorage().computeIfAbsent(factory(level), "bags");
            }
            ready = true;
        }
    }

    private static boolean readyUnload = false;

    @RegisterEventListener(Events.SERVER_LEVEL_UNLOAD)
    public static void onWorldUnload(ServerLevel level) {
        if (level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(DimBag.BAG_DIM)) {
            if (readyUnload) {
                invalidate();
                ready = false;
                readyUnload = false;
            } else
                readyUnload = true;
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        for (int i = 0; i < instances.size(); ++i)
            instances.get(i).storeOn(raw.getCompound(i));
        compoundTag.put("bags", raw);
        return compoundTag;
    }

    @Override
    public boolean isDirty() { return true; }
}
