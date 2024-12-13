package com.limachi.dim_bag.events;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.World;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class RoomEvents {
    @Configs.Config(cmt = "list of mobs (ressource style, regex compatible), that should not be able to grief (warning, the defaults are there because some vanilla mobs might be able to break walls)")
    public static String[] BLACK_LIST_MOB_GRIEF = {"minecraft:enderman", "minecraft:silverfish"};

    @Configs.Config(cmt = "list of blocks that should not be allowed to be placed in the bag (note: will still be allowed if placed while in creative)")
    public static String[] BLACK_LIST_IN_BAG = {"minecraft:.+_bed", "minecraft:respawn_anchor"};

    @SubscribeEvent
    public static void placingInvalidBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!event.getEntity().isCreative() && level.dimension().equals(DimBag.BAG_DIM)) {
            ResourceLocation item = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (item != null) {
                String test = item.toString();
                if (Arrays.stream(BLACK_LIST_IN_BAG).anyMatch(test::matches))
                    event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void mobGrief(EntityMobGriefingEvent event) {
        if (event.getEntity().level().dimension() == DimBag.BAG_DIM) {
            String entityRes = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString();
            if (Arrays.stream(BLACK_LIST_MOB_GRIEF).anyMatch(entityRes::matches))
                event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void howTFDidYouPutAWitherOrDragonInYourBagAndThoughtItWasAGoodIdea(LivingDestroyBlockEvent event) {
        if (DimBag.isWall(event.getEntity().level(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeMined(PlayerEvent.BreakSpeed event) {
        if (event.getPosition().map(p->DimBag.isWall(event.getEntity().level(), p)).orElse(false))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeBroken(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative() && DimBag.isWall((Level)event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeExploded(ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension() == DimBag.BAG_DIM) {
            Vec3 pos = event.getExplosion().getPosition();
            BagsData.runOnBag(event.getLevel(), new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), bag->event.getAffectedBlocks().removeIf(p->bag.getRoom().isWall(p)));
        }
    }

    @SubscribeEvent
    public static void entitiesArentAllowedToTravelInBagDimensionOutsideRooms(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        if (!level.isClientSide && level.dimension().equals(DimBag.BAG_DIM)) {
            BagsData.runOnBag(DimBag.closestRoomId(entity.blockPosition()), bag->{
                if (!bag.getRoom().isInside(entity.blockPosition()))
                    bag.enter(entity, false);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mojangCantFixThisBugReally(SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(DimBag.BAG_DIM) && World.overworld() instanceof ServerLevel overworld)
            overworld.setDayTime(event.getNewTime());
    }
}
