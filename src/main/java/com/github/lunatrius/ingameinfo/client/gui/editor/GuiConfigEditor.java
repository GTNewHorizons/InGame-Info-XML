package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiConfigEditor extends GuiScreen {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_MARGIN_BOTTOM = 24;

    private final GuiScreen parentScreen;
    private GuiTexturedButton btnDone;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public GuiConfigEditor(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        updatePanelBounds();

        this.btnDone = new GuiTexturedButton(
                BUTTON_DONE,
                this.width / 2 - 30,
                this.panelY + this.panelHeight - BUTTON_MARGIN_BOTTOM,
                60,
                I18n.format("gui.done"));
    }

    private void updatePanelBounds() {
        this.panelWidth = Math.min(300, this.width - 40);
        this.panelHeight = Math.min(220, this.height - 40);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        if (action == 0 && this.btnDone.mousePressed(x, y)) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        super.mouseClicked(x, y, action);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        VisualConfigTheme.drawPanel(this.panelX, this.panelY, this.panelWidth, this.panelHeight);

        drawCenteredString(
                this.fontRendererObj,
                VisualConfigTheme.colorize(I18n.format("gui.ingameinfoxml.visualconfig"), true),
                this.width / 2,
                this.panelY + 8,
                0xFFFFFF);

        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
