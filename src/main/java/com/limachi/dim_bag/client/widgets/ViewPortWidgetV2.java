package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.IBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public abstract class ViewPortWidgetV2<T extends ViewPortWidgetV2> extends AbstractWidget {

    protected Matrix4f invertedTransform = new Matrix4f();
    protected Matrix4f transform = new Matrix4f();
    protected ArrayList<NarratableEntry> narratableChildren = new ArrayList<>();
    protected ArrayList<GuiEventListener> children = new ArrayList<>();
    protected ArrayList<Renderable> renderableChildren = new ArrayList<>();
    protected int minX = Integer.MAX_VALUE;
    protected int minY = Integer.MAX_VALUE;
    protected int maxX = Integer.MIN_VALUE;
    protected int maxY = Integer.MIN_VALUE;

    public ViewPortWidgetV2(int x, int y, int w, int h) {
        super(x, y, w, h, Component.empty());
        resetTransform();
    }

    public float getInnerWidth() {
        if (minX == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || minY == Integer.MAX_VALUE || maxY == Integer.MIN_VALUE)
            return 0;
        Vector4f min = new Vector4f(minX, minY, 0, 0);
        Vector4f max = new Vector4f(maxX, maxY, 0, 0);
        transform.transform(min);
        transform.transform(max);
        return Math.max(0, max.x - min.x);
    }

    public float getInnerHeight() {
        if (minX == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || minY == Integer.MAX_VALUE || maxY == Integer.MIN_VALUE)
            return 0;
        Vector4f min = new Vector4f(minX, minY, 0, 0);
        Vector4f max = new Vector4f(maxX, maxY, 0, 0);
        transform.transform(min);
        transform.transform(max);
        return Math.max(0, max.y - min.y);
    }

    public T zoom(float factor) {
        if (factor > 0) {
            transform.scale(factor);
            transform.invertAffine(invertedTransform);
        }
        return (T)this;
    }

    public T zoomTo(float scale) {
        if (scale > 0) {
            Vector3f prev = new Vector3f();
            transform.getScale(prev);
            transform.scale(scale / prev.x, scale / prev.y, scale / prev.z);
            transform.invertAffine(invertedTransform);
        }
        return (T)this;
    }

    public T move(float dx, float dy) {
        transform.translate(dx, dy, 0);
        transform.invertAffine(invertedTransform);
        return (T)this;
    }

    public T moveTo(float x, float y) {
        transform.set(3, 0, x + getX());
        transform.set(3, 1, y + getY());
        transform.invertAffine(invertedTransform);
        return (T)this;
    }

    public T rotate(float rad) {
        transform.rotate(rad, new Vector3f(0, 0, 1));
        transform.invertAffine(invertedTransform);
        return (T)this;
    }

    @Override
    public void setX(int val) {
        if (getX() != val) {
            transform.set(3, 0, val);
            transform.invertAffine(invertedTransform);
            super.setX(val);
        }
    }

    @Override
    public void setY(int val) {
        if (getY() != val) {
            transform.set(3, 1, val);
            transform.invertAffine(invertedTransform);
            super.setY(val);
        }
    }

    public T resetTransform() {
        transform = new Matrix4f();
        transform.translate(getX(), getY(), 0);
        transform.invertAffine(invertedTransform);
        return (T)this;
    }

    public T addWidget(Object widget) {
        while (widget instanceof IBuilder builder)
            widget = builder.build();
        if (widget instanceof Renderable renderable)
            renderableChildren.add(renderable);
        if (widget instanceof NarratableEntry narration)
            narratableChildren.add(narration);
        if (widget instanceof GuiEventListener listener)
            children.add(listener);
        if (widget instanceof LayoutElement layout) {
            minX = Math.min(minX, layout.getX());
            minY = Math.min(minY, layout.getY());
            maxX = Math.max(maxX, layout.getX() + layout.getWidth());
            maxY = Math.max(maxY, layout.getY() + layout.getHeight());
        }
        return (T)this;
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (renderableChildren.isEmpty())
            return;
        gui.pose().pushPose();
        Matrix4f prev = gui.pose().last().pose();
        Vector4f topLeft = new Vector4f(getX(), getY(), 0, 1);
        Vector4f bottomRight = new Vector4f(getX() + getWidth() + 1, getY() + getHeight() + 1, 0, 1);
        prev.transform(topLeft);
        prev.transform(bottomRight);
        gui.enableScissor((int)topLeft.x, (int)topLeft.y, (int)bottomRight.x, (int)bottomRight.y);
        gui.pose().mulPoseMatrix(transform);
        if (!clicked(mouseX, mouseY)) {
            mouseX = Integer.MIN_VALUE;
            mouseY = Integer.MIN_VALUE;
        } else {
            Vector4f mouse = new Vector4f(mouseX, mouseY, 1, 1);
            invertedTransform.transform(mouse);
            mouseX = (int)mouse.x;
            mouseY = (int)mouse.y;
        }
        for (Renderable child : renderableChildren)
            child.render(gui, mouseX, mouseY, partialTick);
        gui.disableScissor();
        gui.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narratorOutput) {
        for (NarratableEntry child : narratableChildren)
            child.updateNarration(narratorOutput);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!children.isEmpty() && clicked(mouseX, mouseY)) {
            Vector4f mouse = new Vector4f((float)mouseX, (float)mouseY, 1, 1);
            invertedTransform.transform(mouse);
            mouseX = mouse.x;
            mouseY = mouse.y;
            for (GuiEventListener child : children)
                child.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (!children.isEmpty() && clicked(mouseX, mouseY)) {
            Vector4f mouse = new Vector4f((float)mouseX, (float)mouseY, 1, 1);
            invertedTransform.transform(mouse);
            mouseX = mouse.x;
            mouseY = mouse.y;
            for (GuiEventListener child : children)
                child.mouseScrolled(mouseX, mouseY, scroll);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!children.isEmpty() && clicked(mouseX, mouseY)) {
            Vector4f mouse = new Vector4f((float)mouseX, (float)mouseY, 1, 1);
            invertedTransform.transform(mouse);
            mouseX = mouse.x;
            mouseY = mouse.y;
            for (GuiEventListener child : children)
                child.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!children.isEmpty() && clicked(mouseX, mouseY)) {
            Vector4f mouse = new Vector4f((float)mouseX, (float)mouseY, 1, 1);
            invertedTransform.transform(mouse);
            mouseX = mouse.x;
            mouseY = mouse.y;
            for (GuiEventListener child : children)
                child.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener child : children)
            if (child.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener child : children)
            if (child.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return false;
    }

    @Override
    public boolean charTyped(char ascii, int modifiers) {
        for (GuiEventListener child : children)
            if (child.charTyped(ascii, modifiers))
                return true;
        return false;
    }
}
