package com.limachi.dim_bag.save_datas.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.bag_modules.TeleportModule;
import com.limachi.dim_bag.capabilities.entities.BagTP;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.bag_modes.ModesRegistry;
import com.limachi.dim_bag.items.bag_modes.SettingsMode;
import com.limachi.dim_bag.items.bag_modes.TankMode;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.nbt.NBT;
import com.limachi.lim_lib.utils.SimpleTank;
import com.limachi.lim_lib.utils.Tags;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;
import java.util.function.BiConsumer;

public class BagInstance {

    public static final String DISABLED_MARKER = "disabled";
    public static final String POSITION = "position";
    public static final String MODULE_STORAGE = "modules";
    public static final String MODES_STORAGE = "modes";

    private final int bag;
    private final HolderData holder;
    private final CompoundTag rawData;
    private final SlotData slots;
    private final TankData tanks;
    private final EnergyData energy;
    private final RoomData room;

    public BagInstance(ServerLevel level, int id, CompoundTag data) {
        bag = id;
        rawData = data;
        if (!rawData.contains(MODULE_STORAGE))
            rawData.put(MODULE_STORAGE, new CompoundTag());
        if (!rawData.contains(MODES_STORAGE)) {
            rawData.put(MODES_STORAGE, new CompoundTag());
            CompoundTag modes = rawData.getCompound(MODES_STORAGE);
            for (ModesRegistry.ModeEntry me : ModesRegistry.modesList)
                modes.put(me.name(), me.mode().initialData(this));
        }
        slots = new SlotData(this, Tags.getOrCreateList(rawData, "slots", ListTag::new));
        tanks = new TankData(this, Tags.getOrCreateList(rawData, "tanks", ListTag::new), ()->getModeData(TankMode.NAME));
        energy = new EnergyData(this);
        room = new RoomData(level, this, rawData);
        holder = new HolderData(id, data.getCompound("holder"), room.minWalls(), room.maxWalls(), level);
    }

    public ServerLevel bagLevel() { return room.bagLevel(); }

    public RoomData getRoom() { return room; }

    public long enabledModesMask() {
        long mask = 0;
        CompoundTag modes = rawData.getCompound(MODES_STORAGE);
        for (String mode : modes.getAllKeys())
            if (isModeEnabled(mode))
                mask |= 1L << ModesRegistry.getModeIndex(mode);
        return mask;
    }

    public CompoundTag unsafeRawAccess() { return rawData; }
    public CompoundTag getModeData(String mode) {
        return Tags.getOrCreateCompound(rawData.getCompound(MODES_STORAGE), mode, ()->ModesRegistry.getMode(mode).initialData(this));
    }

    public LazyOptional<SlotData> slotsHandle() { return slots.getHandle(); }

    public LazyOptional<SlotData.SlotEntry> slotHandle(BlockPos pos) { return slots.getSlotHandle(pos); }

    public Component getSlotLabel(BlockPos pos) { return slots.getSlotLabel(pos); }

    public void setSlotLabel(BlockPos pos, Component label) { slots.setSlotLabel(pos, label); }

    public LazyOptional<TankData> tanksHandle() { return tanks.getHandle(); }

    public LazyOptional<SimpleTank> tankHandle(BlockPos pos) { return tanks.getTankHandle(pos); }

    public Component getTankLabel(BlockPos pos) { return tanks.getTankLabel(pos); }

    public void setTankLabel(BlockPos pos, Component label) { tanks.setTankLabel(pos, label); }

    public LazyOptional<EnergyData> energyHandle() { return energy.getHandle(); }

    public void invalidate() {
        slots.invalidate();
        tanks.invalidate();
        energy.invalidate();
    }

    public int bagId() { return bag; }

    public void storeOn(CompoundTag instance) {
        instance.put("holder", holder.serialize());
        instance.put("slots", slots.serialize());
        instance.put("tanks", tanks.serialize());
        energy.store();
    }

    public void tick(Entity entity) {
        holder.tick(this, entity);
        if (entity != null && entity.level() instanceof ServerLevel serverLevel && Events.tick % 100 == 0) {
            room.iterBlockEntities().forEach(be -> {
                if (be instanceof BeaconBlockEntity beacon)
                    room.beaconEffects(beacon).forEach(e->MobEffectUtil.addEffectToPlayersAround(serverLevel, entity, entity.position(), 10, e, 200));
                if (be instanceof ConduitBlockEntity)
                    MobEffectUtil.addEffectToPlayersAround(serverLevel, entity, entity.position(), 10, new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true), 200);
            });
        }
    }

    public Optional<Entity> getHolder(boolean nonParadoxOnly) { return holder.getHolder(nonParadoxOnly); }

    public Optional<Pair<Level, BlockPos>> getHolderPosition(boolean nonParadoxOnly) {
        return holder.getHolderPosition(nonParadoxOnly);
    }

    public void updateEventTracker(BiConsumer<DynamicGameEventListener<?>, ServerLevel> consumer) {
        holder.updateEventTracker(consumer);
    }

    /**
     * used in settings screen
     */
    public static boolean isModeEnabled(CompoundTag settings, String name) {
        if (!ModesRegistry.getMode(name).canDisable())
            return true;
        return !settings.getBoolean(name + "_disabled");
    }

    /**
     * used in settings screen
     */
    public static boolean setEnabledMode(CompoundTag settings, String name, boolean state) {
        if (!state && !ModesRegistry.getMode(name).canDisable())
            return false;
        settings.putBoolean(name + "_disabled", !state);
        return true;
    }

    public boolean isModeEnabled(String name) {
        String module = ModesRegistry.getMode(name).requiredModule;
        if (module != null && !rawData.getCompound(MODULE_STORAGE).contains(module))
            return false;
        return isModeEnabled(rawData.getCompound(MODES_STORAGE).getCompound(SettingsMode.NAME), name);
    }

    public boolean setEnabledMode(String name, boolean state) {
        String module = ModesRegistry.getMode(name).requiredModule;
        if (state && module != null && !rawData.getCompound(MODULE_STORAGE).contains(module))
            return false;
        return setEnabledMode(rawData.getCompound(MODES_STORAGE).getCompound(SettingsMode.NAME), name, state);
    }

    public void installModule(String name, BlockPos pos, CompoundTag data) {
        CompoundTag modules = Tags.getOrCreateCompound(rawData.getCompound(MODULE_STORAGE), name, CompoundTag::new);
        long p = pos.asLong();
        String ps = "" + p;
        if (modules.contains(ps, Tag.TAG_COMPOUND)) {
            CompoundTag entry = modules.getCompound(ps);
            NBT.clear(entry);
            entry.merge(data);
            entry.putLong(POSITION, p);
        } else {
            CompoundTag insert = data.copy();
            insert.putLong(POSITION, p);
            modules.put(ps, insert);
        }
    }

    public CompoundTag uninstallModule(String name, BlockPos pos) {
        CompoundTag modules = rawData.getCompound(MODULE_STORAGE).getCompound(name);
        String ps = "" + pos.asLong();
        CompoundTag entry = modules.getCompound(ps);
        entry.remove(POSITION);
        CompoundTag out = entry.copy();
        modules.remove(ps);
        if (modules.isEmpty())
            rawData.getCompound(MODULE_STORAGE).remove(name);
        return out;
    }

    public CompoundTag getModule(String name, BlockPos pos) {
        CompoundTag modules = rawData.getCompound(MODULE_STORAGE).getCompound(name);
        return modules.getCompound("" + pos.asLong());
    }

    public boolean isModulePresent(String name) { return rawData.getCompound(MODULE_STORAGE).contains(name); }

    public CompoundTag getAllModules(String name) { return rawData.getCompound(MODULE_STORAGE).getCompound(name); }

    public Iterator<Pair<BlockPos, CompoundTag>> iterModules(String name) {
        final CompoundTag s = rawData.getCompound(MODULE_STORAGE).getCompound(name);
        final Iterator<String> k = s.getAllKeys().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return k.hasNext();
            }

            @Override
            public Pair<BlockPos, CompoundTag> next() {
                String t = k.next();
                return new Pair<>(BlockPos.of(Long.decode(t)), s.getCompound(t));
            }
        };
    }

    public Entity enter(Entity entity, boolean proxy) {
        BlockPos destination = DimBag.roomCenter(bag);
        BlockPos test = TeleportModule.getDestination(this, entity).orElse(null);
        if (test == null && !(entity instanceof Player || entity instanceof ItemEntity)) return null;
        if (test != null)
            destination = test;
        else {
            test = ((Optional<BlockPos>) Cap.run(entity, BagTP.TOKEN, c -> Optional.of(c.getEnterPos(bag)), Optional.empty())).orElse(null);
            if (test != null && room.isInside(test) && !room.isWall(test))
                destination = test;
        }
        if (proxy)
            Cap.run(entity, BagTP.TOKEN, c -> c.setLeavePos(bag, entity.level().dimension(), entity.blockPosition()));
        return World.teleportEntity(entity, DimBag.BAG_DIM, destination.getX() + 0.5, destination.getY() + 0.5, destination.getZ() + 0.5, entity.getXRot(), entity.getYRot());
    }

    public Entity leave(Entity entity) {
        if (!(entity instanceof BagEntity) && !(entity instanceof BagItemEntity) && entity.equals(holder.entity) && holder.paradox && bagLevel() != null) //paradox stranding prevention, another check could be done globally every X tick by scanning the room for players and bags and forcefully expulsing bags (with same id as the room) if no players are found in the room
            bagLevel().getEntities(entity,new AABB(room.minWalls(), room.maxWalls()), e->(e instanceof BagEntity bag1 && bag1.getBagId() == bag) || (e instanceof BagItemEntity bag2 && bag2.getBagId() == bag)).forEach(this::leave);
        Entity finalEntity = entity;
        Optional<Pair<Level, BlockPos>> out = entity.getCapability(CapabilityManager.get(BagTP.TOKEN)).resolve().map(c -> {
            Pair<Level, BlockPos> t = c.getLeavePos(bag);
            c.setEnterPos(bag, finalEntity.blockPosition());
            c.clearLeavePos(bag);
            return t;
        });
        if (out.isEmpty())
            out = getHolderPosition(true);
        if (out.isPresent()) {
            entity = World.teleportEntity(entity, out.get().getFirst().dimension(), out.get().getSecond());
            if (entity instanceof Player player && getModeData(SettingsMode.NAME).getBoolean("quick_equip"))
                entity.level().getEntities(BagEntity.R_TYPE.get(), new AABB(entity.blockPosition().offset(-2, -2, -2), entity.blockPosition().offset(2, 2, 2)), e->e.getBagId() == bag).forEach(e-> BagItem.equipBag(player, e));
        }
        return entity;
    }
}
