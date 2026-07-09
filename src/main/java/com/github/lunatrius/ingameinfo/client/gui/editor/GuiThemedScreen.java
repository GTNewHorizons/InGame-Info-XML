package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.github.lunatrius.ingameinfo.InGameInfoCore;

public abstract class GuiThemedScreen extends GuiScreen {

    protected static final int BUTTON_MARGIN_BOTTOM = 24;

    private static final int OUTSIDE_ROW_MARGIN = 6;
    private static final int OUTSIDE_BUTTON_WIDTH = 64;
    private static final int OUTSIDE_BUTTON_GAP = 5;

    // Shared across every editor screen so the toggle sticks as you navigate between them.
    private static boolean previewEnabled = false;

    protected final GuiScreen parentScreen;

    protected int panelX;
    protected int panelY;
    protected int panelWidth;
    protected int panelHeight;

    private GuiTexturedButton btnPreview;

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

    protected void onDone() {
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char character, int code) {
        if (code == Keyboard.KEY_ESCAPE) {
            onDone();
        }
    }

    protected GuiTexturedButton createDoneButton(int id) {
        return new GuiTexturedButton(
                id,
                this.width / 2 - 30,
                this.panelY + this.panelHeight - BUTTON_MARGIN_BOTTOM,
                60,
                I18n.format("gui.done"));
    }

    protected GuiTexturedButton createOutsideButton(int id, int slot, String label) {
        int x = this.panelX + slot * (OUTSIDE_BUTTON_WIDTH + OUTSIDE_BUTTON_GAP);
        int y = this.panelY + this.panelHeight + OUTSIDE_ROW_MARGIN;
        return new GuiTexturedButton(id, x, y, OUTSIDE_BUTTON_WIDTH, label);
    }

    /**
     * A toggle that renders the live HUD overlay on top of the editor, so edits can be checked against how they'll
     * actually look in-game.
     */
    protected void initPreviewButton(int id) {
        this.btnPreview = createOutsideButton(id, 0, I18n.format("gui.ingameinfoxml.visualconfig.preview"));
        this.btnPreview.selected = previewEnabled;
    }

    protected boolean handlePreviewClick(int x, int y) {
        if (this.btnPreview != null && this.btnPreview.mousePressed(x, y)) {
            previewEnabled = !previewEnabled;
            this.btnPreview.selected = previewEnabled;
            return true;
        }
        return false;
    }

    protected static boolean isPreviewEnabled() {
        return previewEnabled;
    }

    protected static void setPreviewEnabled(boolean enabled) {
        previewEnabled = enabled;
    }

    protected void drawPreview(int mouseX, int mouseY) {
        if (previewEnabled) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            InGameInfoCore core = InGameInfoCore.INSTANCE;
            core.onTickRender(core.scaledResolution);
        }
        if (this.btnPreview != null) {
            this.btnPreview.draw(this.fontRendererObj, mouseX, mouseY);
        }
    }

    @Override
    public void updateScreen() {
        if (previewEnabled) {
            InGameInfoCore.INSTANCE.onTickClient();
        }
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
