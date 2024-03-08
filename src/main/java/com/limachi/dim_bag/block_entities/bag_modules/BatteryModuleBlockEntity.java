package com.limachi.dim_bag.block_entities.bag_modules;

import com.limachi.dim_bag.block_entities.IRenderUsingItemTag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BatteryModuleBlockEntity extends BlockEntity implements IRenderUsingItemTag {

    @RegisterBlockEntity(blocks = "battery_module")
    public static RegistryObject<BlockEntityType<BatteryModuleBlockEntity>> R_TYPE;

    public BatteryModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    private LazyOptional<IEnergyStorage> energyHandle = null;

    public long renderEnergy = 0;

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (energyHandle == null) {
                BagInstance bag = BagsData.getBagHandle(level, getBlockPos(), ()->energyHandle = null);
                if (bag != null) {
                    energyHandle = bag.energyHandle().cast();
                    energyHandle.addListener(t -> energyHandle = null);
                }
            }
            if (energyHandle != null)
                return energyHandle.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag out = super.getUpdateTag();
        out.putLong("render_energy", renderEnergy);
        return out;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("render_energy", renderEnergy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        renderEnergy = tag.getLong("render_energy");
    }

    public void tick() {
        if (level instanceof ServerLevel serverLevel)
            getCapability(ForgeCapabilities.ENERGY).ifPresent(h->{
                //do push/pull there
                if (renderEnergy != h.getEnergyStored()) {
                    renderEnergy = h.getEnergyStored();
                    setChanged();
                    serverLevel.players().forEach(p->p.connection.send(getUpdatePacket()));
                }
            });
    }

    @Override
    public void prepareRender(CompoundTag tag) {
        renderEnergy = tag.getLong("battery");
    }
}
