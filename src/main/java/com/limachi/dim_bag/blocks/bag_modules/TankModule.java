package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.block_entities.bag_modules.TankModuleBlockEntity;
import com.limachi.dim_bag.items.BlockItemWithCustomRenderer;
import com.limachi.dim_bag.items.bag_modes.TankMode;
import com.limachi.dim_bag.menus.TankMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import com.limachi.lim_lib.utils.FluidItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class TankModule extends BaseModule implements EntityBlock {
    public static final String NAME = "tanks";

    @RegisterBlock
    public static RegistryObject<TankModule> R_BLOCK;

    public TankModule() { super(NAME); }

    public static class TankModuleItem extends BlockItemWithCustomRenderer {

        @RegisterItem(name = "tank_module")
        public static RegistryObject<BlockItem> R_ITEM;

        public TankModuleItem() { super(R_BLOCK.get(), new Properties()); }

        @Override
        @Nonnull
        public Component getName(@Nonnull ItemStack stack) {
            if (stack.hasTag()) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(stack.getTag());
                if (!fluid.isEmpty()) {
                    ItemStack fluidItem = FluidItem.fromFluid(fluid);
                    return Component.translatable("block.dim_bag.tank_module_with_fluid", fluidItem.getItem().getName(fluidItem));
                }
            }
            return super.getName(stack);
        }
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag().copy();
        if (data.contains("display", Tag.TAG_COMPOUND)) {
            data.putString("label", data.getCompound("display").getString("Name"));
            data.remove("display");
        }
        CompoundTag tankMode = bag.getModeData(TankMode.NAME);
        if (!tankMode.contains("selected"))
            tankMode.putLong("selected", pos.asLong());
        bag.tanksHandle().ifPresent(s->s.installTank(pos, data));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag tankMode = bag.getModeData(TankMode.NAME);
        if (tankMode.getLong("selected") == pos.asLong()) {
            long p = bag.tanksHandle().map(d->d.getTanks() > 0 ? d.getTank(0).asLong() : 0L).orElse(0L);
            if (p != 0)
                tankMode.putLong("selected", p);
            else
                tankMode.remove("selected");
        }
        bag.tanksHandle().ifPresent(s->stack.getOrCreateTag().merge(s.uninstallTank(pos)));
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            FluidUtil.interactWithFluidHandler(player, hand, level, pos, null);
        else
            BagsData.runOnBag(level, pos, b-> TankMenu.open(player, b.bagId(), pos));
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return TankModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level unusedLevel, @Nonnull BlockState unusedState, @Nonnull BlockEntityType<T> unusedType) {
        return (level, pos, state, be) -> {
            if (be instanceof TankModuleBlockEntity o)
                o.tick();
        };
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getLightEmission(state, level, pos);
    }
}
