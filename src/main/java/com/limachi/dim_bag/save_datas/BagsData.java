package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * did I just reinvent world capabilities? probably... but at least I understand the invalidation/rebuild process
 * (invalidators didn't seem to fire on level capabilities, resulting in garbage when switching between saves/servers)
 */
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagsData extends SavedData {

    @Configs.Config(path = "rooms", min = "3", max = "126", cmt = "Initial size of a new bag (in blocks, including walls)")
    public static int DEFAULT_ROOM_RADIUS = 3;

    @Configs.Config(path = "rooms", min = "512", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    public static int ROOM_SPACING = 1024;

    @Configs.Config(path = "rooms", min = "3", max = "126", cmt = "Maximum size of a bag (in blocks, including walls)")
    public static int MAXIMUM_ROOM_RADIUS = 64;

    private static BagsData INSTANCE = null;

    @RegisterMsg
    public record ClientSideRoomSizeUpdateBatch(BlockPos[] mins, BlockPos[] maxs) implements IRecordMsg {
        @Override
        public void clientWork(Player player) {
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
        NetworkManager.toClient(player, new ClientSideRoomSizeUpdateBatch(mins, maxs));
    }

    public static BagsData getInstance() {
        if (!Sides.isLogicalClient())
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

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && (level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(DimBag.BAG_DIM))) {
            if (ready) {
                invalidate();
                if (level.dimension().equals(Level.OVERWORLD) && World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel bagLevel)
                    INSTANCE = level.getDataStorage().computeIfAbsent(t -> new BagsData(bagLevel, t), () -> new BagsData(bagLevel), "bags");
                if (level.dimension().equals(DimBag.BAG_DIM) && World.getLevel(Level.OVERWORLD) instanceof ServerLevel overwrold)
                    INSTANCE = overwrold.getDataStorage().computeIfAbsent(t -> new BagsData(level, t), ()->new BagsData(level), "bags");
            }
            ready = true;
        }
    }

    private static boolean readyUnload = false;

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && (level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(DimBag.BAG_DIM))) {
            if (readyUnload) {
                invalidate();
                ready = false;
                readyUnload = false;
            } else
                readyUnload = true;
        }
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        for (int i = 0; i < instances.size(); ++i)
            instances.get(i).storeOn(raw.getCompound(i));
        nbt.put("bags", raw);
        return nbt;
    }

    @Override
    public boolean isDirty() { return true; }
}
