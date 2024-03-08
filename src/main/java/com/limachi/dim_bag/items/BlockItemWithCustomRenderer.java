package com.limachi.dim_bag.items;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.block_entities.IRenderUsingItemTag;
import com.limachi.dim_bag.client.CustomItemStackRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class BlockItemWithCustomRenderer extends BlockItem {
    public BlockItemWithCustomRenderer(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CustomItemStackRenderer.getInstance();
            }
        });
    }

    private BlockEntity renderableEntity;

    @OnlyIn(Dist.CLIENT)
    public void renderByItem(BlockEntityRenderDispatcher dispatcher, ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof BlockItemWithCustomRenderer bi && bi.getBlock() instanceof EntityBlock eb) {
            if (renderableEntity == null)
                renderableEntity = eb.newBlockEntity(DimBag.INVALID_POS, bi.getBlock().defaultBlockState());
            if (renderableEntity != null) {
                if (renderableEntity instanceof IRenderUsingItemTag r)
                    r.prepareRender(stack.getOrCreateTag());
                dispatcher.renderItem(renderableEntity, pose, buffer, combinedLight, combinedOverlay);
            }
        }
    }
}
