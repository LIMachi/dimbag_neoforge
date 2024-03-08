package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.client.widgets.*;
import com.limachi.dim_bag.entities.utils.Actuator;
import com.limachi.dim_bag.menus.ParasiteMenu;
import com.limachi.dim_bag.utils.Tags;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class ParasiteScreen extends AbstractContainerScreen<ParasiteMenu> {

    public ParasiteScreen(ParasiteMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }

    private VerticalSlider slider = null;

    @Override
    protected void init() {
        super.init();
        final int innerWidth = imageWidth - 16;
        final TextEdit path = new TextEditWithSuggestions(font, 8 + getGuiLeft(), 18 + getGuiTop(), innerWidth, 16, menu.command, t->{
            menu.command = t.getValue();
        }, Actuator.command_suggestions);
        addRenderableWidget(path);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (getFocused() instanceof ICatchEsc c)
            return !c.catchEsc();
        return true;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blitNineSliced(GuiUtils.BACKGROUND_TEXTURE, getGuiLeft(), getGuiTop(), imageWidth, imageHeight, 8, 256, 256, 0, 0);
    }

    @Override
    public void onClose() {
        ScreenNBTMsg.send(0, Tags.singleton("command", menu.command));
        super.onClose();
    }
}
