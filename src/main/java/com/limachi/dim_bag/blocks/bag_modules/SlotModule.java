package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.block_entities.bag_modules.SlotModuleBlockEntity;
import com.limachi.dim_bag.items.BlockItemWithCustomRenderer;
import com.limachi.dim_bag.menus.SlotMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class SlotModule extends BaseModule implements EntityBlock {
    public static final String SLOT_KEY = "slots";

    @RegisterBlock
    public static RegistryObject<SlotModule> R_BLOCK;

    public SlotModule() { super(SLOT_KEY); }

    public static class SlotModuleItem extends BlockItemWithCustomRenderer {

        @RegisterItem(name = "slot_module")
        public static RegistryObject<BlockItem> R_ITEM;

        public SlotModuleItem() { super(R_BLOCK.get(), new Properties()); }
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag().copy();
        if (data.contains("display", Tag.TAG_COMPOUND)) {
            data.putString("label", data.getCompound("display").getString("Name"));
            data.remove("display");
        }
        bag.slotsHandle().ifPresent(s->s.installSlot(pos, data));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.slotsHandle().ifPresent(s->stack.getOrCreateTag().merge(s.uninstallSlot(pos)));
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        BagsData.runOnBag(level, pos, b->SlotMenu.open(player, b.bagId(), pos));
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return SlotModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level unusedLevel, @Nonnull BlockState unusedState, @Nonnull BlockEntityType<T> unusedType) {
        return (level, pos, state, be) -> {
            if (be instanceof SlotModuleBlockEntity o)
                o.tick();
        };
    }
}
