package com.limachi.dim_bag.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ViewPortStyledWidget extends ViewPortWidget {

    protected int verticalSpacing = 3;
    protected int horizontalSpacing = 3;
    protected int wrap;
    protected boolean horizontal;

    public ViewPortStyledWidget(int x, int y, int w, int h, boolean horizontal, int wrap) {
        super(x, y, w, h);
        this.horizontal = horizontal;
        this.wrap = wrap;
    }

    protected void addObject(AbstractWidget widget, int spacerGirth) {
        int dx = 0, dy = 0;
        if (spacerGirth > 0) {
            if (horizontal) {
                widget.setHeight(wrap - verticalSpacing * 2);
                dy = verticalSpacing;
                widget.setWidth(spacerGirth);
            } else {
                widget.setWidth(wrap - horizontalSpacing * 2);
                dx = horizontalSpacing;
                widget.setHeight(spacerGirth);
            }
        }
        if (children.isEmpty()) {
            widget.setX(dx);
            widget.setY(dy);
        } else {
            if (horizontal) {
                AbstractWidget prev = children.get(children.size() - 1);
                int x = prev.getX() + prev.getWidth() + horizontalSpacing;
                if (x >= getX() + wrap) {
                    widget.setX(0);
                    widget.setY(maxY + verticalSpacing - getY());
                } else {
                    widget.setX(x - getX());
                    widget.setY(0);
                }
            } else {
                AbstractWidget prev = children.get(children.size() - 1);
                int y = prev.getY() + prev.getHeight() + verticalSpacing;
                if (y >= getY() + wrap) {
                    widget.setY(0);
                    widget.setX(maxX + horizontalSpacing - getX());
                } else {
                    widget.setY(y - getY());
                    widget.setX(0);
                }
            }
        }
        super.addWidget(widget);
    }

    public void addSpacer() { addSpacer(1, 0xFF373737); }


    public void addSpacer(int girth, int color) {
        addObject(new AbstractWidget(0, 0, 1, 1, Component.empty()) {
            @Override
            protected void renderWidget(GuiGraphics gui, int p_268034_, int p_268009_, float p_268085_) {
                gui.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput out) {}
        }, girth);
    }

    @Override
    public void addWidget(AbstractWidget widget) { addObject(widget, 0); }
}
