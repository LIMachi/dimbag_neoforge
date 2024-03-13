package com.limachi.dim_bag.save_datas.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.bag_modules.ParadoxModule;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.World;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.*;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

class HolderData implements VibrationSystem {
    protected Entity entity = null;
    protected BlockPos position = null;
    protected Level level = null;
    protected boolean paradox = false;
    protected final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;
    protected final DynamicVibrationUser vibrationUser;
    protected VibrationSystem.Data vibrationData;
    protected int loadCoolDown = 0;

    @Override
    @Nonnull
    public Data getVibrationData() { return vibrationData; }

    @Override
    @Nonnull
    public User getVibrationUser() { return vibrationUser; }

    protected static class DynamicVibrationUser implements User {

        public static final PositionSource NO_SOURCE = new BlockPositionSource(DimBag.INVALID_POS) {
            @Override
            @Nonnull
            public Optional<Vec3> getPosition(@Nonnull Level level) {
                return Optional.empty();
            }
        };
        protected PositionSource source;
        final HolderData holder;

        DynamicVibrationUser(HolderData holder) {
            this.holder = holder;
            setUser(holder.entity);
        }

        protected void setUser(Entity entity) {
            if (entity != null)
                source = new EntityPositionSource(entity, (entity instanceof Player) ? entity.getEyeHeight() / 1.4f : entity.getEyeHeight());
            else
                source = NO_SOURCE;
        }

        @Override
        public int getListenerRadius() {
            return source != NO_SOURCE ? 16 : 0;
        }

        @Override
        @Nonnull
        public PositionSource getPositionSource() {
            return source;
        }

        @Override
        public boolean canReceiveVibration(@Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull GameEvent event, @Nonnull GameEvent.Context ctx) {
            if (holder.entity == null || holder.entity == ctx.sourceEntity())
                return false;
            if (holder.entity instanceof Mob mob && mob.isNoAi())
                return false;
            if (holder.entity instanceof LivingEntity living && living.isDeadOrDying())
                return false;
            return level.getWorldBorder().isWithinBounds(pos);
        }

        @Override
        public void onReceiveVibration(@Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull GameEvent event, @Nullable Entity entity, @Nullable Entity indirect, float distance) {

        }
    }

    protected HolderData(CompoundTag data, BlockPos minWalls, BlockPos maxWalls, ServerLevel bagLevel) {
        vibrationData = new VibrationSystem.Data();
        dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this)) {
            @Override
            public void add(@Nonnull ServerLevel level) {
                this.lastSection = null;
                move(level);
            }
        };
        if (data.contains("dimension") && data.contains("position")) {
            position = BlockPos.of(data.getLong("position"));
            level = World.getLevel(data.getString("dimension"));
        }
        if (data.contains("entity")) {
            UUID searchEntity = data.getUUID("entity");
            if (data.contains("paradox_position")) {
                BlockPos searchPos = BlockPos.of(data.getLong("paradox_position"));
                List<Entity> found = bagLevel.getEntities((Entity) null, new AABB(searchPos.offset(-1, -1, -1), searchPos.offset(1, 1, 1)), e -> e.getUUID().equals(searchEntity));
                if (found.size() > 0) {
                    entity = found.get(0);
                    paradox = true;
                }
            } else if (position != null && level != null) {
                List<Entity> found = level.getEntities((Entity) null, new AABB(position.offset(-1, -1, -1), position.offset(1, 1, 1)), e -> e.getUUID().equals(searchEntity));
                if (found.size() > 0)
                    entity = found.get(0);
            }
            if (entity == null && paradox)
                bagLevel.getEntities((Entity)null, new AABB(minWalls, maxWalls), e -> e.getUUID().equals(searchEntity));
        }
        vibrationUser = new DynamicVibrationUser(this);
    }

    protected CompoundTag serialize() {
        CompoundTag out = new CompoundTag();
        if (position != null && level != null) {
            out.putLong("position", position.asLong());
            out.putString("dimension", level.dimension().location().toString());
        }
        if (entity != null) {
            if (paradox)
                out.putLong("paradox_position", entity.blockPosition().asLong());
            out.putUUID("entity", entity.getUUID());
        }
        return out;
    }

    protected int heartbeat = 0;

    public void tick(BagInstance instance, Entity entity) {
        if (entity != null && level instanceof ServerLevel serverlevel) {
            heartbeat = Events.tick;
            boolean set = setHolder(instance, entity);
            VibrationSystem.Ticker.tick(serverlevel, vibrationData, vibrationUser);
            if (--loadCoolDown <= 0 || set) {
                if (position != null && !(entity instanceof Player))
                    World.temporaryChunkLoad(level, position);
                loadCoolDown = 200;
                instance.temporaryChunkLoad();
            }
        }
    }

    //a holder is valid if it ticked in the last 3 ticks (usefull to disable some behaviors when the bag is no longer ticking aka destroyed or put in a container)
    public boolean stillValid() {
        return heartbeat + 2 >= Events.tick;
    }

    public boolean setHolder(BagInstance instance, Entity entity) {
        if (this.entity != entity) {
            if (entity.level().dimension().equals(DimBag.BAG_DIM) && BagsData.runOnBag(entity.level(), entity.blockPosition(), BagInstance::bagId, 0) == 0) {
                if (entity instanceof BagItemEntity || entity instanceof BagEntity)
                    instance.leave(entity);
                paradox = true;
                return false;
            }
            boolean paradox = (entity.level().dimension().equals(DimBag.BAG_DIM) && instance.isInRoom(entity.blockPosition()));
            if (paradox) {
                if (entity instanceof BagItemEntity || entity instanceof BagEntity) { //bags as item/entities should never be allowed to live inside a bag if not inside an inventory (as this would probably result in loss of bag access)
                    if (entity.equals(entity))
                        instance.leave(entity);
                    this.paradox = true; //if the ticking entity is a bag but the holder is not a bag, instead we can monitor entities that are leaving the bag, and if the holder is leaving the bag, scan the room to teleport the bags with the holder to fix paradox stranding (could also scan the bag room every X tick for players, and if none are found try to extract bags)
                    return false;
                }
                this.paradox = true;
                if (!instance.isModulePresent(ParadoxModule.PARADOX_KEY)) {
                    if (level != null && position != null)
                        BagItem.unequipBags(entity, instance.bagId(), level, position, false);
                    else
                        ; //FIXME: player spawn if entity is player? world spawn?
                    return false;
                }
            } else {
                this.paradox = false;
                position = entity.blockPosition();
                level = entity.level();
            }
            vibrationUser.setUser(entity);
            if (level instanceof ServerLevel) {
                entity.updateDynamicGameEventListener(DynamicGameEventListener::add);
                if (this.entity != null)
                    this.entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
            }
            this.entity = entity;
            return true;
        }
        return false;
    }

    public Optional<Entity> getHolder(boolean nonParadoxOnly) {
        if (!stillValid() || (nonParadoxOnly && paradox)) return Optional.empty();
        return Optional.ofNullable(entity);
    }

    public Optional<Pair<Level, BlockPos>> getHolderPosition(boolean nonParadoxOnly) {
        if (!stillValid())
            return Optional.empty();
        if (paradox && !nonParadoxOnly && entity != null)
            return Optional.of(new Pair<>(World.getLevel(DimBag.BAG_DIM), entity.blockPosition()));
        if (position != null && level != null)
            return Optional.of(new Pair<>(level, position));
        return Optional.empty();
    }

    public void updateEventTracker(BiConsumer<DynamicGameEventListener<?>, ServerLevel> consumer) {
        if (level instanceof ServerLevel serverLevel)
            consumer.accept(dynamicGameEventListener, serverLevel);
    }
}
