package com.limachi.dim_bag;

import com.limachi.dim_bag.utils.ModBase;
import com.limachi.dim_bag.utils.annotations.Config;
import com.limachi.dim_bag.utils.annotations.Mod;
import com.limachi.dim_bag.utils.annotations.RegisterTab;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

@Mod("dim_bag")
public final class DimBag extends ModBase {

    public static final ArrayList<Pair<BlockPos, BlockPos>> CLIENT_SIDE_ROOM_SIZES = new ArrayList<>();

    @Config(path = "rooms", min = "512", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    public static int ROOM_SPACING = 1024;

    public static final ResourceKey<Level> BAG_DIM = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dim_bag", "bag"));

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("dim_bag.tab.title"));
        builder.icon(()->new ItemStack(Items.COMPARATOR));
    }

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    public static int closestRoomId(BlockPos pos) {
        int max;
//        if (BagsData.getInstance() != null)
//            max = BagsData.max();
//        else
//            max = CLIENT_SIDE_ROOM_SIZES.size();
        max = 128;
        return Mth.clamp((pos.getX() - 8 + ROOM_SPACING / 2) / ROOM_SPACING + 1, 0, max);
    }

    public static boolean isWall(Level level, BlockPos pos) {
        if (level.dimension().equals(BAG_DIM)) {
            int id = closestRoomId(pos);
            if (id <= 0)
                return false;
//            if (level instanceof ServerLevel)
//                return BagsData.runOnBag(id, b -> b.getRoom().isWall(pos), false);
//            else {
//                if (id >= CLIENT_SIDE_ROOM_SIZES.size())
//                    return false;
//                Pair<BlockPos, BlockPos> p = CLIENT_SIDE_ROOM_SIZES.get(id - 1);
//                return RoomData.isWall(pos, p.getFirst(), p.getSecond());
//            }
            return true;
        }
        return false;
    }

    public DimBag() {}
}
