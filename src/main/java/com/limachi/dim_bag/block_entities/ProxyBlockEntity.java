package com.limachi.dim_bag.block_entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.VirtualBagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProxyBlockEntity extends BlockEntity {

    protected boolean real = false;
    protected int bag = 0;
    public static ItemStack UPGRADE_STACK = null;

    @RegisterBlockEntity
    public static RegistryObject<BlockEntityType<ProxyBlockEntity>> R_TYPE;

    public ProxyBlockEntity(BlockPos pos, BlockState state) {
        super(R_TYPE.get(), pos, state);
    }

    public boolean hasBagAccess(int target, boolean realOnly) {
        return (!realOnly || real) && (target == 0 || target == getBag());
    }

    public int getBag() {
        return bag > 0 ? bag : level instanceof ServerLevel sl && sl.dimension().equals(DimBag.BAG_DIM) ? DimBag.closestRoomId(worldPosition) : 0;
    }

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (ForgeCapabilities.FLUID_HANDLER.equals(cap))
            return BagsData.runOnBag(getBag(), BagInstance::tanksHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ITEM_HANDLER.equals(cap))
            return BagsData.runOnBag(getBag(), BagInstance::slotsHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ENERGY.equals(cap))
            return BagsData.runOnBag(getBag(), BagInstance::energyHandle, LazyOptional.empty()).cast();
        return LazyOptional.empty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("real", real);
        tag.putInt("bag", bag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        real = tag.getBoolean("real");
        bag = tag.getInt("bag");
    }

    public static ItemStack getUpgradeStack() {
        if (UPGRADE_STACK == null)
            UPGRADE_STACK = new ItemStack(Items.NETHER_STAR);
        return UPGRADE_STACK;
    }

    public boolean use(Player player, ItemStack stack, Direction from) {
        if (stack.getItem() instanceof BagItem) {
            if (real)
                drop(from);
            bag = BagItem.getBagId(stack);
            real = !(stack.getItem() instanceof VirtualBagItem);
            return real;
        }
        if (stack.getItem().equals(getUpgradeStack().getItem()) && bag > 0 && real) {
            int prev = bag;
            drop(from);
            bag = prev;
            return true;
        }
        BagMenu.open(player, getBag(), 0);
        return false;
    }

    public void drop(@Nullable Direction dir) {
        if (bag > 0 && level != null) {
            ItemEntity out;
            if (real)
                out = new BagItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, BagItem.create(bag), 0, 0, 0);
            else
                out = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, getUpgradeStack().copy(), 0, 0, 0);
            bag = 0;
            real = false;
            if (dir != null)
                out.move(MoverType.SELF, new Vec3(dir.getStepX() / 1.9, dir.getStepY() / 1.9, dir.getStepZ() / 1.9));
            level.addFreshEntity(out);
        }
    }
}
