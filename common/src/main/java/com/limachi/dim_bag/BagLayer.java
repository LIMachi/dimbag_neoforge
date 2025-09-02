package com.limachi.dim_bag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.lwjgl.system.NonnullDefault;

@Environment(EnvType.CLIENT)
@NonnullDefault
//@Mod.EventBusSubscriber(modid = DimBag.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BagLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

//    @SubscribeEvent
//    static void registerLayersRenderers(EntityRenderersEvent.AddLayers event) {
//        for (String rp : event.getSkins()) {
//            PlayerRenderer renderer = event.getSkin(rp);
//            if (renderer != null)
//                renderer.addLayer(new BagLayer<>(renderer, event.getEntityModels()));
//        }
//    }

    private final BagEntityModel<T> model;

    public BagLayer(RenderLayerParent<T, M> parent, EntityModelSet set) {
        super(parent);
        model = new BagEntityModel<T>(set.bakeLayer(BagEntityModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible() && entity instanceof Player player /*&& BagVisibilities.shouldShowBag(player)*/) {
//            pose.pushPose();
            getParentModel().copyPropertiesTo(model);
            if (getParentModel() instanceof HumanoidModel<?> humanoid)
                model.copyPropertiesFromHumanoid(humanoid);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//            pose.translate(0f, 0.5f, 0.1f);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(BagEntityRenderer.TEXTURE), false);
            model.renderToBuffer(pose, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
//            pose.popPose();
        }
    }
}
