package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.EntityAttributeBuilder;
import com.limachi.lim_lib.common.annotations.RegisterEntity;
import com.limachi.lim_lib.common.annotations.RegisterEventListener;
import com.limachi.lim_lib.common.modCreation.Events;

import com.mojang.datafixers.util.Pair;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashSet;

public class BagEntity extends Mob {
    @RegisterEntity(width = 0.75f, height = 0.95f)
    public static RegistrySupplier<EntityType<BagEntity>> R_TYPE;

    @EntityAttributeBuilder
    public static AttributeSupplier.Builder attributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.);
    }

    public BagEntity(EntityType<? extends Mob> entityType, Level level) { super(entityType, level); }

    @Override
    public boolean isPersistenceRequired() { return true; }

    public static final HashSet<String> VULNERABILITIES = Util.make(new HashSet<>(), h->{
        h.add("player"); //standard behavior
        h.add("genericKill"); //marked for death
    });

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return VULNERABILITIES.contains(damageSource.type().msgId());
    }

    public static BagEntity create(Level lvl, double x, double y, double z, int id) {
        BagEntity out = new BagEntity(R_TYPE.get(), lvl);
        out.moveTo(x, y, z);
        out.bagId = id;
        lvl.addFreshEntity(out);
        return out;
    }

    public static BagEntity create(Level lvl, BlockPos pos, int id) {
        return create(lvl, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, id);
    }

    @Override
    public boolean removeWhenFarAway(double closestPlayer) { return false; }

    @Override
    public boolean ignoreExplosion(Explosion explosion) { return true; }

    @Override
    public boolean isAlwaysTicking() { return true; }

    int bagId = -1;

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("bagId", bagId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        bagId = compoundTag.getInt("bagId");
    }

    public int getBagId() { return bagId; }

    public static final TicketType<ChunkPos> BAG_TICKET = TicketType.create("dim_bag", Comparator.comparingLong(ChunkPos::toLong), 600);

    public static void loadAroundBag(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl)
            for (int x = -1; x <= 1; ++x)
                for (int z = -1; z <= 1; ++z) {
                    int cx = SectionPos.blockToSectionCoord(pos.getX()) + x;
                    int cz = SectionPos.blockToSectionCoord(pos.getZ()) + z;
                    var c = new ChunkPos(cx, cz);
                    sl.getChunkSource().addRegionTicket(BAG_TICKET, c, 2, c);
                }
    }

    public static void registerBagPos(int bag, Level level, BlockPos pos) {
        var bags = Rooms.BAGS.get();
        if (bag >= 0 && bags != null) {
            var prev = bags.put(bag, new Pair<>(pos, level));
            if (prev == null || !prev.getFirst().equals(pos) || !prev.getSecond().equals(level))
                Rooms.BAGS.setDirty();
        }
    }

    public static void unregisterBagPos(int bag) {
        var bags = Rooms.BAGS.get();
        if (bag >= 0 && bags != null)
            if (bags.remove(bag) != null)
                Rooms.BAGS.setDirty();
    }

    @RegisterEventListener(Events.SERVER_STARTED)
    public static void serverStarted(MinecraftServer minecraftServer) {
        var bags = Rooms.BAGS.get();
        if (bags != null)
            for (var p : bags.values())
                loadAroundBag(p.getSecond(), p.getFirst());
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 20 == 0) {
            loadAroundBag(level(), blockPosition());
            registerBagPos(bagId, level(), blockPosition());
        }
        if (blockPosition().getY() < level().getMinBuildHeight())
            addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 2, false, false));
        if (blockPosition().getY() < level().getMinBuildHeight() - 2)
            addEffect(new MobEffectInstance(MobEffects.LEVITATION, 50, 2, false, false));
    }

    //left click to equip (attack, including projectiles because why not :P, right click to use/enter)

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return super.mobInteract(player, interactionHand);
        //like the bag item: either we enter the bag, or the bag is in the room with us (if the bag is with us, teleport it outside first)
    }

    @Override
    protected boolean wouldNotSuffocateAtTargetPose(Pose pose) { return true; }

    private int markedForDeath = -300;

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        switch (damageSource.type().msgId()) {
            case "genericKill" -> {
                if (tickCount - markedForDeath < 300) {
                    remove(RemovalReason.KILLED);
                    return true;
                } else {
                    markedForDeath = tickCount;
                    if (damageSource.getEntity() instanceof Player player)
                        player.sendSystemMessage(Component.translatable("entity.dim_bag.marked_for_death_message"));
                }
            }
            case "player" -> {
                if (damageSource.getEntity() instanceof Player player)
                    player.sendSystemMessage(Component.translatable("entity.dim_bag.equip_wip"));
            }
        }
        return false;
    }
}
