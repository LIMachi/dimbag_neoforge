package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.menus.SculkMenu;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class SculkScreen extends AbstractContainerScreen<SculkMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(DimBag.MOD_ID, "textures/screen/sculk_module.png");

    public SculkScreen(SculkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.empty());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        super.renderLabels(p_281635_, p_282681_, p_283686_);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blit(BACKGROUND, getGuiLeft(), getGuiTop(), 0f, 0F, imageWidth, imageHeight, 256, 256);
    }
}
