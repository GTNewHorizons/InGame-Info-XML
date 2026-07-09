package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.reference.Names;

public class GuiConfigEditor extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final Alignment[][] GRID = { { Alignment.TOPLEFT, Alignment.TOPCENTER, Alignment.TOPRIGHT },
            { Alignment.MIDDLELEFT, Alignment.MIDDLECENTER, Alignment.MIDDLERIGHT },
            { Alignment.BOTTOMLEFT, Alignment.BOTTOMCENTER, Alignment.BOTTOMRIGHT } };

    private GuiTexturedButton btnDone;
    private final List<AlignmentButton> alignmentButtons = new ArrayList<>();

    public GuiConfigEditor(GuiScreen parentScreen) {
        super(parentScreen);
    }

    @Override
    protected String getTitleSegment() {
        return I18n.format("gui.ingameinfoxml.visualconfig");
    }

    @Override
    public void initGui() {
        super.initGui();

        this.btnDone = createDoneButton(BUTTON_DONE);

        this.alignmentButtons.clear();
        int rows = GRID.length;
        int cols = GRID[0].length;
        int top = this.panelY + 22;
        int bottom = this.btnDone.y - 6;
        int cellHeight = (bottom - top) / rows;
        int cellWidth = (this.panelWidth - 20) / cols;
        int left = this.panelX + 10;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Alignment alignment = GRID[row][col];
                String label = I18n.format(Names.Config.LANG_PREFIX + "." + alignment.toString().toLowerCase());
                GuiTexturedButton button = new GuiTexturedButton(
                        100 + row * cols + col,
                        left + col * cellWidth,
                        top + row * cellHeight,
                        cellWidth - 4,
                        label);
                this.alignmentButtons.add(new AlignmentButton(button, alignment));
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        if (action == 0) {
            if (this.btnDone.mousePressed(x, y)) {
                this.mc.displayGuiScreen(this.parentScreen);
                return;
            }
            for (AlignmentButton alignmentButton : this.alignmentButtons) {
                if (alignmentButton.button.mousePressed(x, y)) {
                    this.mc.displayGuiScreen(new GuiLineList(this, alignmentButton.alignment));
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

        for (AlignmentButton alignmentButton : this.alignmentButtons) {
            alignmentButton.button.draw(this.fontRendererObj, mouseX, mouseY);
        }

        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static class AlignmentButton {

        private final GuiTexturedButton button;
        private final Alignment alignment;

        private AlignmentButton(GuiTexturedButton button, Alignment alignment) {
            this.button = button;
            this.alignment = alignment;
        }
    }
}
