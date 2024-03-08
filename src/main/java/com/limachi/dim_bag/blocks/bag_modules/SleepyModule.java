package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class SleepyModule extends BaseSingletonModule {

    @Configs.Config
    public static String[] BLACK_LIST_IN_BAG_UNLESS_SLEEPY_MODULE = {"minecraft:.+_bed", "minecraft:respawn_anchor"};

    public static final String NAME = "sleep";

    @RegisterBlock
    public static RegistryObject<SleepyModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public SleepyModule() { super(NAME); }

    @SubscribeEvent
    public static void placingInvalidBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.dimension().equals(DimBag.BAG_DIM)) {
            ResourceLocation item = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (item != null) {
                String test = item.toString();
                if (Arrays.stream(BLACK_LIST_IN_BAG_UNLESS_SLEEPY_MODULE).anyMatch(test::matches) && BagsData.runOnBag(level, event.getPos(), bag->!bag.isModulePresent(NAME), true))
                    event.setCanceled(true);
            }
        }
    }
}
