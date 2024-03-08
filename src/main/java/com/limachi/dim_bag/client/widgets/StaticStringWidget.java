package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.IBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

public class StaticStringWidget implements Layout, Renderable {
    Component text;
    int color;
    boolean shadow;
    int x;
    int y;
    Font font;

    public static class Builder implements IBuilder {
        Component text = Component.empty();
        int color = -1;
        boolean shadow = true;
        int x = 0;
        int y = 0;
        Font font = Minecraft.getInstance().font;

        public StaticStringWidget build() { return new StaticStringWidget(x, y, font, text, color, shadow); }

        public Builder text(Component text) { this.text = text; return this; }
        public Builder text(String text) { this.text = Component.literal(text); return this; }
        public Builder jsonText(String json) { this.text = Component.Serializer.fromJson(json); return this; }
        public Builder at(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder x(int x) { this.x = x; return this; }
        public Builder y(int y) { this.y = y; return this; }
        public Builder shadow(boolean flag) { this.shadow = flag; return this; }
        public Builder font(Font font) { this.font = font; return this; }
        public Builder color(int color) { this.color = color; return this; }
        public Builder color(int a, int r, int g, int b) { this.color = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF); return this; }
        public Builder color(double a, double r, double g, double b) { return color((int)(0xFF * a), (int)(0xFF * r), (int)(0xFF * g), (int)(0xFF * b)); }
    }

    public static Builder builder() { return new Builder(); }

    protected StaticStringWidget(int x, int y, Font font, Component text, int color, boolean shadow) {
        this.x = x;
        this.y = y;
        this.font = Objects.requireNonNullElseGet(font, ()->Minecraft.getInstance().font);
        this.text = Objects.requireNonNullElseGet(text, Component::empty);
        this.color = color;
        this.shadow = shadow;
    }

    @Override
    public void render(GuiGraphics gui, int p_253973_, int p_254325_, float p_254004_) {
        gui.drawString(font, text, x, y, color, shadow);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> p_270255_) {}

    @Override
    public void setX(int val) { x = val; }

    @Override
    public void setY(int val) { y = val; }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getWidth() { return font.width(text); }

    @Override
    public int getHeight() { return font.lineHeight; }
}
