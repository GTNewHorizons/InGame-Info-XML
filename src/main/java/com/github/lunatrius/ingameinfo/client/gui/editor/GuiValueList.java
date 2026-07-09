package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Mouse;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueSimple;

/**
 * Generic editor for a list of value objects 
 */
public class GuiValueList extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_ADD_VALUE = 1;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_BUTTON_WIDTH = 16;
    private static final int ROW_BUTTON_GAP = 2;

    private final List<Value> values;

    private GuiTexturedButton btnDone;
    private GuiTexturedButton btnAddValue;
    private final List<ValueRow> rows = new ArrayList<>();

    private int contentTop;
    private int contentBottom;
    private int visibleRows;
    private int scrollOffset;
    private int textRight;

    public GuiValueList(GuiScreen parentScreen, List<Value> values) {
        super(parentScreen);
        this.values = values;
    }

    @Override
    protected String getTitleSegment() {
        return I18n.format("gui.ingameinfoxml.visualconfig.editline");
    }

    @Override
    protected int getPreferredPanelWidth() {
        return 512;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.btnAddValue = new GuiTexturedButton(
                BUTTON_ADD_VALUE,
                this.width / 2 - 70,
                this.panelY + this.panelHeight - BUTTON_MARGIN_BOTTOM,
                60,
                I18n.format("gui.ingameinfoxml.visualconfig.addvalue"));
        this.btnDone = createDoneButton(BUTTON_DONE);
        this.btnDone.x = this.width / 2 + 10;

        this.contentTop = this.panelY + 22;
        this.contentBottom = this.btnDone.y - 6;

        rebuildRows();
    }

    private void rebuildRows() {
        this.visibleRows = Math.max(1, (this.contentBottom - this.contentTop) / ROW_HEIGHT);
        int maxScroll = Math.max(0, this.values.size() - this.visibleRows);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        this.rows.clear();
        int rightEdge = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH - 4;
        int buttonsX = rightEdge - (ROW_BUTTON_WIDTH * 3 + ROW_BUTTON_GAP * 2);
        this.textRight = buttonsX - ROW_BUTTON_GAP;

        int iconY = (ROW_HEIGHT - VisualConfigTheme.BUTTON_HEIGHT) / 2;
        int y = this.contentTop;
        for (int i = 0; i < this.visibleRows && this.scrollOffset + i < this.values.size(); i++) {
            int index = this.scrollOffset + i;
            Value value = this.values.get(index);

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
            down.enabled = index < this.values.size() - 1;

            this.rows.add(new ValueRow(value, index, y, up, down, delete));
            y += ROW_HEIGHT;
        }
    }

    private void moveValue(int index, int delta) {
        int target = index + delta;
        if (target < 0 || target >= this.values.size()) {
            return;
        }
        Collections.swap(this.values, index, target);
        onValuesChanged();
    }

    private void deleteValue(int index) {
        this.values.remove(index);
        onValuesChanged();
    }

    private void addValue() {
        this.values.add(Value.fromString("str"));
        this.scrollOffset = Math.max(0, this.values.size() - this.visibleRows);
        onValuesChanged();
    }

    private void onValuesChanged() {
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
    protected void mouseClicked(int x, int y, int action) {
        if (action == 0) {
            if (this.btnDone.mousePressed(x, y)) {
                this.mc.displayGuiScreen(this.parentScreen);
                return;
            }
            if (this.btnAddValue.mousePressed(x, y)) {
                addValue();
                return;
            }
            for (ValueRow row : this.rows) {
                if (row.btnUp.mousePressed(x, y)) {
                    moveValue(row.index, -1);
                    return;
                }
                if (row.btnDown.mousePressed(x, y)) {
                    moveValue(row.index, 1);
                    return;
                }
                if (row.btnDelete.mousePressed(x, y)) {
                    deleteValue(row.index);
                    return;
                }
                // Wire up individual values
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
        int backgroundWidth = scrollbarX - 3 - (this.panelX + 8);

        String hoveredTooltip = null;
        for (ValueRow row : this.rows) {
            boolean rowHovered = mouseY >= row.rowY && mouseY < row.rowY + ROW_HEIGHT
                    && mouseX >= this.panelX + 8
                    && mouseX < this.panelX + 8 + backgroundWidth;
            VisualConfigTheme.drawLineBackground(this.panelX + 8, row.rowY, backgroundWidth, rowHovered);

            String rawPreview = preview(row.value);
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

            boolean textHovered = mouseX >= this.panelX + 10
                    && mouseX < this.textRight
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

        VisualConfigTheme.drawScrollbar(
                scrollbarX,
                this.contentTop,
                this.contentBottom - this.contentTop,
                this.values.size(),
                this.visibleRows,
                this.scrollOffset);

        this.btnAddValue.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (hoveredTooltip != null) {
            drawHoveringText(Collections.singletonList(hoveredTooltip), mouseX, mouseY, this.fontRendererObj);
        }
    }

    private static String preview(Value value) {
        if (value instanceof ValueSimple.ValueVariable) {
            return "{" + value.getRawValue(false) + "}";
        } else if (value.isSimple()) {
            return value.getRawValue(false);
        } else {
            return "[" + value.getType() + "]";
        }
    }

    private static class ValueRow {

        private final Value value;
        private final int index;
        private final int rowY;
        private final GuiIconButton btnUp;
        private final GuiIconButton btnDown;
        private final GuiIconButton btnDelete;

        private ValueRow(Value value, int index, int rowY, GuiIconButton btnUp, GuiIconButton btnDown,
                GuiIconButton btnDelete) {
            this.value = value;
            this.index = index;
            this.rowY = rowY;
            this.btnUp = btnUp;
            this.btnDown = btnDown;
            this.btnDelete = btnDelete;
        }
    }
}
