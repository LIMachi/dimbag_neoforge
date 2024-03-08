package com.limachi.dim_bag.blocks.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.block_entities.bag_modules.SculkModuleBlockEntity;
import com.limachi.dim_bag.menus.SculkMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class SculkModule extends BaseSingletonModule implements EntityBlock {

    public static final String NAME = "sculk";

    @RegisterBlock
    public static RegistryObject<SculkModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public SculkModule() { super(NAME); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return SculkModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.installModule(NAME, pos, new CompoundTag());
        if (level.getBlockEntity(pos) instanceof SculkModuleBlockEntity be)
            be.install(stack.getOrCreateTag());
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.uninstallModule(NAME, pos);
        if (level.getBlockEntity(pos) instanceof SculkModuleBlockEntity be)
            stack.getOrCreateTag().merge(be.uninstall());
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        SculkMenu.open(player, pos);
        return true;
    }

    @SubscribeEvent
    public static void experienceListener(LivingExperienceDropEvent event) {
        if (event.getDroppedExperience() <= 0) return;
        final int[] left = {event.getDroppedExperience()};
        BagsData.onEach(b->{
            if (left[0] > 0 && b.bagLevel() != null && b.isModulePresent(NAME) && b.getHolderPosition(false).map(p -> event.getEntity() != null && p.getFirst().equals(event.getEntity().level()) && event.getEntity().blockPosition().distSqr(p.getSecond()) < 64).orElse(false)) {
                CompoundTag modules = b.getAllModules(NAME);
                for (String k : modules.getAllKeys()) {
                    if (b.bagLevel().getBlockEntity(BlockPos.of(modules.getCompound(k).getLong("position"))) instanceof SculkModuleBlockEntity be)
                        left[0] = be.insertXp(left[0]);
                }
            }
        });
        event.setDroppedExperience(left[0]);
    }
}
