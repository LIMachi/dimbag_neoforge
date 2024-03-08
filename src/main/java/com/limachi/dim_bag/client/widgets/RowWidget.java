package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.IBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.function.Consumer;

public class RowWidget extends ViewPortWidgetV2 {

    public class RowObjectSpacer implements Renderable, LayoutElement {
        public int color;
        public int x;
        public int girth;
        public RowObjectSpacer(int x, int girth, int color) {
            this.x = x;
            this.girth = girth;
            this.color = color;
        }

        @Override
        public void render(GuiGraphics gui, int p_253973_, int p_254325_, float p_254004_) {
            gui.fill(x, spacing, x + girth, height - spacing * 2, color);
        }

        @Override
        public void setX(int val) { x = val; }

        @Override
        public void setY(int val) {}

        @Override
        public int getX() { return x; }

        @Override
        public int getY() { return 0; }

        @Override
        public int getWidth() { return girth; }

        @Override
        public int getHeight() { return height; }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> runner) {}
    }

    int spacing;

    public RowWidget(int x, int y, int w, int h, int spacing) {
        super(x, y, w, h);
        this.spacing = spacing;
    }

    @Override
    public RowWidget addWidget(Object widget) {
        while (widget instanceof IBuilder builder)
            widget = builder.build();
        if (widget instanceof LayoutElement layout) {
            if (maxX != Integer.MIN_VALUE)
                layout.setX(maxX + spacing);
            else
                layout.setX(0);
        }
        super.addWidget(widget);
        return this;
    }

    public RowWidget addSpacer() { return addSpacer(1, 0xFF373737); }

    public RowWidget addSpacer(int girth, int color) { return addWidget(new RowObjectSpacer(0, girth, color)); }
}
