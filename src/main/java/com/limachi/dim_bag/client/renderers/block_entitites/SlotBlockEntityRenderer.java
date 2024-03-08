package com.limachi.dim_bag.client.renderers.block_entitites;

import com.limachi.dim_bag.block_entities.bag_modules.SlotModuleBlockEntity;
import com.limachi.dim_bag.client.CustomItemStackRenderer;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@StaticInitClient
public class SlotBlockEntityRenderer implements BlockEntityRenderer<SlotModuleBlockEntity> {

    static {
        ClientRegistries.setBer(SlotModuleBlockEntity.R_TYPE, SlotBlockEntityRenderer::new);
    }

    public static final float ITEM_SCALE = 0.90f;
    public static final float BLOCK_SCALE = 1.75f;

    public static final double CENTER = 0.5d;

    protected final BlockEntityRendererProvider.Context ctx;

    protected SlotBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(@Nonnull SlotModuleBlockEntity be, float f, @Nonnull PoseStack pose, @Nonnull MultiBufferSource multiBuffer, int combinedLight, int combinedOverlay) {
        Level level = be.getLevel();
        boolean gui = level == null;
        BlockPos pos = be.getBlockPos();
        if (gui)
            level = Minecraft.getInstance().level;
        ctx.getBlockRenderDispatcher().getModelRenderer().tesselateBlock(level, ctx.getBlockRenderDispatcher().getBlockModel(be.getBlockState()), be.getBlockState(), pos, pose, multiBuffer.getBuffer(RenderType.cutoutMipped()), false, RandomSource.create(), be.getBlockState().getSeed(pos), combinedOverlay);
        ItemStack stack = be.renderStack;
        if (!stack.isEmpty() && Sides.getPlayer() instanceof LocalPlayer player) {
            pose.pushPose();
            pose.translate(CENTER, CENTER, CENTER);
            if (gui) {
                if (!(stack.getItem() instanceof BlockItem)) {
                    pose.mulPose(Axis.YP.rotationDegrees(-45));
                    pose.mulPose(Axis.XP.rotationDegrees(30));
                }
                else if (CustomItemStackRenderer.currentItemDisplayContext.firstPerson())
                    pose.mulPose(Axis.YP.rotationDegrees(180));
            } else {
                double dx = player.position().x - pos.getX() - 0.5;
                double dz = player.position().z - pos.getZ() - 0.5;
                if (!KeyMapController.SNEAK.getState(player)) {
                    double dy = player.position().y - pos.getY() - 0.5 + player.getEyeHeight();
                    pose.mulPose(Axis.YP.rotation((float) Math.atan2(dx, dz)));
                    pose.mulPose(Axis.XP.rotation(-(float) Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
                } else {
                    double test = Math.round((Math.atan2(dx, dz) * 180. / Math.PI) / 90);
                    pose.mulPose(Axis.YP.rotationDegrees((float) test * 90));
                }
                pose.mulPose(Axis.YP.rotationDegrees(180));
            }
            if (ctx.getItemRenderer().getModel(stack, null, null, 0).isGui3d())
                pose.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
            else
                pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            ctx.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, pose, multiBuffer, be.getLevel(), 0);
            pose.popPose();
        }
    }
}
