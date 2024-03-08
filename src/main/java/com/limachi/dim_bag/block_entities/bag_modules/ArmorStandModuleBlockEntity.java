package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.blocks.bag_modules.ArmorStandModule;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.StackUtils;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * config menu:
 *   chose selected slot
 *   chose energy action slot
 * inject:
 *   item (place item in selected slot)
 *   energy (charge item in selected slot)
 * extract:
 *   item (remove item from selected slot)
 *   energy (extract from items selected slot)
 */

public class ArmorStandModuleBlockEntity extends BlockEntity implements IEnergyStorage, IItemHandler {

    protected BagInstance bag = null;
    protected final LazyOptional<ArmorStandModuleBlockEntity> handle = LazyOptional.of(()->this);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (ForgeCapabilities.ITEM_HANDLER.equals(cap) || ForgeCapabilities.ENERGY.equals(cap)) {
            if (bag == null && level != null)
                bag = BagsData.getBagHandle(level, worldPosition, () -> bag = null);
            if (bag != null)
                return handle.cast();
        }
        return LazyOptional.empty();
    }

    protected EquipmentSlot selectedInventorySlot = EquipmentSlot.MAINHAND;
    protected EquipmentSlot selectedEnergySlot = EquipmentSlot.MAINHAND;

    @RegisterBlockEntity(blocks = "armor_stand_module")
    public static RegistryObject<BlockEntityType<ArmorStandModuleBlockEntity>> R_TYPE;

    public ArmorStandModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("inv_slot", selectedInventorySlot.ordinal());
        tag.putInt("energy_slot", selectedEnergySlot.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        selectedInventorySlot = EquipmentSlot.values()[tag.getInt("inv_slot")];
        selectedEnergySlot = EquipmentSlot.values()[tag.getInt("energy_slot")];
    }

    public Optional<IEnergyStorage> energyHandle() {
        return bag == null ? Optional.empty() : bag.getHolder(false).flatMap(e->e instanceof LivingEntity target && !(target instanceof BagEntity) ? target.getItemBySlot(selectedEnergySlot).getCapability(ForgeCapabilities.ENERGY).resolve() : Optional.empty());
    }

    public Optional<SlotAccess> slotHandle() {
        return bag == null ? Optional.empty() : bag.getHolder(false).flatMap(e->e instanceof LivingEntity target && !(target instanceof BagEntity) ? Optional.of(SlotAccess.forEquipmentSlot(target, selectedInventorySlot)) : Optional.empty());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) { return energyHandle().map(s->s.receiveEnergy(maxReceive, simulate)).orElse(0); }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) { return energyHandle().map(s->s.extractEnergy(maxExtract, simulate)).orElse(0); }

    @Override
    public int getEnergyStored() { return energyHandle().map(IEnergyStorage::getEnergyStored).orElse(0); }

    @Override
    public int getMaxEnergyStored() { return energyHandle().map(IEnergyStorage::getMaxEnergyStored).orElse(0); }

    @Override
    public boolean canExtract() { return energyHandle().map(IEnergyStorage::canExtract).orElse(false); }

    @Override
    public boolean canReceive() { return energyHandle().map(IEnergyStorage::canReceive).orElse(false); }

    @Override
    public int getSlots() { return bag != null && bag.getHolder(false).map(e->e instanceof LivingEntity).orElse(false) ? 1 : 0; }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return slotHandle().map(sa -> {
            ItemStack stack = sa.get();
            if (ArmorStandModule.canExtract(stack))
                return stack;
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!ArmorStandModule.canInsert(stack))
            return stack;
        return slotHandle().map(s->{
            if (!StackUtils.canMerge(stack, s.get())) return stack;
            Pair<ItemStack, ItemStack> p = StackUtils.merge(s.get(), stack);
            if (!simulate)
                s.set(p.getFirst());
            return p.getSecond();
        }).orElse(stack);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slotHandle().map(s->{
            if (amount <= 0) return ItemStack.EMPTY;
            if (!ArmorStandModule.canExtract(s.get()))
                return ItemStack.EMPTY;
            Pair<ItemStack, ItemStack> p = StackUtils.extract(s.get(), amount);
            if (!simulate)
                s.set(p.getSecond());
            return p.getFirst();
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public int getSlotLimit(int slot) { return bag == null ? 0 : selectedInventorySlot.isArmor() ? 1 : 64; }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return ArmorStandModule.canInsert(stack); //FIXME: could also check if the slot access is available and can receive the item
    }
}
