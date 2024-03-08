package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.IBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.function.Consumer;

public class ColumnWidget extends ViewPortWidgetV2 {

    public class ColumnObjectSpacer implements Renderable, LayoutElement {
        public int color;
        public int y;
        public int girth;
        public ColumnObjectSpacer(int y, int girth, int color) {
            this.y = y;
            this.girth = girth;
            this.color = color;
        }

        @Override
        public void render(GuiGraphics gui, int p_253973_, int p_254325_, float p_254004_) {
            gui.fill(spacing, y, width - spacing * 2, y + girth, color);
        }

        @Override
        public void setX(int val) {}

        @Override
        public void setY(int val) { y = val; }

        @Override
        public int getX() { return 0; }

        @Override
        public int getY() { return y; }

        @Override
        public int getWidth() { return width; }

        @Override
        public int getHeight() { return girth; }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> runner) {}
    }

    int spacing;

    public ColumnWidget(int x, int y, int w, int h, int spacing) {
        super(x, y, w, h);
        this.spacing = spacing;
    }

    @Override
    public ColumnWidget addWidget(Object widget) {
        while (widget instanceof IBuilder builder)
            widget = builder.build();
        if (widget instanceof LayoutElement layout) {
            if (maxY != Integer.MIN_VALUE)
                layout.setY(maxY + spacing);
            else
                layout.setY(0);
        }
        super.addWidget(widget);
        return this;
    }

    public ColumnWidget addSpacer() { return addSpacer(1, 0xFF373737); }

    public ColumnWidget addSpacer(int girth, int color) { return addWidget(new ColumnObjectSpacer(0, girth, color)); }
}
