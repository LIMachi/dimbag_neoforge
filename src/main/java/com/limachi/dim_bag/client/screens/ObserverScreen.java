package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.entities.utils.TagOperation;
import com.limachi.dim_bag.menus.ObserverMenu;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import com.limachi.lim_lib.widgets.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class ObserverScreen extends AbstractContainerScreen<ObserverMenu> {

    public ObserverScreen(ObserverMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    /*
    protected Component getLineComponent(int line) {
        CompoundTag entry = menu.commands.getCompound(line);
        String kind = entry.getString("kind");
        switch (kind) {
            case "obs" -> { return Component.literal(entry.getString("mtd") + " * " + entry.getDouble("mul")); }
            case "op" -> { return Component.translatable("screen.observer.button.line").append(": " + entry.getInt("a")).append(" " + entry.getString("op") + " ").append(Component.translatable("screen.observer.button.line")).append(": " + entry.getInt("b")); }
            case "lit" -> { return Component.translatable("screen.observer.button.value").append(": " + entry.getDouble("val")); }
            default -> { return Component.empty(); }
        }
    }

    protected static String cycleKind(String kind) {
        return switch (kind) {
            case "obs" -> "op";
            case "op" -> "lit";
            default -> "obs";
        };
    }

    protected static class LinePopUp extends Screen {

        protected final ObserverScreen parent;
        protected final CompoundTag entry;
        protected final PopUpScreenButton creator;
        protected ScreenRectangle background;
        protected final int line;
        protected int deltaH = 0;

        protected LinePopUp(ObserverScreen parent, PopUpScreenButton creator, int line) {
            super(Component.empty());
            if (line >= parent.menu.commands.size()) {
                line = parent.menu.commands.size();
                parent.menu.commands.add(new CompoundTag());
            }
            this.line = line;
            entry = parent.menu.commands.getCompound(line);
            this.parent = parent;
            this.creator = creator;
        }

        @Override
        protected void init() {
            background = new ScreenRectangle(creator.getX() + creator.getWidth() - 80, creator.getY() - 4, 200, 50);
            if (!entry.contains("kind"))
                entry.putString("kind", "obs");
            String kind = entry.getString("kind");
            addRenderableWidget(Button.builder(Component.translatable("screen.observer.button.kind", Component.translatable("screen.observer.button.kind." + kind)), b->{
                entry.putString("kind", cycleKind(entry.getString("kind")));
                rebuildWidgets();
            }).bounds(background.left() + 5, background.top() + 5, background.width() - 26, 12).build());
            TextAndImageButton remove = TextAndImageButton.builder(Component.empty(), Builders.WIDGETS, b->{parent.menu.commands.remove(line); onClose();}).texStart(Builders.CHECK_MARK_X, 0).yDiffTex(0).textureSize(256, 256).usedTextureSize(Builders.CHECK_MARK_WIDTH, Builders.CHECK_MARK_HEIGHT).offset(0, 2).build();
            remove.setX(background.left() + background.width() - 18);
            remove.setY(background.top() + 5);
            remove.setWidth(12);
            remove.setHeight(12);
            addRenderableWidget(remove);
            deltaH = 0;
            switch (kind) {
                default -> {
                    if (!entry.contains("mtd"))
                        entry.putString("mtd", "onGround");
                    if (!entry.contains("mul"))
                        entry.putDouble("mul", 1.);
                    addRenderableWidget(new TextEditWithSuggestions(font, background.left() + 5, background.top() + 19, background.width() - 10, 12, entry.getString("mtd"), w->{entry.putString("mtd", w.getValue()); parent.rebuildWidgets();}, EntityObserver.COMBINED_GETTERS).forceSuggestion(true));
                    addRenderableWidget(new TextEdit(font, background.left() + 5, background.top() + 33, background.width() - 10, 12, "" + entry.getDouble("mul"), w->{
                        try {
                            entry.putDouble("mul", Double.parseDouble(w.getValue()));
                        } catch (Exception ignore) {
                            entry.putDouble("mul", 0.);
                        }
                    }));
                }
                case "op" -> {
                    if (!entry.contains("op"))
                        entry.putString("op", "+");
                    if (!entry.contains("a"))
                        entry.putInt("a", line == 0 ? 1 : 0);
                    if (!entry.contains("b"))
                        entry.putInt("b", line == 0 ? 1 : 0);
                    addRenderableWidget(new TextEditWithSuggestions(font, background.left() + 5, background.top() + 19, background.width() - 10, 12, entry.getString("op"), w->{entry.putString("op", w.getValue()); parent.rebuildWidgets();}, EntityObserver.DOUBLE_OPERATIONS.keySet()).forceSuggestion(true));
                    ArrayList<String> lines = new ArrayList<>();
                    for (int i = 0; i < 8; ++i)
                        if (i != line)
                            lines.add("" + i);
                    int m = ((background.width() - 10) / 2 - 1) / 2;
                    addRenderableWidget(new TextEditWithSuggestions(font, background.left() + 5 + 25, background.top() + 33, m, 12, "" + entry.getInt("a"), w->{
                        try {
                            entry.putInt("a", Integer.parseInt(w.getValue()));
                        } catch (Exception ignore) {
                            entry.putInt("a", 0);
                        }
                    }, lines).forceSuggestion(true));
                    addRenderableWidget(new TextEditWithSuggestions(font, background.left() + 6 + 25 + (background.width() - 10) / 2, background.top() + 33, m, 12, "" + entry.getInt("b"), w->{
                        try {
                            entry.putInt("b", Integer.parseInt(w.getValue()));
                        } catch (Exception ignore) {
                            entry.putInt("b", 0);
                        }
                    }, lines).forceSuggestion(true));
                }
                case "lit" -> {
                    addRenderableWidget(new TextEdit(font, background.left() + 5, background.top() + 19, background.width() - 10, 12, "" + entry.getDouble("val"), w->{
                        try {
                            entry.putDouble("val", Double.parseDouble(w.getValue()));
                        } catch (Exception ignore) {
                            entry.putDouble("val", 0.);
                        }
                    }));
                    deltaH = -12;
                }
            }
        }


        @Override
        public void onClose() {
            String kind = entry.getString("kind");
            if (!"op".equals(kind)) {
                entry.remove("a");
                entry.remove("b");
                entry.remove("op");
            }
            if (!"lit".equals(kind))
                entry.remove("val");
            if (!"obs".equals(kind)) {
                entry.remove("mtd");
                if (!"rs".equals(kind))
                    entry.remove("mul");
            }
            if (!"rs".equals(kind))
                entry.remove("side");
            parent.rebuildWidgets();
            super.onClose();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button))
                return true;
            if (mouseX < background.left() || mouseX > background.right() || mouseY < background.top() || mouseY > background.bottom()) {
                onClose();
                return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (super.keyPressed(keyCode, scanCode, modifiers))
                return true;
            if (keyCode == InputConstants.KEY_DELETE || keyCode == InputConstants.KEY_BACKSPACE) {
                parent.menu.commands.remove(line);
                onClose();
                return true;
            }
            if (keyCode == InputConstants.KEY_RETURN) {
                onClose();
                return true;
            }
            return false;
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            gui.blitNineSliced(GuiUtils.BACKGROUND_TEXTURE, background.left(), background.top(), background.width(), background.height() + deltaH, 8, 256, 256, 0, 0);
            if ("op".equals(entry.getString("kind"))) {
                gui.drawString(Minecraft.getInstance().font, Component.translatable("screen.observer.button.line").append(":"), background.left() + 5, background.top() + 35, -1);
                gui.drawString(Minecraft.getInstance().font, Component.translatable("screen.observer.button.line").append(":"), background.left() + 6 + (background.width() - 10) / 2, background.top() + 35, -1);
            }
            super.render(gui, mouseX, mouseY, partialTick);
        }
    }
     */

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }

    private VerticalSlider slider = null;

    @Override
    protected void init() {
        super.init();
        final int innerWidth = imageWidth - 16;
        final int innerHeight = imageHeight - 56;
        final TextEdit path = new TextEdit(font, 8 + getGuiLeft(), 18 + getGuiTop(), innerWidth, 16, menu.command.getString("path"), t->{
            menu.command.putString("path", t.getValue());
            if (slider != null)
                slider.setValue(0);
            rebuildWidgets();
        });
        addRenderableWidget(path);
        final TextEditWithSuggestions operator = new TextEditWithSuggestions(font, 8 + getGuiLeft(), 34 + getGuiTop(), innerWidth / 5, 16, menu.command.getString("operator"), t->menu.command.putString("operator", t.getValue()), "regex", "==", "!=", "+", "-", "*", "/", "%", "<", ">", "<=", ">=");
        operator.moveSuggestionArea(operator.getX() - operator.getWidth(), operator.getY());
        addRenderableWidget(operator);
        final TextEdit operand = new TextEdit(font, 8 + getGuiLeft() + innerWidth / 5 + 2, 34 + getGuiTop(), innerWidth / 5 * 4 - 2, 16, menu.command.getString("operand"), t->menu.command.putString("operand", t.getValue()));
        addRenderableWidget(operand);
        final ViewPortWidget suggestions = new ViewPortWidget(8 + getGuiLeft(), 52 + getGuiTop(), innerWidth - 16, innerHeight);
        int offset = 0;
        int longest = 0;
        List<String> tl = new ArrayList<>(TagOperation.pathSuggestions(menu.targetData, menu.command.getString("path"), 1));
        if (!menu.command.getString("path").isBlank())
            tl.addAll(TagOperation.pathSuggestions(menu.targetData, "", 1));
        for (String suggestion : tl) {
            longest = Math.max(longest, font.width(suggestion));
            suggestions.addWidget(Button.builder(Component.literal(suggestion), b->{
                String[] split = b.getMessage().getString().split(": ");
                if (split.length > 1) {
                    if (split[0].contains("{...}") || split[0].contains("[...]")) {
                        operand.setValue("");
                        operand.finishInput();
                    } else {
                        operator.setValue("==");
                        operator.finishInput();
                        String val = split[1];
                        if (!val.startsWith("\""))
                            val = val.replaceAll("[dfsblL]", "");
                        else
                            val = val.substring(1, val.length() - 1);
                        operand.setValue(val);
                        operand.finishInput();
                    }
                }
                if (split.length > 0) {
                    path.setValue(split[0].replaceAll("\\{\\.\\.\\.}", "").replaceAll("\\[\\.\\.\\.]", ""));
                    path.finishInput();
                }
            }).bounds(0, offset, innerWidth - 16, 14).build());
            offset += 16;
        }
        final int suggestionsExtra = Math.max(0, offset - suggestions.getHeight());
        slider = new VerticalSlider(getGuiLeft() + innerWidth - 8, 52 + getGuiTop(), 16, innerHeight, slider != null ? slider.getValue() : 0., s->suggestions.applyDelta(0, (int)Math.round(suggestionsExtra * s.getValue())));
        addRenderableWidget(slider);
        addRenderableWidget(suggestions);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (slider != null && mouseX >= 8 + getGuiLeft() && mouseX < getGuiLeft() + imageWidth - 8 && mouseY >= 52 + getGuiTop() && mouseY < getGuiTop() + imageHeight - 4) {
            slider.setValue(Mth.clamp(slider.getValue() - scroll / slider.getHeight(), 0., 1.));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
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
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blitNineSliced(GuiUtils.BACKGROUND_TEXTURE, getGuiLeft(), getGuiTop(), imageWidth, imageHeight, 8, 256, 256, 0, 0);
//        for (int i = 0; i < 8; ++i)
//            gui.drawCenteredString(font, "" + i, 9 + getGuiLeft(), 22 + i * 18 + getGuiTop(), -1);
    }

    @Override
    public void onClose() {
        menu.command.putString("type", TagOperation.getTypeForPath(menu.targetData, menu.command.getString("path")));
        ScreenNBTMsg.send(0, menu.command);
        super.onClose();
    }
}
