package com.limachi.dim_bag;

import com.limachi.lim_lib.client.annotations.StaticInitClient;
import com.limachi.lim_lib.client.modCreation.ClientStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class BagEntityModel<T extends LivingEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DimBag.MOD.registries.mod_id, "bag_model"), "main");

    @StaticInitClient(value = ClientStage.ENTITY_RENDERER, before = true)
    public static void staticInit() {
        EntityModelLayerRegistry.register(LAYER_LOCATION, BagEntityModel::createBodyLayer);
    }

    protected final ModelPart all;
    protected final ModelPart lid;
    protected final ModelPart handles;
    protected final ModelPart left_handle;
    protected final ModelPart right_handle;

    public BagEntityModel(ModelPart root) {
        all = root.getChild("body");
        lid = all.getChild("lid");
        handles = all.getChild("handles");
        left_handle = handles.getChild("left_handle");
        right_handle = handles.getChild("right_handle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        CubeDeformation nullDeformation = new CubeDeformation(0.0F);
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0f, 24f, 0f, 0f, (float)Math.PI, 0f));

        body.addOrReplaceChild("main", CubeListBuilder.create().texOffs(40, 0).addBox(6f, -12f, -1f, 2f, 11f, 4f, nullDeformation)
                .texOffs(52, 0).addBox(-8f, -12f, -1f, 2f, 11f, 4f, nullDeformation)
                .texOffs(0, 0).addBox(-6f, -12f, -3f, 12f, 12f, 8f, nullDeformation), PartPose.offset(0f, 0f, 0f));

        body.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 20).addBox(-6f, -3f, 0f, 12f, 3f, 8f, nullDeformation)
                .texOffs(32, 22).addBox(-2f, -2f, 8f, 4f, 4f, 2f, nullDeformation), PartPose.offset(0f, -12f, -3f));

        PartDefinition handles = body.addOrReplaceChild("handles", CubeListBuilder.create(), PartPose.offset(0f, -1f, 0f));

        handles.addOrReplaceChild("right_handle", CubeListBuilder.create().texOffs(0, 31).addBox(-1f, 4f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(14, 31).addBox(-1f, -5f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(28, 31).addBox(-1f, -4f, -5f, 2f, 8f, 1f, nullDeformation), PartPose.offset(4f, -6f, -2f));

        handles.addOrReplaceChild("left_handle", CubeListBuilder.create().texOffs(0, 37).addBox(-1f, 4f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(14, 37).addBox(-1f, -5f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(34, 31).addBox(-1f, -4f, -5f, 2f, 8f, 1f, nullDeformation), PartPose.offset(-4f, -6f, -2f));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof BagEntity bag) {
            all.setRotation(0, bag.getYRot() + (float)Math.PI, 0);
        } else if (entity instanceof Player) {
            all.offsetPos(new Vector3f(0f, 12f, 4.5f).rotate(new Quaternionf().rotateZYX(all.zRot, all.yRot, all.xRot)));
            handles.setPos(0f, -2.5f, -1f);
        } else {
            //try to put the bag on the head of the entity as if it was a parasite
        }
        //do lid animation
    }

    public <M extends HumanoidModel<?>> void copyPropertiesFromHumanoid(M parent) {
        all.copyFrom(parent.body);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        all.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}