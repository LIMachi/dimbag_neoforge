package com.limachi.dim_bag;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
stores access to a real bag in either form:
 item in inventory (any entity)
 item in curio/equivalent (player only)
 item in world (note: might do like create and make the item in world an actual entity)
 entity in world
 */
public class BagHandle {
    public static final BagHandle INVALIDATED = new BagHandle();

    public enum State {
        Invalidated, //no longer valid (the slot no longer has a bag, the entity was despawned, etc...)
        Equipment, //on a living entity in a equipment slot (body, main hand, off hand)
        Inventory, //in a player inventory (any slot)
        InWorld, //as an item on the ground
        Entity, //as a bag entity in world
    }

    State state;

    LivingEntity holder;
    EquipmentSlot eSlot;
    int slot;

    ItemEntity item;

    BagEntity entity;

    int id;

    protected BagHandle() { state = State.Invalidated; }

    public BagHandle(LivingEntity holder, EquipmentSlot slot) {
        this.holder = holder;
        ItemStack stack = holder.getItemBySlot(slot);
        if (BagItem.isRealBag(stack))
            this.id = BagItem.getRoomId(stack);
        this.eSlot = slot;
        if (this.id >= 0)
            this.state = State.Equipment;
        else
            this.state = State.Invalidated;
    }

    public BagHandle(LivingEntity holder, InteractionHand hand) {
        this(holder, hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    public BagHandle(Player player, int slot) {
        this.holder = holder;
        this.slot = slot;
        ItemStack stack = player.getInventory().getItem(slot);
        if (BagItem.isRealBag(stack))
            this.id = BagItem.getRoomId(stack);
        if (this.id >= 0)
            this.state = State.Inventory;
        else
            this.state = State.Invalidated;
    }

    public BagHandle(ItemEntity item) {
        this.item = item;
        ItemStack stack = item.getItem();
        if (BagItem.isRealBag(stack))
            this.id = BagItem.getRoomId(stack);
        if (this.id >= 0)
            this.state = State.InWorld;
        else
            this.state = State.Invalidated;
    }

    public BagHandle(BagEntity entity) {
        this.entity = entity;
        this.id = entity.bagId;
        if (this.id >= 0)
            this.state = State.Entity;
        else
            this.state = State.Invalidated;
    }

    public boolean checkValid() {
        return switch (state) {
            case Invalidated -> false;
            case Equipment -> {
                if (holder == null || holder.isRemoved())
                    yield false;
                ItemStack stack = holder.getItemBySlot(eSlot);
                yield BagItem.isRealBag(stack) && BagItem.getRoomId(stack) == id;
            }
            case Inventory -> {
                if (holder instanceof Player player && !player.isRemoved()) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    yield BagItem.isRealBag(stack) && BagItem.getRoomId(stack) == id;
                }
                yield false;
            }
            case InWorld -> {
                if (item == null || item.isRemoved())
                    yield false;
                ItemStack stack = item.getItem();
                yield BagItem.isRealBag(stack) && BagItem.getRoomId(stack) == id;
            }
            case Entity -> {
                if (entity == null || entity.isRemoved())
                    yield false;
                yield entity.bagId == id;
            }
        };
    }

    public boolean remove() {
        if (!checkValid())
            return false;
        switch (state) {
            case Equipment -> holder.setItemSlot(eSlot, ItemStack.EMPTY);
            case Inventory -> {
                if (holder instanceof Player player)
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
            case InWorld -> item.remove(Entity.RemovalReason.KILLED);
            case Entity -> entity.remove(Entity.RemovalReason.KILLED);
        }
        holder = null;
        entity = null;
        eSlot = null;
        slot = 0;
        item = null;
        state = State.Invalidated;
        return true;
    }

    public BagHandle spawn(Level level, BlockPos pos) { return spawn(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5); }
    public BagHandle spawn(Level level, Vec3 pos) { return spawn(level, pos.x, pos.y, pos.z); }
    public BagHandle spawn(Level level, double x, double y, double z) {
        int id = this.id;
        if (remove())
            return spawn(level, x, y, z, id);
        return INVALIDATED;
    }
    public static BagHandle spawn(Level level, double x, double y, double z, int id) { return new BagHandle(BagEntity.create(level, x, y, z, id)); }

    public BagHandle drop(Level level, BlockPos pos) { return drop(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5); }
    public BagHandle drop(Level level, Vec3 pos) { return drop(level, pos.x, pos.y, pos.z); }
    public BagHandle drop(Level level, double x, double y, double z) {
        int id = this.id;
        if (remove())
            return drop(level, x, y, z, id);
        return INVALIDATED;
    }
    public static BagHandle drop(Level level, double x, double y, double z, int id) { return new BagHandle(new ItemEntity(level, x, y, z, BagItem.withRoomId(id))); }

    public BagHandle equip(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        int id = this.id;
        if (remove())
            return equip(livingEntity, equipmentSlot, id);
        return INVALIDATED;
    }

    public static BagHandle equip(LivingEntity livingEntity, EquipmentSlot equipmentSlot, int id) {
        if (livingEntity.getItemBySlot(equipmentSlot).isEmpty())
            livingEntity.setItemSlot(equipmentSlot, BagItem.withRoomId(id));
        return new BagHandle(livingEntity, equipmentSlot);
    }

    public BagHandle putInSlot(Player player, int slot) {
        int id = this.id;
        if (remove())
            return putInSlot(player, slot, id);
        return INVALIDATED;
    }

    public static BagHandle putInSlot(Player player, int slot, int id) {
        if (player.getInventory().getItem(slot).isEmpty())
            player.getInventory().setItem(slot, BagItem.withRoomId(id));
        return new BagHandle(player, slot);
    }

    public BlockPos position() {
        if (!checkValid())
            return null;
        return switch (state) {
            case Invalidated -> null;
            case Equipment, Inventory -> holder.blockPosition();
            case InWorld -> item.blockPosition();
            case Entity -> entity.blockPosition();
        };
    }

    public int bagId() { return checkValid() ? id : -1; }
}
