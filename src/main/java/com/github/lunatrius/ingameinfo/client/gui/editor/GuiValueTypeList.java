package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Mouse;

import com.github.lunatrius.ingameinfo.value.registry.ValueRegistry;

/**
 * Picker for the complex value types (if, concat, math/logic ops, ...) that don't have a dedicated button. Icon has its
 * own button on the value list, so it's excluded here.
 */
public class GuiValueTypeList extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final int ROW_HEIGHT = 20;

    private final Consumer<String> onPick;
    private final List<String> names = buildNames();
    private final List<Row> rows = new ArrayList<>();

    private static List<String> buildNames() {
        List<String> names = new ArrayList<>(ValueRegistry.INSTANCE.getComplexTypeNames());
        names.remove("icon");
        return names;
    }

    private GuiTexturedButton btnDone;

    private int contentTop;
    private int contentBottom;
    private int visibleRows;
    private int scrollOffset;
    private boolean needsScrollbar;
    private boolean scrollbarDragging;

    public GuiValueTypeList(GuiScreen parentScreen, Consumer<String> onPick) {
        super(parentScreen);
        this.onPick = onPick;
    }

    @Override
    protected String getTitleSegment() {
        return I18n.format("gui.ingameinfoxml.visualconfig.addcomplex");
    }

    @Override
    protected int getPreferredPanelWidth() {
        return 220;
    }

    @Override
    protected int getPreferredPanelHeight() {
        return 260;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.btnDone = createDoneButton(BUTTON_DONE);

        this.contentTop = this.panelY + 22;
        this.contentBottom = this.btnDone.y - 6;

        rebuildRows();
    }

    private int scrollbarHeight() {
        return this.visibleRows * ROW_HEIGHT;
    }

    private void rebuildRows() {
        this.visibleRows = Math.max(1, (this.contentBottom - this.contentTop) / ROW_HEIGHT);
        this.needsScrollbar = this.names.size() > this.visibleRows;
        int maxScroll = Math.max(0, this.names.size() - this.visibleRows);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));

        this.rows.clear();
        int y = this.contentTop;
        for (int i = 0; i < this.visibleRows && this.scrollOffset + i < this.names.size(); i++) {
            int index = this.scrollOffset + i;
            this.rows.add(new Row(this.names.get(index), y));
            y += ROW_HEIGHT;
        }
    }

    private void pick(String name) {
        this.onPick.accept(name);
        this.mc.displayGuiScreen(this.parentScreen);
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
            this.scrollOffset = VisualConfigTheme
                    .scrollOffsetForY(mouseY, this.contentTop, scrollbarHeight(), this.names.size(), this.visibleRows);
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
            int scrollbarX = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH;
            if (this.needsScrollbar && x >= scrollbarX
                    && x < scrollbarX + VisualConfigTheme.SCROLLBAR_RAIL_WIDTH
                    && y >= this.contentTop
                    && y < this.contentTop + scrollbarHeight()) {
                this.scrollbarDragging = true;
                this.scrollOffset = VisualConfigTheme
                        .scrollOffsetForY(y, this.contentTop, scrollbarHeight(), this.names.size(), this.visibleRows);
                rebuildRows();
                return;
            }
            for (Row row : this.rows) {
                if (x >= this.panelX + 10 && x < this.panelX + this.panelWidth - 10
                        && y >= row.rowY
                        && y < row.rowY + ROW_HEIGHT) {
                    pick(row.name);
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

        int scrollbarX = this.panelX + this.panelWidth - 10 - VisualConfigTheme.SCROLLBAR_RAIL_WIDTH;
        int backgroundWidth = (this.needsScrollbar ? scrollbarX - 3 : this.panelX + this.panelWidth - 10)
                - (this.panelX + 8);

        for (Row row : this.rows) {
            boolean rowHovered = mouseY >= row.rowY && mouseY < row.rowY + ROW_HEIGHT
                    && mouseX >= this.panelX + 8
                    && mouseX < this.panelX + 8 + backgroundWidth;
            VisualConfigTheme.drawLineBackground(this.panelX + 8, row.rowY, backgroundWidth, rowHovered);

            String text = VisualConfigTheme.colorize(row.name, true);
            int textY = row.rowY + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2 + 1;
            this.fontRendererObj.drawStringWithShadow(text, this.panelX + 10, textY, 0xFFFFFF);
        }

        if (this.needsScrollbar) {
            VisualConfigTheme.drawScrollbar(
                    scrollbarX,
                    this.contentTop,
                    scrollbarHeight(),
                    this.names.size(),
                    this.visibleRows,
                    this.scrollOffset);
        }

        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawPreview(mouseX, mouseY);
    }

    private static class Row {

        private final String name;
        private final int rowY;

        private Row(String name, int rowY) {
            this.name = name;
            this.rowY = rowY;
        }
    }
}
