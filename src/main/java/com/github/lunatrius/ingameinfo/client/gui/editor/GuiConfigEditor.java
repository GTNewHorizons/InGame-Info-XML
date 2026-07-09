package com.github.lunatrius.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.client.gui.GuiModConfig;
import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.handler.KeyInputHandler;
import com.github.lunatrius.ingameinfo.reference.Names;

public class GuiConfigEditor extends GuiThemedScreen implements GuiYesNoCallback {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_PREVIEW_PRIMARY = 97;
    private static final int BUTTON_PREVIEW_SECONDARY = 98;
    private static final int BUTTON_MOD_CONFIG = 96;
    private static final int BUTTON_SCALE_DOWN = 94;
    private static final int BUTTON_SCALE_UP = 95;
    private static final int BUTTON_SCALE_RESET = 93;
    private static final Alignment[][] GRID = { { Alignment.TOPLEFT, Alignment.TOPCENTER, Alignment.TOPRIGHT },
            { Alignment.MIDDLELEFT, Alignment.MIDDLECENTER, Alignment.MIDDLERIGHT },
            { Alignment.BOTTOMLEFT, Alignment.BOTTOMCENTER, Alignment.BOTTOMRIGHT } };

    private GuiTexturedButton btnDone;
    private GuiTexturedButton btnPreviewPrimary;
    private GuiTexturedButton btnPreviewSecondary;
    private GuiTexturedButton btnModConfig;
    private GuiTexturedButton btnScaleDown;
    private GuiTexturedButton btnScaleUp;
    private GuiTexturedButton btnScaleReset;
    private String secondaryConfigName;
    private String pendingSwitchFilename;
    private final List<AlignmentButton> alignmentButtons = new ArrayList<>();

    public GuiConfigEditor(GuiScreen parentScreen) {
        super(parentScreen);
    }

    @Override
    protected String getTitleSegment() {
        return I18n.format("gui.ingameinfoxml.visualconfig");
    }

    @Override
    protected int getPreferredPanelHeight() {
        return 270;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.btnDone = createDoneButton(BUTTON_DONE);

        this.secondaryConfigName = KeyInputHandler.getSecondaryConfigName(ConfigurationHandler.configName);
        boolean hasSecondary = InGameInfoCore.INSTANCE.hasConfigFileWithLocale(this.secondaryConfigName);

        String previewLabel = I18n.format("gui.ingameinfoxml.visualconfig.preview");
        this.btnPreviewPrimary = createOutsideButton(BUTTON_PREVIEW_PRIMARY, 0, previewLabel + " 1");
        this.btnPreviewSecondary = createOutsideButton(BUTTON_PREVIEW_SECONDARY, 1, previewLabel + " 2");
        this.btnPreviewSecondary.enabled = hasSecondary;
        updatePreviewSelection();

        this.btnModConfig = createOutsideButton(
                BUTTON_MOD_CONFIG,
                2,
                I18n.format("gui.ingameinfoxml.visualconfig.modconfig"));

        int scaleY = this.panelY + 22;
        int centerX = this.width / 2;
        this.btnScaleDown = new GuiTexturedButton(BUTTON_SCALE_DOWN, centerX - 70, scaleY, 20, "-");
        this.btnScaleUp = new GuiTexturedButton(BUTTON_SCALE_UP, centerX + 50, scaleY, 20, "+");

        int resetY = scaleY + 20;
        this.btnScaleReset = new GuiTexturedButton(
                BUTTON_SCALE_RESET,
                centerX - 25,
                resetY,
                50,
                I18n.format("gui.ingameinfoxml.visualconfig.reset"));

        this.alignmentButtons.clear();
        int rows = GRID.length;
        int cols = GRID[0].length;
        int top = resetY + 22;
        int bottom = this.btnDone.y - 6;
        int cellHeight = (bottom - top) / rows;
        int columnGap = 4;
        int cellWidth = (this.panelWidth - 20 - columnGap * (cols - 1)) / cols;
        int left = this.panelX + 10;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Alignment alignment = GRID[row][col];
                String label = I18n.format(Names.Config.LANG_PREFIX + "." + alignment.toString().toLowerCase());
                GuiTexturedButton button = new GuiTexturedButton(
                        100 + row * cols + col,
                        left + col * (cellWidth + columnGap),
                        top + row * cellHeight,
                        cellWidth,
                        label);
                this.alignmentButtons.add(new AlignmentButton(button, alignment));
            }
        }
    }

    private void adjustScale(int delta) {
        int current = ConfigurationHandler.propscale.getInt(ConfigurationHandler.DEFAULT_SCALE);
        int next = Math.max(1, Math.min(20, current + delta));
        if (next == current) {
            return;
        }
        ConfigurationHandler.propscale.set(next);
        ConfigurationHandler.Scale = next;
        ConfigurationHandler.save();
    }

    private void resetScale() {
        ConfigurationHandler.propscale.set(ConfigurationHandler.DEFAULT_SCALE);
        ConfigurationHandler.Scale = ConfigurationHandler.DEFAULT_SCALE;
        ConfigurationHandler.save();
    }

    private void updatePreviewSelection() {
        String active = InGameInfoCore.INSTANCE.getBaseConfigFileName();
        boolean primaryActive = ConfigurationHandler.configName.equalsIgnoreCase(active);
        boolean secondaryActive = this.secondaryConfigName.equalsIgnoreCase(active);
        this.btnPreviewPrimary.selected = isPreviewEnabled() && primaryActive;
        this.btnPreviewSecondary.selected = isPreviewEnabled() && secondaryActive;
    }

    private void requestPreview(String filename) {
        String active = InGameInfoCore.INSTANCE.getBaseConfigFileName();
        boolean alreadyActive = filename.equalsIgnoreCase(active);

        if (alreadyActive) {
            setPreviewEnabled(!isPreviewEnabled());
            updatePreviewSelection();
            return;
        }

        if (InGameInfoCore.INSTANCE.isDirty()) {
            this.pendingSwitchFilename = filename;
            this.mc.displayGuiScreen(
                    new GuiYesNo(
                            this,
                            I18n.format("gui.ingameinfoxml.visualconfig.unsaved.title"),
                            I18n.format("gui.ingameinfoxml.visualconfig.unsaved.message"),
                            0));
            return;
        }

        switchAndPreview(filename);
    }

    private void switchAndPreview(String filename) {
        InGameInfoCore.INSTANCE.setConfigFileWithLocale(filename);
        InGameInfoCore.INSTANCE.reloadConfig();
        setPreviewEnabled(true);
        updatePreviewSelection();
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        this.mc.displayGuiScreen(this);
        if (result && this.pendingSwitchFilename != null) {
            switchAndPreview(this.pendingSwitchFilename);
        }
        this.pendingSwitchFilename = null;
    }

    @Override
    protected void onDone() {
        String filename = InGameInfoCore.INSTANCE.getBaseConfigFileName();
        if (filename != null) {
            InGameInfoCore.INSTANCE.saveConfig(filename);
        }
        super.onDone();
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        if (action == 0) {
            if (this.btnDone.mousePressed(x, y)) {
                onDone();
                return;
            }
            if (this.btnPreviewPrimary.mousePressed(x, y)) {
                requestPreview(ConfigurationHandler.configName);
                return;
            }
            if (this.btnPreviewSecondary.mousePressed(x, y)) {
                requestPreview(this.secondaryConfigName);
                return;
            }
            if (this.btnModConfig.mousePressed(x, y)) {
                this.mc.displayGuiScreen(new GuiModConfig(this));
                return;
            }
            if (this.btnScaleDown.mousePressed(x, y)) {
                adjustScale(-1);
                return;
            }
            if (this.btnScaleUp.mousePressed(x, y)) {
                adjustScale(1);
                return;
            }
            if (this.btnScaleReset.mousePressed(x, y)) {
                resetScale();
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
        this.btnPreviewPrimary.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnPreviewSecondary.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnModConfig.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnScaleDown.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnScaleUp.draw(this.fontRendererObj, mouseX, mouseY);
        this.btnScaleReset.draw(this.fontRendererObj, mouseX, mouseY);

        String scaleLabel = VisualConfigTheme
                .colorize(I18n.format("gui.ingameinfoxml.visualconfig.scale", ConfigurationHandler.Scale / 10F), true);
        this.fontRendererObj.drawStringWithShadow(
                scaleLabel,
                this.width / 2 - this.fontRendererObj.getStringWidth(scaleLabel) / 2,
                this.btnScaleDown.y + 4,
                0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawPreview(mouseX, mouseY);
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
