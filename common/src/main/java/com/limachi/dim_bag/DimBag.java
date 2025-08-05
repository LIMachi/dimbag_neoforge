package com.limachi.dim_bag;

import com.limachi.lim_lib.InstancedMod;
import com.limachi.lim_lib.common.annotations.*;
import com.limachi.lim_lib.common.dataStorage.LevelDataField;

import com.limachi.lim_lib.common.utils.Game;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.*;

public final class DimBag {
    @ModInstance
    public static InstancedMod MOD;

    @Config(path = "rooms", min = "1024", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS! (the default value of 2048 is enough for ~15 bags per player for ~1000 players)", reload = true)
    public static int ROOM_SPACING = 2048;

    public static final ResourceKey<Level> BAG_DIM = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("dim_bag", "bag"));

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("dim_bag.tab.title"));
        builder.icon(()->new ItemStack(Items.COMPARATOR));
    }

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    public static int closestRoomId(BlockPos pos) {
        return Mth.clamp((pos.getX() - 8 + ROOM_SPACING / 2) / ROOM_SPACING + 1, 0, 128);
    }

    @LevelData
    public static final LevelDataField<Integer> test = new LevelDataField<>(0);

    @RegisterCommand("test get")
    public static int get(CommandContext<CommandSourceStack> ctx) {
        Component component = Component.literal("val: " + test.get(Game.getLevel(Level.OVERWORLD.location())));
        ctx.getSource().sendSuccess(()->component, true);
        return 1;
    }

    @RegisterCommand("test set <val>")
    public static int set(CommandContext<CommandSourceStack> ctx, @CmdArg("val") int val) {
        test.set(Game.getLevel(Level.OVERWORLD.location()), val);
        return 1;
    }
}
