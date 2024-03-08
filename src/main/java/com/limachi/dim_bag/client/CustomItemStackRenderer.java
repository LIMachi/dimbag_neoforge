package com.limachi.dim_bag.client;

import com.limachi.dim_bag.items.BlockItemWithCustomRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomItemStackRenderer extends BlockEntityWithoutLevelRenderer {
    private static CustomItemStackRenderer INSTANCE = null;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public CustomItemStackRenderer(BlockEntityRenderDispatcher berDispatcher, EntityModelSet models) {
        super(berDispatcher, models);
        blockEntityRenderDispatcher = berDispatcher;
    }

    public static CustomItemStackRenderer getInstance() {
        if (INSTANCE == null) {
            Minecraft mc = Minecraft.getInstance();
            INSTANCE = new CustomItemStackRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return INSTANCE;
    }

    public static ItemDisplayContext currentItemDisplayContext = ItemDisplayContext.NONE;

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        currentItemDisplayContext = ctx;
        if (stack.getItem() instanceof BlockItemWithCustomRenderer bi)
            bi.renderByItem(blockEntityRenderDispatcher, stack, ctx, pose, buffer, combinedLight, combinedOverlay);
    }
}
