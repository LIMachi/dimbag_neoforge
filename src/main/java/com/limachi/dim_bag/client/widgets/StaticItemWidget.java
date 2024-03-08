package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.IBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StaticItemWidget implements Layout, Renderable {

    ItemStack icon;
    int x;
    int y;

    public static class Builder implements IBuilder {
        ItemStack item = ItemStack.EMPTY;
        int x = 0;
        int y = 0;

        public StaticItemWidget build() { return new StaticItemWidget(x, y, item); }

        public Builder at(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder x(int x) { this.x = x; return this; }
        public Builder y(int y) { this.y = y; return this; }
        private Builder item(Item item) {
            CompoundTag prevTag = this.item.getTag();
            this.item = new ItemStack(item);
            if (prevTag != null)
                this.item.setTag(prevTag);
            return this;
        }
        public Builder item(ItemLike item) { return item(item.asItem()); }
        public Builder item(ItemStack item) { this.item = item; return this; }
        public Builder item(Supplier<ItemLike> item) { return item(item.get()); }
        public Builder item(RegistryObject<Item> item) { return item(item.get()); }
        public Builder tag(CompoundTag tag) { this.item.setTag(tag); return this; }
        public Builder merge(CompoundTag tag) { this.item.getOrCreateTag().merge(tag); return this; }
    }

    public static Builder builder() { return new Builder(); }

    protected StaticItemWidget(int x, int y, ItemStack stack) {
        this.x = x;
        this.y = y;
        icon = stack;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.renderItem(icon, x, y);
    }

    @Override
    public void setX(int val) { x = val; }

    @Override
    public void setY(int val) { y = val; }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getWidth() { return 16; }

    @Override
    public int getHeight() { return 16; }

    @Override
    public void visitChildren(Consumer<LayoutElement> p_270255_) {}
}
