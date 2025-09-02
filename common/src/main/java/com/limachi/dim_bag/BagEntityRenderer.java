package com.limachi.dim_bag;

import com.limachi.lim_lib.client.annotations.RegisterEntityRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.resources.ResourceLocation;

@RegisterEntityRenderer
@Environment(EnvType.CLIENT)
public class BagEntityRenderer extends MobRenderer<BagEntity, BagEntityModel<BagEntity>> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DimBag.MOD.registries.mod_id, "textures/entity/bag_entity.png");

    public BagEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx, new BagEntityModel<BagEntity>(ctx.bakeLayer(BagEntityModel.LAYER_LOCATION)), 0.5f); }

    @Override
    public ResourceLocation getTextureLocation(BagEntity entity) { return TEXTURE; }
}
