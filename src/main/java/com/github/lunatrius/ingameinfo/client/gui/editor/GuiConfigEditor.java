package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiConfigEditor extends GuiScreen {

    private static final int BUTTON_DONE = 0;

    private final GuiScreen parentScreen;
    private GuiTexturedButton btnDone;

    public GuiConfigEditor(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.btnDone = new GuiTexturedButton(
                BUTTON_DONE,
                this.width / 2 - 30,
                this.height - 114,
                60,
                I18n.format("gui.done"));
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

        int panelWidth = Math.min(300, this.width - 40);
        int panelHeight = Math.min(220, this.height - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        VisualConfigTheme.drawPanel(panelX, panelY, panelWidth, panelHeight);

        drawCenteredString(
                this.fontRendererObj,
                VisualConfigTheme.colorize(I18n.format("gui.ingameinfoxml.visualconfig"), true),
                this.width / 2,
                panelY + 8,
                0xFFFFFF);

        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
