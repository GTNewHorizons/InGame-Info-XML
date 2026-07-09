package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Mouse;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.client.gui.GuiTags;
import com.github.lunatrius.ingameinfo.client.gui.Info;
import com.github.lunatrius.ingameinfo.client.gui.InfoText;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueSimple;

/**
 * Generic editor for a list of value objects
 */
public class GuiValueList extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_ADD_VALUE = 1;
    private static final int BUTTON_ADD_TAG = 2;
    private static final int BUTTON_PREVIEW = 99;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_BUTTON_WIDTH = 16;
    private static final int ROW_BUTTON_GAP = 2;

    private final List<Value> values;

    private GuiTexturedButton btnDone;
    private GuiTexturedButton btnAddValue;
    private GuiTexturedButton btnAddTag;
    private final List<ValueRow> rows = new ArrayList<>();

    private int contentTop;
    private int contentBottom;
    private int visibleRows;
    private int scrollOffset;
    private int textRight;
    private int rowRight;
    private boolean needsScrollbar;
    private boolean scrollbarDragging;

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
        return 300;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.btnDone = createDoneButton(BUTTON_DONE);

        int buttonWidth = 90;
        int gap = 5;
        int groupWidth = buttonWidth * 3 + gap * 2;
        int startX = this.width / 2 - groupWidth / 2;
        int buttonY = this.btnDone.y;

        this.btnAddValue = new GuiTexturedButton(
                BUTTON_ADD_VALUE,
                startX,
                buttonY,
                buttonWidth,
                I18n.format("gui.ingameinfoxml.visualconfig.addvalue"));
        this.btnAddTag = new GuiTexturedButton(
                BUTTON_ADD_TAG,
                startX + buttonWidth + gap,
                buttonY,
                buttonWidth,
                I18n.format("gui.ingameinfoxml.visualconfig.addtag"));
        this.btnDone.x = startX + (buttonWidth + gap) * 2;
        this.btnDone.width = buttonWidth;
        initPreviewButton(BUTTON_PREVIEW);

        this.contentTop = this.panelY + 22;
        this.contentBottom = this.btnDone.y - 6;

        rebuildRows();
    }

    /**
     * The content area's height doesn't always divide evenly by ROW_HEIGHT, which would otherwise leave the
     * scrollbar rail poking out past the last row's background by that remainder.
     */
    private int scrollbarHeight() {
        return this.visibleRows * ROW_HEIGHT;
    }

    private void rebuildRows() {
        this.visibleRows = Math.max(1, (this.contentBottom - this.contentTop) / ROW_HEIGHT);
        this.needsScrollbar = this.values.size() > this.visibleRows;
        int maxScroll = Math.max(0, this.values.size() - this.visibleRows);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        this.rows.clear();
        int scrollbarReserve = this.needsScrollbar ? VisualConfigTheme.SCROLLBAR_RAIL_WIDTH + 4 : 0;
        this.rowRight = this.panelX + this.panelWidth - 10 - scrollbarReserve;
        int buttonsX = this.rowRight - (ROW_BUTTON_WIDTH * 3 + ROW_BUTTON_GAP * 2);
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

    private void addTag(String rawName) {
        Value value = Value.fromString("var");
        value.setRawValue(rawName, true);
        this.values.add(value);
        this.scrollOffset = Math.max(0, this.values.size() - this.visibleRows);
        onValuesChanged();
    }

    private void onValuesChanged() {
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
                    scrollbarHeight(),
                    this.values.size(),
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
            if (this.btnAddValue.mousePressed(x, y)) {
                addValue();
                return;
            }
            if (this.btnAddTag.mousePressed(x, y)) {
                this.mc.displayGuiScreen(new GuiTags(this, this::addTag));
                return;
            }
            if (handlePreviewClick(x, y)) {
                return;
            }
            int scrollbarX = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH;
            if (this.needsScrollbar && x >= scrollbarX
                    && x < scrollbarX + VisualConfigTheme.SCROLLBAR_RAIL_WIDTH
                    && y >= this.contentTop
                    && y < this.contentTop + scrollbarHeight()) {
                this.scrollbarDragging = true;
                this.scrollOffset = VisualConfigTheme.scrollOffsetForY(
                        y,
                        this.contentTop,
                        scrollbarHeight(),
                        this.values.size(),
                        this.visibleRows);
                rebuildRows();
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
                if (x >= this.panelX + 10 && x < this.textRight && y >= row.rowY && y < row.rowY + ROW_HEIGHT) {
                    this.mc.displayGuiScreen(new GuiValueEditor(this, row.value));
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
        int backgroundWidth = this.rowRight - (this.panelX + 8) + 1;

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
            // Rendered mode already carries its own color codes from resolved tags - force a white base ("§f")
            // instead of VisualConfigTheme's UI tint, so unformatted portions match the real HUD instead of
            // whatever color happened to be left active by the last tag.
            String text = isRenderedPreviewEnabled() ? "§f" + trimmedRaw
                    : VisualConfigTheme.colorize(trimmedRaw, true);
            int textY = row.rowY + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2 + 1;
            this.fontRendererObj.drawStringWithShadow(text, this.panelX + 10, textY, 0xFFFFFF);

            if (isRenderedPreviewEnabled() && row.value instanceof ValueSimple.ValueVariable) {
                InfoText infoText = row.value.getParent();
                Info icon = infoText != null ? infoText.getAttachedValue(row.value.getRawValue(false)) : null;
                if (icon != null) {
                    drawInlineIcon(icon, this.panelX + 10, textY);
                }
            }

            row.btnUp.draw(mouseX, mouseY);
            row.btnDown.draw(mouseX, mouseY);
            row.btnDelete.draw(mouseX, mouseY);

            boolean textHovered = mouseX >= this.panelX + 10 && mouseX < this.textRight
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
                    scrollbarHeight(),
                    this.values.size(),
                    this.visibleRows,
                    this.scrollOffset);
        }

        this.btnAddValue.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnAddTag.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (hoveredTooltip != null) {
            drawHoveringText(Collections.singletonList(hoveredTooltip), mouseX, mouseY, this.fontRendererObj);
        }

        drawPreview(mouseX, mouseY);
    }

    private static String preview(Value value) {
        if (isRenderedPreviewEnabled()) {
            return resolveValue(value);
        }
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
