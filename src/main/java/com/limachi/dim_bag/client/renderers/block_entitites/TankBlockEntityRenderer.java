package com.limachi.dim_bag.client.renderers.block_entitites;

import com.limachi.dim_bag.block_entities.bag_modules.TankModuleBlockEntity;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@StaticInitClient
public class TankBlockEntityRenderer implements BlockEntityRenderer<TankModuleBlockEntity> {

    static {
        ClientRegistries.setBer(TankModuleBlockEntity.R_TYPE, TankBlockEntityRenderer::new);
    }

    public static final double BLOCK_PIXEL = 1d/16d;
    public static final float Z = 0.001f;

    protected final BlockEntityRendererProvider.Context ctx;

    public TankBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context ctx) { this.ctx = ctx; }

    @Override
    public void render(@Nonnull TankModuleBlockEntity be, float partialTick, @Nonnull PoseStack pose, @Nonnull MultiBufferSource multiBuffer, int combinedLight, int combinedOverlay) {
        Level level = be.getLevel();
        boolean gui = level == null;
        BlockPos pos = be.getBlockPos();
        if (gui)
            level = Minecraft.getInstance().level;
        ctx.getBlockRenderDispatcher().getModelRenderer().tesselateBlock(level, ctx.getBlockRenderDispatcher().getBlockModel(be.getBlockState()), be.getBlockState(), pos, pose, multiBuffer.getBuffer(RenderType.cutoutMipped()), false, RandomSource.create(), be.getBlockState().getSeed(pos), combinedOverlay);
        FluidStack fs = be.renderStack;
        if (!fs.isEmpty()) {
            IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fs.getFluid());
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(renderProperties.getStillTexture(fs));
            if (!MissingTextureAtlasSprite.getLocation().equals(sprite.atlasLocation())) {

                VertexConsumer buffer = multiBuffer.getBuffer(RenderType.translucent());
                Vector4f color = RenderUtils.expandColor(renderProperties.getTintColor(fs), false);
//                float r = color.x;
//                float g = color.y;
//                float b = color.z;
                float qty = (((float) fs.getAmount()) / 1000f) - Z;

//                float v1 = sprite.getV(16 * qty);

                pose.pushPose();
                pose.scale((float) (14 * BLOCK_PIXEL), (float) (14 * BLOCK_PIXEL), (float) (14 * BLOCK_PIXEL));
                pose.translate(BLOCK_PIXEL, BLOCK_PIXEL, BLOCK_PIXEL);

                BatteryBlockEntityRenderer.renderQuad(pose, buffer, new Vector3f(0, 0, 0), new Vector3f(1, qty, 1), sprite, 14f / 16f, 14f / 16f, color, combinedLight, combinedOverlay, -1);

                /*
                Matrix4f matrix = pose.last().pose();
                Matrix3f normal = pose.last().normal();

                //top
                buffer.vertex(matrix, 0f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV1()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 1f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV1()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 1f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 0f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 1, 0).endVertex();
                //bottom
                buffer.vertex(matrix, 1f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV1()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 0f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV1()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 1f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, -1, 0).endVertex();
                //north
                buffer.vertex(matrix, 1f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU0(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 0f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU1(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 0f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 1f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, 1).endVertex();
                //south
                buffer.vertex(matrix, 1f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU0(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU1(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 0f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 1f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 0, 0, -1).endVertex();
                //east
                buffer.vertex(matrix, 1f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU0(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU1(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, 1, 0, 0).endVertex();
                //west
                buffer.vertex(matrix, 0f, qty, 1f).color(r, g, b, 1f).uv(sprite.getU0(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0f, qty, 0f).color(r, g, b, 1f).uv(sprite.getU1(), v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, 1f).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0f, 0f, 1f).color(r, g, b, 1f).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlay).uv2(combinedLight).normal(normal, -1, 0, 0).endVertex();
*/
                pose.popPose();
            }
        }
    }
}