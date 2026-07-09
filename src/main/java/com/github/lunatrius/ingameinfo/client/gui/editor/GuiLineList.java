package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Property;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueSimple;

public class GuiLineList extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_ADD_LINE = 1;
    private static final int BUTTON_PREVIEW = 99;
    private static final int BUTTON_OFFSET_X_DOWN = 90;
    private static final int BUTTON_OFFSET_X_UP = 91;
    private static final int BUTTON_OFFSET_Y_DOWN = 92;
    private static final int BUTTON_OFFSET_Y_UP = 93;
    private static final int BUTTON_OFFSET_RESET = 89;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_BUTTON_WIDTH = 16;
    private static final int ROW_BUTTON_GAP = 2;

    private static final List<String> OFFSET_TOOLTIP = Arrays
            .asList("Hold", "> Shift: x10", "> Ctrl: x100", "> Alt: x5");

    private final Alignment alignment;
    private List<List<Value>> lines;

    private GuiTexturedButton btnDone;
    private GuiTexturedButton btnAddLine;
    private GuiTexturedButton btnOffsetXDown;
    private GuiTexturedButton btnOffsetXUp;
    private GuiTexturedButton btnOffsetYDown;
    private GuiTexturedButton btnOffsetYUp;
    private GuiTexturedButton btnOffsetReset;
    private final List<LineRow> rows = new ArrayList<>();

    private int contentTop;
    private int contentBottom;
    private int visibleRows;
    private int scrollOffset;
    private int textRight;
    private int rowRight;
    private boolean needsScrollbar;
    private boolean scrollbarDragging;

    public GuiLineList(GuiScreen parentScreen, Alignment alignment) {
        super(parentScreen);
        this.alignment = alignment;
    }

    @Override
    protected String getTitleSegment() {
        return I18n.format(Names.Config.LANG_PREFIX + "." + this.alignment.toString().toLowerCase());
    }

    @Override
    protected int getPreferredPanelWidth() {
        return 300;
    }

    @Override
    protected int getPreferredPanelHeight() {
        return 275;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.lines = InGameInfoCore.INSTANCE.getFormat().computeIfAbsent(this.alignment, a -> new ArrayList<>());

        this.btnAddLine = new GuiTexturedButton(
                BUTTON_ADD_LINE,
                this.width / 2 - 70,
                this.panelY + this.panelHeight - BUTTON_MARGIN_BOTTOM,
                60,
                I18n.format("gui.ingameinfoxml.visualconfig.addline"));
        this.btnDone = createDoneButton(BUTTON_DONE);
        this.btnDone.x = this.width / 2 + 10;
        initPreviewButton(BUTTON_PREVIEW);

        int offsetY = this.panelY + 22;
        int centerX = this.width / 2;
        this.btnOffsetXDown = new GuiTexturedButton(BUTTON_OFFSET_X_DOWN, centerX - 130, offsetY, 20, "-");
        this.btnOffsetXUp = new GuiTexturedButton(BUTTON_OFFSET_X_UP, centerX - 30, offsetY, 20, "+");
        this.btnOffsetYDown = new GuiTexturedButton(BUTTON_OFFSET_Y_DOWN, centerX + 10, offsetY, 20, "-");
        this.btnOffsetYUp = new GuiTexturedButton(BUTTON_OFFSET_Y_UP, centerX + 110, offsetY, 20, "+");

        int resetY = offsetY + 20;
        this.btnOffsetReset = new GuiTexturedButton(
                BUTTON_OFFSET_RESET,
                centerX - 25,
                resetY,
                50,
                I18n.format("gui.ingameinfoxml.visualconfig.reset"));

        this.contentTop = resetY + 22;
        this.contentBottom = this.btnDone.y - 6;

        rebuildRows();
    }

    private void rebuildRows() {
        this.visibleRows = Math.max(1, (this.contentBottom - this.contentTop) / ROW_HEIGHT);
        this.needsScrollbar = this.lines.size() > this.visibleRows;
        int maxScroll = Math.max(0, this.lines.size() - this.visibleRows);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        this.rows.clear();
        int scrollbarReserve = this.needsScrollbar ? VisualConfigTheme.SCROLLBAR_RAIL_WIDTH + 4 : 0;
        this.rowRight = this.panelX + this.panelWidth - 10 - scrollbarReserve;
        int buttonsX = this.rowRight - (ROW_BUTTON_WIDTH * 3 + ROW_BUTTON_GAP * 2);
        this.textRight = buttonsX - ROW_BUTTON_GAP;

        int iconY = (ROW_HEIGHT - VisualConfigTheme.BUTTON_HEIGHT) / 2;
        int y = this.contentTop;
        for (int i = 0; i < this.visibleRows && this.scrollOffset + i < this.lines.size(); i++) {
            int index = this.scrollOffset + i;
            List<Value> line = this.lines.get(index);

            GuiIconButton up = new GuiIconButton(
                    buttonsX,
                    y + iconY,
                    VisualConfigTheme.RowIcon.UP,
                    I18n.format("gui.ingameinfoxml.visualconfig.tooltip.moveup"));
            GuiIconButton down = new GuiIconButton(
                    buttonsX + ROW_BUTTON_WIDTH + ROW_BUTTON_GAP,
                    y + iconY,
                    VisualConfigTheme.RowIcon.DOWN,
                    I18n.format("gui.ingameinfoxml.visualconfig.tooltip.movedown"));
            GuiIconButton delete = new GuiIconButton(
                    buttonsX + (ROW_BUTTON_WIDTH + ROW_BUTTON_GAP) * 2,
                    y + iconY,
                    VisualConfigTheme.RowIcon.DELETE,
                    I18n.format("gui.ingameinfoxml.visualconfig.tooltip.delete"));
            up.enabled = index > 0;
            down.enabled = index < this.lines.size() - 1;

            this.rows.add(new LineRow(line, index, y, up, down, delete));
            y += ROW_HEIGHT;
        }
    }

    private void adjustOffsetX(int sign) {
        this.alignment.x += sign * getOffsetStep();
        persistOffset();
    }

    private void adjustOffsetY(int sign) {
        this.alignment.y += sign * getOffsetStep();
        persistOffset();
    }

    /**
     * Ctrl/Shift/Alt take bigger steps so large repositions don't need dozens of clicks.
     */
    private static int getOffsetStep() {
        if (isCtrlKeyDown()) {
            return 100;
        }
        if (isShiftKeyDown()) {
            return 10;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            return 5;
        }
        return 1;
    }

    private void persistOffset() {
        Property property = ConfigurationHandler.propAlignments.get(this.alignment);
        if (property != null) {
            property.set(this.alignment.getXY());
            ConfigurationHandler.save();
        }
    }

    private void resetOffset() {
        this.alignment.x = this.alignment.defaultX;
        this.alignment.y = this.alignment.defaultY;
        persistOffset();
    }

    private void moveLine(int index, int delta) {
        int target = index + delta;
        if (target < 0 || target >= this.lines.size()) {
            return;
        }
        Collections.swap(this.lines, index, target);
        onLinesChanged();
    }

    private void deleteLine(int index) {
        this.lines.remove(index);
        onLinesChanged();
    }

    private void addLine() {
        List<Value> line = new ArrayList<>();
        line.add(Value.fromString("str"));
        this.lines.add(line);
        this.scrollOffset = Math.max(0, this.lines.size() - this.visibleRows);
        onLinesChanged();
    }

    private void onLinesChanged() {
        InGameInfoCore.INSTANCE.markDirty();
        InGameInfoCore.INSTANCE.refreshInfoTexts();
        rebuildRows();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            this.scrollOffset -= Integer.signum(wheel);
            rebuildRows();
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.scrollbarDragging && clickedMouseButton == 0) {
            this.scrollOffset = VisualConfigTheme.scrollOffsetForY(
                    mouseY,
                    this.contentTop,
                    this.contentBottom - this.contentTop,
                    this.lines.size(),
                    this.visibleRows);
            rebuildRows();
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int mouseEvent) {
        this.scrollbarDragging = false;
        super.mouseMovedOrUp(x, y, mouseEvent);
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        if (action == 0) {
            if (this.btnDone.mousePressed(x, y)) {
                onDone();
                return;
            }
            if (this.btnAddLine.mousePressed(x, y)) {
                addLine();
                return;
            }
            if (handlePreviewClick(x, y)) {
                return;
            }
            if (this.btnOffsetXDown.mousePressed(x, y)) {
                adjustOffsetX(-1);
                return;
            }
            if (this.btnOffsetXUp.mousePressed(x, y)) {
                adjustOffsetX(1);
                return;
            }
            if (this.btnOffsetYDown.mousePressed(x, y)) {
                adjustOffsetY(-1);
                return;
            }
            if (this.btnOffsetYUp.mousePressed(x, y)) {
                adjustOffsetY(1);
                return;
            }
            if (this.btnOffsetReset.mousePressed(x, y)) {
                resetOffset();
                return;
            }
            int scrollbarX = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH;
            if (this.needsScrollbar && x >= scrollbarX
                    && x < scrollbarX + VisualConfigTheme.SCROLLBAR_RAIL_WIDTH
                    && y >= this.contentTop
                    && y < this.contentBottom) {
                this.scrollbarDragging = true;
                this.scrollOffset = VisualConfigTheme.scrollOffsetForY(
                        y,
                        this.contentTop,
                        this.contentBottom - this.contentTop,
                        this.lines.size(),
                        this.visibleRows);
                rebuildRows();
                return;
            }
            for (LineRow row : this.rows) {
                if (row.btnUp.mousePressed(x, y)) {
                    moveLine(row.index, -1);
                    return;
                }
                if (row.btnDown.mousePressed(x, y)) {
                    moveLine(row.index, 1);
                    return;
                }
                if (row.btnDelete.mousePressed(x, y)) {
                    deleteLine(row.index);
                    return;
                }
                if (x >= this.panelX + 10 && x < this.textRight && y >= row.rowY && y < row.rowY + ROW_HEIGHT) {
                    this.mc.displayGuiScreen(new GuiValueList(this, row.line));
                    return;
                }
            }
        }
        super.mouseClicked(x, y, action);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawPanel();
        drawTitle();

        int maxTextWidth = this.textRight - (this.panelX + 10);

        int scrollbarX = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH;
        int backgroundWidth = this.rowRight - (this.panelX + 8);

        String hoveredTooltip = null;
        List<String> hoveredTooltipLines = null;
        for (LineRow row : this.rows) {
            boolean rowHovered = mouseY >= row.rowY && mouseY < row.rowY + ROW_HEIGHT
                    && mouseX >= this.panelX + 8
                    && mouseX < this.panelX + 8 + backgroundWidth;
            VisualConfigTheme.drawLineBackground(this.panelX + 8, row.rowY, backgroundWidth, rowHovered);

            String rawPreview = preview(row.line);
            String trimmedRaw = this.fontRendererObj.trimStringToWidth(rawPreview, maxTextWidth);
            boolean truncated = !trimmedRaw.equals(rawPreview);
            if (truncated) {
                int ellipsisWidth = this.fontRendererObj.getStringWidth("...");
                trimmedRaw = this.fontRendererObj.trimStringToWidth(rawPreview, maxTextWidth - ellipsisWidth) + "...";
            }
            String text = VisualConfigTheme.colorize(trimmedRaw, true);
            int textY = row.rowY + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2 + 1;
            this.fontRendererObj.drawStringWithShadow(text, this.panelX + 10, textY, 0xFFFFFF);

            row.btnUp.draw(mouseX, mouseY);
            row.btnDown.draw(mouseX, mouseY);
            row.btnDelete.draw(mouseX, mouseY);

            boolean textHovered = mouseX >= this.panelX + 10 && mouseX < textRight
                    && mouseY >= row.rowY
                    && mouseY < row.rowY + ROW_HEIGHT;

            if (row.btnUp.containsMouse(mouseX, mouseY)) {
                hoveredTooltip = row.btnUp.getTooltip();
            } else if (row.btnDown.containsMouse(mouseX, mouseY)) {
                hoveredTooltip = row.btnDown.getTooltip();
            } else if (row.btnDelete.containsMouse(mouseX, mouseY)) {
                hoveredTooltip = row.btnDelete.getTooltip();
            } else if (truncated && textHovered) {
                hoveredTooltip = rawPreview;
            }
        }

        if (this.needsScrollbar) {
            VisualConfigTheme.drawScrollbar(
                    scrollbarX,
                    this.contentTop,
                    this.contentBottom - this.contentTop,
                    this.lines.size(),
                    this.visibleRows,
                    this.scrollOffset);
        }

        this.btnAddLine.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        this.btnOffsetXDown.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnOffsetXUp.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnOffsetYDown.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnOffsetYUp.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnOffsetReset.draw(this.fontRendererObj, mouseX, mouseY);

        if (this.btnOffsetXDown.isMouseOver(mouseX, mouseY) || this.btnOffsetXUp.isMouseOver(mouseX, mouseY)
                || this.btnOffsetYDown.isMouseOver(mouseX, mouseY)
                || this.btnOffsetYUp.isMouseOver(mouseX, mouseY)) {
            hoveredTooltipLines = OFFSET_TOOLTIP;
        }

        String xLabel = VisualConfigTheme
                .colorize(I18n.format("gui.ingameinfoxml.visualconfig.offset_x", this.alignment.x), true);
        this.fontRendererObj.drawStringWithShadow(
                xLabel,
                (this.btnOffsetXDown.x + this.btnOffsetXUp.x + 20) / 2
                        - this.fontRendererObj.getStringWidth(xLabel) / 2,
                this.btnOffsetXDown.y + 4,
                0xFFFFFF);

        String yLabel = VisualConfigTheme
                .colorize(I18n.format("gui.ingameinfoxml.visualconfig.offset_y", this.alignment.y), true);
        this.fontRendererObj.drawStringWithShadow(
                yLabel,
                (this.btnOffsetYDown.x + this.btnOffsetYUp.x + 20) / 2
                        - this.fontRendererObj.getStringWidth(yLabel) / 2,
                this.btnOffsetYDown.y + 4,
                0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (hoveredTooltipLines != null) {
            drawHoveringText(hoveredTooltipLines, mouseX, mouseY, this.fontRendererObj);
        } else if (hoveredTooltip != null) {
            drawHoveringText(Collections.singletonList(hoveredTooltip), mouseX, mouseY, this.fontRendererObj);
        }

        drawPreview(mouseX, mouseY);
    }

    private static String preview(List<Value> line) {
        if (line.isEmpty()) {
            return I18n.format("gui.ingameinfoxml.visualconfig.emptyline");
        }

        StringBuilder builder = new StringBuilder();
        for (Value value : line) {
            if (value instanceof ValueSimple.ValueVariable) {
                builder.append('{').append(value.getRawValue(false)).append('}');
            } else if (value.isSimple()) {
                builder.append(value.getRawValue(false));
            } else {
                builder.append('[').append(value.getType()).append(']');
            }
        }
        return builder.toString();
    }

    private static class LineRow {

        private final List<Value> line;
        private final int index;
        private final int rowY;
        private final GuiIconButton btnUp;
        private final GuiIconButton btnDown;
        private final GuiIconButton btnDelete;

        private LineRow(List<Value> line, int index, int rowY, GuiIconButton btnUp, GuiIconButton btnDown,
                GuiIconButton btnDelete) {
            this.line = line;
            this.index = index;
            this.rowY = rowY;
            this.btnUp = btnUp;
            this.btnDown = btnDown;
            this.btnDelete = btnDelete;
        }
    }
}
