package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.bag_modes.ModesRegistry;
import com.limachi.dim_bag.items.bag_modes.SettingsMode;
import com.limachi.dim_bag.items.bag_modes.TankMode;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.dim_bag.menus.slots.BagTankSlot;
import com.limachi.dim_bag.save_datas.bag_data.BagInstance;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import com.limachi.lim_lib.utils.Tags;
import com.limachi.lim_lib.widgets.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class BagScreen extends AbstractContainerScreen<BagMenu> {

    protected final ItemStack inventoryIcon;
    protected final ItemStack settingsIcon;
    protected final ItemStack tanksIcon;

    public static final ResourceLocation BACKGROUND = new ResourceLocation(DimBag.MOD_ID, "textures/screen/bag_inventory.png");
    public static final ResourceLocation ENERGY_BAR = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/energy.png");
    public static final ResourceLocation FLUID_SLOT_OVERLAY = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/fluid_slot_overlay.png");
    public static final ResourceLocation SELECTED_FLUID_SLOT = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/selected_fluid_slot_overlay.png");
    public static final int BACKGROUND_WIDTH = 218;
    public static final int BACKGROUND_HEIGHT = 184;

    protected VerticalSlider slider;
    protected Component name;

    protected static final int settingsScrollFactor = 16;
    protected ColumnWidget settingsWidgets = null;
    protected double localScroll = 0.;

    public BagScreen(BagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = BACKGROUND_WIDTH;
        imageHeight = BACKGROUND_HEIGHT;
        titleLabelX = 30;
        inventoryLabelX = 30;
        inventoryLabelY = 90;
        inventoryIcon = new ItemStack(BagItem.R_ITEM.get());
        settingsIcon = inventoryIcon.copy();
        settingsIcon.getOrCreateTag().putString(BagItem.BAG_MODE_OVERRIDE, SettingsMode.NAME);
        tanksIcon = inventoryIcon.copy();
        tanksIcon.getOrCreateTag().putString(BagItem.BAG_MODE_OVERRIDE, TankMode.NAME);
        name = menu.settingsData.contains("label") ? Component.Serializer.fromJson(menu.settingsData.getString("label")) : BagItem.create(0).getDisplayName();
    }

    protected RowWidget createModeToggle(String mode) {
        RowWidget out = new RowWidget(0, 0, imageWidth - 60, 20, 3);
        out.addWidget(StaticItemWidget.builder().item(BagItem.R_ITEM).tag(Tags.singleton(BagItem.BAG_MODE_OVERRIDE, mode)));
        out.addWidget(StaticStringWidget.builder().text(mode).color(4210752).shadow(false).y(5));
        out.addWidget(new Checkbox(0, 0, 20, 20, Component.empty(), BagInstance.isModeEnabled(menu.settingsData, mode)) {
            @Override
            public void onPress() {
                super.onPress();
                BagInstance.setEnabledMode(menu.settingsData, mode, selected());
            }
        });
//        out.zoom(2);
        return out;
    }

    @Override
    protected void init() {
        super.init();
        slider = new VerticalSlider(195 + getGuiLeft(), 15 + getGuiTop(), 16, 105, localScroll, this::sliderUpdate);
        addRenderableWidget(slider);
        if (menu.page.get() >= 2) {
            addRenderableWidget(new TextEdit(font, titleLabelX + getGuiLeft(), titleLabelY + getGuiTop(), imageWidth - titleLabelX * 2, 12, name.getString(), t->name = Component.literal(t.getValue())));
            settingsWidgets = new ColumnWidget(getGuiLeft() + 30, getGuiTop() + 20, imageWidth - 60, imageHeight - 50, 3);
            settingsWidgets.addWidget(new BooleanTextButton(0, 0, imageWidth - 60, 14, Component.translatable("screen.bag.button.quick_equip"), Component.translatable("screen.bag.button.normal_equip"), menu.settingsData.getBoolean("quick_equip"), b->menu.settingsData.putBoolean("quick_equip", b.getState())));
            settingsWidgets.addWidget(new BooleanTextButton(0, 0, imageWidth - 60, 14, Component.translatable("screen.bag.button.quick_enter"), Component.translatable("screen.bag.button.normal_unequip"), menu.settingsData.getBoolean("quick_enter"), b->menu.settingsData.putBoolean("quick_enter", b.getState())));
            settingsWidgets.addSpacer();
            settingsWidgets.addWidget(new Checkbox(0, 32, imageWidth - 60, 20, Component.literal("replace_inv"), menu.settingsData.getBoolean("bag_instead_of_inventory")){
                @Override
                public void onPress() {
                    super.onPress();
                    menu.settingsData.putBoolean("bag_instead_of_inventory", selected());
                }
            });
            settingsWidgets.addSpacer();
            for (ModesRegistry.ModeEntry mode : ModesRegistry.modesList)
                if (mode.mode().canDisable())
                    settingsWidgets.addWidget(createModeToggle(mode.name()));
//                    settingsWidgets.addWidget(new Checkbox(0, 0, imageWidth - 60, 14, Component.literal(mode.name()), BagInstance.isModeEnabled(menu.settingsData, mode.name())){
//                        @Override
//                        public void onPress() {
//                            super.onPress();
//                            BagInstance.setEnabledMode(menu.settingsData, mode.name(), selected());
//                        }
//                    });
//            settingsWidgets.zoom(0.5f);
            settingsWidgets.moveTo(0, -Math.round(Math.max(0, settingsWidgets.getInnerHeight() - settingsWidgets.getHeight()) * localScroll));
            addRenderableWidget(settingsWidgets);
        }
    }

    @Override
    public void onClose() {
        ScreenNBTMsg.send(1, menu.settingsData);
        super.onClose();
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics gui, int mouseX, int mouseY) {
        if (menu.page.get() < 2) {
            gui.drawString(font, name, titleLabelX, titleLabelY, 4210752, false);
            gui.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
            long energy = menu.energy.getLong();
            long maxEnergy = menu.maxEnergy.getLong();
            int empty;
            if (maxEnergy <= 0)
                empty = 79;
            else
                empty = (int)Math.round(79. * Mth.clamp(1. - (double)energy / (double)maxEnergy, 0., 1.));
            if (empty > 0)
                gui.fill(imageWidth - 29 - empty, inventoryLabelY, imageWidth - 29, inventoryLabelY + 9, 0xFF000000);
            String er = energy < 1000 ? "" + energy : energy < 1000_000 ? energy / 1000 + "K" : energy < 1000_000_000 ? energy / 1000_000 + "M" : energy < 1000_000_000_000L ? energy / 1000_000_000 + "G" : energy / 1000_000_000_000L + "T";
            String em = maxEnergy < 1000 ? "" + maxEnergy : maxEnergy < 1000_000 ? maxEnergy / 1000 + "K" : maxEnergy < 1000_000_000 ? maxEnergy / 1000_000 + "M" : maxEnergy < 1000_000_000_000L ? maxEnergy / 1000_000_000 + "G" : maxEnergy / 1000_000_000_000L + "T";
            gui.drawString(font, Component.literal("FE: " + er + " / " + em), imageWidth - 107, inventoryLabelY + 1, -1);
        }
    }

//    @Override
//    public void renderSlot(@Nonnull GuiGraphics gui, @Nonnull Slot slot) {
//        if (slot instanceof TankSlot tank)
//            tank.renderSlot(gui);
//        else
//            super.renderSlot(gui, slot);
//    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double fromX, double fromY) {
        return ((getFocused() != null && isDragging() && button == 0) && getFocused().mouseDragged(mouseX, mouseY, button, fromX, fromY)) || super.mouseDragged(mouseX, mouseY, button, fromX, fromY);
    }

    protected void sliderUpdate(VerticalSlider slider) {
        if (menu.page.get() < 2) {
            CompoundTag out = new CompoundTag();
            out.putDouble("scroll", slider.getValue());
            ScreenNBTMsg.send(0, out);
        } else {
            localScroll = slider.getValue();
            if (settingsWidgets != null)
                settingsWidgets.moveTo(0, -Math.round(Math.max(0, settingsWidgets.getInnerHeight() - settingsWidgets.getHeight()) * localScroll));
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        for (Slot slot : menu.slots)
            if (slot instanceof BagTankSlot bts && slot.isActive() && bts.selected())
                gui.blit(SELECTED_FLUID_SLOT, slot.x - 1 + getGuiLeft(), slot.y - 1 + getGuiTop(), 100, 0, 0, 18, 18, 18, 18);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    @Nonnull
    protected List<Component> getTooltipFromContainerItem(@Nonnull ItemStack stack) {
        if (hoveredSlot != null)
            return SlotScreen.slotTooltip(hoveredSlot, stack);
        return super.getTooltipFromContainerItem(stack);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blit(BACKGROUND, getGuiLeft(), getGuiTop(), 0f, 0F, imageWidth, imageHeight, 256, 256);
        if (menu.page.get() == 0) {
            gui.blit(BACKGROUND, getGuiLeft(), getGuiTop() + 157, 0, imageHeight, 24, 23, 256, 256);
            for (Slot slot : menu.slots)
                if (slot instanceof BagSlot && !slot.isActive())
                    GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, true);
        } else if (menu.page.get() == 1) {
            gui.blit(BACKGROUND, getGuiLeft(), getGuiTop() + 131, 0, imageHeight, 24, 23, 256, 256);
            for (Slot slot : menu.slots)
                if (slot instanceof BagTankSlot && !slot.isActive()) {
                    GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, true);
                    gui.blit(FLUID_SLOT_OVERLAY, slot.x - 1 + getGuiLeft(), slot.y - 1 + getGuiTop(), 0, 0, 18, 18, 16, 16);
                }
        }
        else if (menu.page.get() == 2) {
            gui.blit(BACKGROUND, getGuiLeft() + 194, getGuiTop() + 157, 194, imageHeight, 24, 23, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 15, 28, 184, 162, 72, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 15 + 72, 28, 184, 162, 72, 256, 256);
        }
        else if (menu.page.get() == 3) {
            gui.blit(BACKGROUND, getGuiLeft() + 194, getGuiTop() + 131, 194, imageHeight, 24, 23, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 15, 28, 184, 162, 72, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 15 + 72, 28, 184, 162, 72, 256, 256);
        }
        gui.renderItem(inventoryIcon, getGuiLeft() + 4, getGuiTop() + 161);
        gui.renderItem(tanksIcon, getGuiLeft() + 4, getGuiTop() + 135);
        gui.renderItem(settingsIcon, getGuiLeft() + 198, getGuiTop() + 161);
        gui.renderItem(settingsIcon, getGuiLeft() + 198, getGuiTop() + 135);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - getGuiLeft();
        double y = mouseY - getGuiTop();
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 && menu.page.get() == 1) {
            for (int i = 36; i < 72; ++i) {
                Slot slot = menu.slots.get(i);
                if (slot.isActive() && x >= slot.x && x <= slot.x + 16 && y >= slot.y && y <= slot.y + 16) {
                    CompoundTag out = new CompoundTag();
                    out.putInt("select", i - 36);
                    ScreenNBTMsg.send(0, out);
                    return true;
                }
            }
        }
        if (y >= 131 && y <= 179) {
            if (x >= 0 && x <= 21) {
                if (y <= 154)
                    menu.page.set(1);
                if (y >= 157)
                    menu.page.set(0);
                CompoundTag out = new CompoundTag();
                out.putInt("page", menu.page.get());
                slider.setValue(0.);
                localScroll = 0.;
                ScreenNBTMsg.send(0, out);
                rebuildWidgets();
                return true;
            } else if (x >= 198 && x <= 218) {
                if (y <= 154)
                    menu.page.set(3);
                if (y >= 157)
                    menu.page.set(2);
                CompoundTag out = new CompoundTag();
                out.putInt("page", menu.page.get());
                slider.setValue(0.);
                localScroll = 0.;
                ScreenNBTMsg.send(0, out);
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
