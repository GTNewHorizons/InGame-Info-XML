package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public abstract class GuiThemedScreen extends GuiScreen {

    protected static final int BUTTON_MARGIN_BOTTOM = 24;

    protected final GuiScreen parentScreen;

    protected int panelX;
    protected int panelY;
    protected int panelWidth;
    protected int panelHeight;

    protected GuiThemedScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    protected abstract String getTitleSegment();

    protected int getPreferredPanelWidth() {
        return 300;
    }

    protected int getPreferredPanelHeight() {
        return 220;
    }

    @Override
    public void initGui() {
        updatePanelBounds();
    }

    protected void updatePanelBounds() {
        this.panelWidth = Math.min(getPreferredPanelWidth(), this.width - 40);
        this.panelHeight = Math.min(getPreferredPanelHeight(), this.height - 40);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    protected GuiTexturedButton createDoneButton(int id) {
        return new GuiTexturedButton(
                id,
                this.width / 2 - 30,
                this.panelY + this.panelHeight - BUTTON_MARGIN_BOTTOM,
                60,
                I18n.format("gui.done"));
    }

    protected void drawPanel() {
        VisualConfigTheme.drawPanel(this.panelX, this.panelY, this.panelWidth, this.panelHeight);
    }

    protected final String getParentTitle() {
        String segment = getTitleSegment();
        if (this.parentScreen instanceof GuiThemedScreen) {
            return ((GuiThemedScreen) this.parentScreen).getParentTitle() + " > " + segment;
        }
        return segment;
    }

    protected void drawTitle() {
        drawTitle(getParentTitle());
    }

    private void drawTitle(String title) {
        String colored = VisualConfigTheme.colorize(title, true);
        this.fontRendererObj.drawStringWithShadow(
                colored,
                this.width / 2 - this.fontRendererObj.getStringWidth(colored) / 2,
                this.panelY + 8,
                0xFFFFFF);
    }
}
