package com.limachi.dim_bag.client.renderers.block_entitites;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.block_entities.bag_modules.BatteryModuleBlockEntity;
import com.limachi.dim_bag.blocks.bag_modules.BatteryModule;
import com.limachi.dim_bag.save_datas.bag_data.EnergyData;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@StaticInitClient
public class BatteryBlockEntityRenderer implements BlockEntityRenderer<BatteryModuleBlockEntity> {

    static {
        ClientRegistries.setBer(BatteryModuleBlockEntity.R_TYPE, BatteryBlockEntityRenderer::new);
    }

    public static final ResourceLocation BATTERY_TEXTURE = new ResourceLocation(DimBag.MOD_ID, "block/battery/inner");
    public static final double BLOCK_PIXEL = 1d/16d;
    public static final float Z = 0.001f;

    protected final BlockEntityRendererProvider.Context ctx;

    public BatteryBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context ctx) { this.ctx = ctx; }

    public static void renderQuad(PoseStack pose, VertexConsumer buffer, Vector3f min, Vector3f max, TextureAtlasSprite sprite, float su, float sv, Vector4f color, int combinedLight, int combinedOverlay, int faceMask) {
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        float u0, v0, u1, v1;

        if ((faceMask & (1 << Direction.UP.ordinal())) != 0) {
            u0 = sprite.getU(min.x * su * 16f);
            v0 = sprite.getV((1 - min.z) * sv * 16f);
            u1 = sprite.getU(max.x * su * 16f);
            v1 = sprite.getV((1 - max.z) * sv * 16f);
            buffer.vertex(matrix, min.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
            buffer.vertex(matrix, max.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
            buffer.vertex(matrix, max.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
            buffer.vertex(matrix, min.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
        }
        if ((faceMask & (1 << Direction.DOWN.ordinal())) != 0) {
            u0 = sprite.getU(min.x * su * 16f);
            v0 = sprite.getV((1 - min.z) * sv * 16f);
            u1 = sprite.getU(max.x * su * 16f);
            v1 = sprite.getV((1 - max.z) * sv * 16f);
            buffer.vertex(matrix, max.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
            buffer.vertex(matrix, min.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
            buffer.vertex(matrix, min.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
            buffer.vertex(matrix, max.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
        }
        if ((faceMask & (1 << Direction.NORTH.ordinal())) != 0) {
            u0 = sprite.getU(min.x * su * 16f);
            v0 = sprite.getV((1 - min.y) * sv * 16f);
            u1 = sprite.getU(max.x * su * 16f);
            v1 = sprite.getV((1 - max.y) * sv * 16f);
            buffer.vertex(matrix, min.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
            buffer.vertex(matrix, min.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
            buffer.vertex(matrix, max.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
            buffer.vertex(matrix, max.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
        }
        if ((faceMask & (1 << Direction.SOUTH.ordinal())) != 0) {
            u0 = sprite.getU(min.x * su * 16f);
            v0 = sprite.getV((1 - min.y) * sv * 16f);
            u1 = sprite.getU(max.x * su * 16f);
            v1 = sprite.getV((1 - max.y) * sv * 16f);
            buffer.vertex(matrix, min.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
            buffer.vertex(matrix, min.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
            buffer.vertex(matrix, max.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
            buffer.vertex(matrix, max.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
        }
        if ((faceMask & (1 << Direction.EAST.ordinal())) != 0) {
            u0 = sprite.getU(min.z * su * 16f);
            v0 = sprite.getV((1 - min.y) * sv * 16f);
            u1 = sprite.getU(max.z * su * 16f);
            v1 = sprite.getV((1 - max.y) * sv * 16f);
            buffer.vertex(matrix, max.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
            buffer.vertex(matrix, max.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
            buffer.vertex(matrix, max.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
            buffer.vertex(matrix, max.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
        }
        if ((faceMask & (1 << Direction.WEST.ordinal())) != 0) {
            u0 = sprite.getU(min.z * su * 16f);
            v0 = sprite.getV((1 - min.y) * sv * 16f);
            u1 = sprite.getU(max.z * su * 16f);
            v1 = sprite.getV((1 - max.y) * sv * 16f);
            buffer.vertex(matrix, min.x, max.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
            buffer.vertex(matrix, min.x, min.y, min.z).color(color.x, color.y, color.z, color.w).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
            buffer.vertex(matrix, min.x, min.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
            buffer.vertex(matrix, min.x, max.y, max.z).color(color.x, color.y, color.z, color.w).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
        }
    }

    @Override
    public void render(@Nonnull BatteryModuleBlockEntity be, float partialTick, @Nonnull PoseStack pose, @Nonnull MultiBufferSource multiBuffer, int combinedLight, int combinedOverlay) {
        Level level = be.getLevel();
        boolean gui = level == null;
        BlockPos pos = be.getBlockPos();
        if (gui)
            level = Minecraft.getInstance().level;
        ctx.getBlockRenderDispatcher().getModelRenderer().tesselateBlock(level, ctx.getBlockRenderDispatcher().getBlockModel(be.getBlockState()), be.getBlockState(), pos, pose, multiBuffer.getBuffer(RenderType.cutoutMipped()), false, RandomSource.create(), be.getBlockState().getSeed(pos), combinedOverlay);
        long content = be.renderEnergy;

        VertexConsumer buffer = multiBuffer.getBuffer(RenderType.solid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BATTERY_TEXTURE);

        pose.pushPose();
        pose.scale((float) (14 * BLOCK_PIXEL), (float) (14 * BLOCK_PIXEL), (float) (14 * BLOCK_PIXEL));
        pose.translate(BLOCK_PIXEL, BLOCK_PIXEL, BLOCK_PIXEL);

        if (content <= 0)
            renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(.3f, .3f, .3f, 1), combinedLight, combinedOverlay, -1);
        else if (content >= EnergyData.ENERGY_STORAGE_PER_BATTERY)
            renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(1, 1, 1, 1), combinedLight, combinedOverlay, -1);
        else {
            float f = content / (float)EnergyData.ENERGY_STORAGE_PER_BATTERY;
            renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(1, 1, 1, 1), combinedLight, combinedOverlay, 1 << Direction.DOWN.ordinal());
            renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(.3f, .3f, .3f,1), combinedLight, combinedOverlay, 1 << Direction.UP.ordinal());
            int mask = -1;
            mask ^= (1 << Direction.DOWN.ordinal()) | (1 << Direction.UP.ordinal());
            renderQuad(pose, buffer, new Vector3f(0, f, 0), new Vector3f(1, 1, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(.3f, .3f, .3f, 1), combinedLight, combinedOverlay, mask);
            renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, f, 1), sprite, 14f / 16f, 14f / 16f, new Vector4f(1, 1, 1, 1), combinedLight, combinedOverlay, mask);
        }
        pose.popPose();
    }
}