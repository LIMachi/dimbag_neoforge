package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.menus.SlotMenu;
import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import com.limachi.lim_lib.widgets.TextEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class SlotScreen extends AbstractContainerScreen<SlotMenu> {

    private Component label;

    public SlotScreen(SlotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.empty());
        label = title;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TextEdit(font, titleLabelX + getGuiLeft() - 2, titleLabelY + getGuiTop(), imageWidth - titleLabelX - 2, 14, label.getString(), w->{
            label = Component.literal(w.getValue());
            CompoundTag out = new CompoundTag();
            out.putString("label", Component.Serializer.toJson(label));
            ScreenNBTMsg.send(0, out);
        }));
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
    }

    @Nonnull
    public static List<Component> slotTooltip(@Nonnull Slot slot, @Nonnull ItemStack stack) {
        List<Component> tooltip = getTooltipFromItem(Minecraft.getInstance(), stack);
        if (slot instanceof BagSlot bagSlot && !stack.isEmpty() && !tooltip.isEmpty()) {
            if (hasShiftDown() || stack.getCount() <= stack.getMaxStackSize())
                tooltip.set(0, ((MutableComponent) tooltip.get(0)).append(Component.translatable("screen.bag_slot.size_acronym_extended", stack.getCount(), bagSlot.getMaxStackSize(stack))));
            else
                tooltip.set(0, ((MutableComponent) tooltip.get(0)).append(Component.translatable("screen.bag_slot.size_acronym", stack.getCount() / stack.getMaxStackSize(), stack.getCount() % stack.getMaxStackSize(), bagSlot.maxSizeInStacks())));
        }
        return tooltip;
    }

    @Override
    @Nonnull
    protected List<Component> getTooltipFromContainerItem(@Nonnull ItemStack stack) {
        if (hoveredSlot != null)
            return slotTooltip(hoveredSlot, stack);
        return super.getTooltipFromContainerItem(stack);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiUtils.background(gui);
        for (Slot slot : menu.slots)
            GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, false);
    }
}
