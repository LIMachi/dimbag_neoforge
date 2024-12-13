package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.menus.SensorMenu;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import com.limachi.lim_lib.widgets.Builders;
import com.limachi.lim_lib.widgets.ICatchEsc;
import com.limachi.lim_lib.widgets.TextEdit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class SensorScreen extends AbstractContainerScreen<SensorMenu> {

    public SensorScreen(SensorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TextEdit(font, 10 + getGuiLeft(), 10 + getGuiTop(), 156, 16, Component.Serializer.fromJson(menu.data.getString("label")).getString(), s->menu.data.putString("label", Component.Serializer.toJson(Component.literal(s.getValue())))));
        Builders.checkList(this, 10 + getGuiLeft(), 30 + getGuiTop(), 156, 86, List.of(Component.literal("test 1"), Component.literal("test 2")), List.of(true, false), c->{});
//        addRenderableWidget(new BooleanTextButton(10 + getGuiLeft(), 30 + getGuiTop(), 156, 16, WHITELIST, BLACKLIST, menu.data.getBoolean("white_list"), b->menu.data.putBoolean("white_list", b.getState())));
//        addRenderableWidget(new BooleanTextButton(10 + getGuiLeft(), 50 + getGuiTop(), 156, 16, AFFECT_PLAYER, DONT_AFFECT_PLAYER, menu.data.getBoolean("affect_players"), b->menu.data.putBoolean("affect_players", b.getState())));
//        ArrayList<String> filters = new ArrayList<>();
//        for (Tag t : menu.data.getList("filters", Tag.TAG_STRING))
//            if (t instanceof StringTag s)
//                filters.add(s.getAsString());
//        Builders.editableTextList(this, 10 + getGuiLeft(), 70 + getGuiTop(), 156, 86, filters, l->{
//            ListTag list = new ListTag();
//            for (String entry : l.getEntries())
//                list.add(StringTag.valueOf(entry));
//            menu.data.put("filters", list);
//        });
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double fromX, double fromY) {
        return ((getFocused() != null && isDragging() && button == 0) && getFocused().mouseDragged(mouseX, mouseY, button, fromX, fromY)) || super.mouseDragged(mouseX, mouseY, button, fromX, fromY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (getFocused() instanceof ICatchEsc c)
            return !c.catchEsc();
        return true;
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics gui, int mouseX, int mouseY) {}

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiUtils.background(gui);
    }

    @Override
    public void onClose() {
        menu.close();
        super.onClose();
    }
}
