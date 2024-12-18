package com.limachi.dim_bag.entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.bag_modules.IBagModule;
import com.limachi.dim_bag.blocks.bag_modules.ParasiteModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.bag_modes.ParasiteMode;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.dim_bag.save_datas.bag_data.SlotData;
import com.limachi.lim_lib.ISpecialEntityRider;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.EntityAttributeBuilder;
import com.limachi.lim_lib.registries.annotations.RegisterEntity;
import com.limachi.lim_lib.utils.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BagEntity extends Mob implements Container, ISpecialEntityRider {

    @RegisterEntity(width = 0.75f, height = 0.95f)
    public static RegistryObject<EntityType<BagEntity>> R_TYPE;

    @EntityAttributeBuilder
    public static AttributeSupplier.Builder attributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.);
    }

    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();

    public BagEntity(EntityType<? extends Mob> type, Level lvl) { super(type, lvl); }

    public static BagEntity create(Level lvl, double x, double y, double z, int id) {
        BagEntity out = new BagEntity(R_TYPE.get(), lvl);
        out.moveTo(x, y, z);
        out.getPersistentData().putInt(BagItem.BAG_ID_KEY, id);
        lvl.addFreshEntity(out);
        World.loadAround(out, out.chunkPosition(), true, true);
        return out;
    }

    @Override
    public void remove(@Nonnull RemovalReason reason) {
        World.loadAround(this, this.chunkPosition(), false, true);
        super.remove(reason);
    }

    public static BagEntity create(Level lvl, BlockPos pos, int id) {
        return create(lvl, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, id);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    public int getBagId() { return getPersistentData().getInt(BagItem.BAG_ID_KEY); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FALL) || source.is(DamageTypes.STALAGMITE)) //easter egg, a bag that receive cramming, fall or stalagmite damage will be auto equiped to a nearby entity TODO: (should probably put a damage cap for activation)
            level().getEntities(this, new AABB(blockPosition())).stream().findFirst().ifPresent(e->BagItem.equipBag(e, this));
        if (source.getEntity() != null || source.getDirectEntity() != null) {

            Entity sEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();

            if (sEntity != null && !sEntity.isRemoved()) {
                int id = getBagId();
                if (sEntity instanceof Player player) {
                    if (KeyMapController.SNEAK.getState(player))
                        BagItem.equipBag(player, this);
                    else {
                        PlayerUtils.giveOrDrop(player, BagItem.create(id));
                        this.remove(RemovalReason.KILLED);
                    }
                } else if (BagsData.runOnBag(id, b->b.isModulePresent(ParasiteModule.NAME), false))
                    BagItem.equipBag(sEntity, this);
            }
        }
        return false;
    }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    protected void pickUpItem(@Nonnull ItemEntity itemEntity) {}

    public static List<ItemStack> getEmptyEquipment() { return EMPTY_EQUIPMENT; }

    @Override
    @Nonnull
    public Iterable<ItemStack> getHandSlots() { return EMPTY_EQUIPMENT; }

    @Override
    @Nonnull
    public Iterable<ItemStack> getArmorSlots() { return EMPTY_EQUIPMENT; }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (ForgeCapabilities.FLUID_HANDLER.equals(cap))
            return BagsData.runOnBag(this, BagInstance::tanksHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ITEM_HANDLER.equals(cap))
            return BagsData.runOnBag(this, BagInstance::slotsHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ENERGY.equals(cap))
            return BagsData.runOnBag(this, BagInstance::energyHandle, LazyOptional.empty()).cast();
        else
            return LazyOptional.empty();
    }

    @Override
    public boolean isAlwaysTicking() { return true; }

    ChunkPos prevChunkPos = chunkPosition();

    @Override
    public void tick() {
        if (level() instanceof ServerLevel) {
            if (prevChunkPos != chunkPosition()) {
                World.loadAround(this, prevChunkPos, false, true);
                World.loadAround(this, chunkPosition(), true, true);
                prevChunkPos = chunkPosition();
            }
            BagsData.runOnBag(getBagId(), b->{
                Entity entity = this.getVehicle();
                if (entity == null)
                    entity = this;
                b.tick(entity);
            });
        }
        if (getYRot() != getYHeadRot())
            setYRot(getYHeadRot());
        super.tick();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        //do corrections here
        Entity vehicle = getVehicle();
        if (vehicle instanceof Player player) {
            double dx = player.getX();
            double dy = player.getZ();
            double x = dx + 0;
            double y = dy + 0.45;
            double ang = -player.yBodyRot / 180. * Math.PI;
            double rx = (x - dx) * Math.cos(ang) - (y - dy) * Math.sin(ang) + dx;
            double ry = (x - dx) * Math.sin(ang) - (y - dy) * Math.cos(ang) + dy;
            setPos(rx, player.getY() + 0.8, ry);
//            setYBodyRot(player.getVisualRotationYInDegrees());
        }
        if (vehicle != null) {
//            setXRot(vehicle.getXRot());
//            setYRot(vehicle.getYRot());
        }
        if (vehicle instanceof LivingEntity living) {
            setYBodyRot(living.yBodyRot);
        }
        if (vehicle instanceof Mob mob && !isParasiting()) {
            for (Goal.Flag reset : Goal.Flag.values()) {
                if (reset == Goal.Flag.JUMP && mob.getVehicle() instanceof Boat)
                    continue;
                mob.goalSelector.setControlFlag(reset, true);
            }
        }
    }

    public boolean isParasiting() { return false; }

    @Override
    public boolean canPickUpLoot() { return false; }

    @Override
    public boolean canHoldItem(@Nonnull ItemStack stack) { return false; }

    @Override
    @Nonnull
    public InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        if (player.getItemInHand(hand).getItem() instanceof BlockItem bi && bi.getBlock() instanceof IBagModule module) {
            int id = getBagId();
            ItemStack stack = player.getItemInHand(hand);
            if (id > 0 && World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level) {
                BagsData.runOnBag(getBagId(), b->{
                    if (module.canInstall(b)) {
                        BlockPos pos = b.getRoom().getAnyInstallPosition();
                        if (pos != null) {
                            level.setBlockAndUpdate(pos, bi.getBlock().defaultBlockState());
                            module.install(b, player, level, pos, stack);
                            if (!player.isCreative()) {
                                stack.shrink(1);
                                player.setItemInHand(hand, stack);
                            }
                        } else {
                            //can't install (no valid position)
                        }
                    } else {
                        //can't install (prevented by module)
                    }
                });
                return InteractionResult.SUCCESS;
            }
        }
        if (KeyMapController.SNEAK.getState(player))
            BagsData.runOnBag(getBagId(), b->b.enter(player, false));
        else
            BagMenu.open(player, getBagId(), 0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getContainerSize() {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(SlotData::getContainerSize).orElse(0), 0);
    }

    @Override
    public boolean isEmpty() {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(SlotData::isEmpty).orElse(false), false);
    }

    @Override
    public ItemStack getItem(int slot) {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(h->h.getItem(slot)).orElse(ItemStack.EMPTY), ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(h->h.removeItem(slot, amount)).orElse(ItemStack.EMPTY), ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(h->h.removeItemNoUpdate(slot)).orElse(ItemStack.EMPTY), ItemStack.EMPTY);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        BagsData.runOnBag(this, b->b.slotsHandle().ifPresent(h->h.setItem(slot, stack)));
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return BagsData.runOnBag(this, b->b.slotsHandle().map(h->stillValid(player)).orElse(false), false);
    }

    @Override
    public void clearContent() { BagsData.runOnBag(this, b->b.slotsHandle().ifPresent(SlotData::clearContent)); }

    @Override
    public boolean canControl(Entity entity) { return BagsData.runOnBag(this, b->b.isModeEnabled(ParasiteMode.NAME)); }
}
