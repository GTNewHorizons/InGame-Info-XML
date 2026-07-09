package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.client.gui.GuiTags;
import com.github.lunatrius.ingameinfo.value.Value;
import com.github.lunatrius.ingameinfo.value.ValueSimple;

/**
 * Single value raw text edit
 */
public class GuiValueEditor extends GuiThemedScreen {

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_EDIT_CHILDREN = 1;
    private static final int BUTTON_PICK_TAG = 2;

    private final Value value;
    private final boolean isVariable;

    private String currentText;

    private GuiTextField textField;
    private GuiTexturedButton btnDone;
    private GuiTexturedButton btnEditChildren;
    private GuiTexturedButton btnPickTag;

    public GuiValueEditor(GuiScreen parentScreen, Value value) {
        super(parentScreen);
        this.value = value;
        this.isVariable = value instanceof ValueSimple.ValueVariable;
        this.currentText = value.getRawValue(true);
    }

    @Override
    protected String getTitleSegment() {
        return "[" + this.value.getType() + "]";
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.btnDone = createDoneButton(BUTTON_DONE);

        int fieldY = this.panelY + 26;
        int fieldWidth = this.panelWidth - 20;
        this.textField = new GuiTextField(this.fontRendererObj, this.panelX + 10, fieldY, fieldWidth, 16);
        this.textField.setMaxStringLength(4096);
        this.textField.setText(this.currentText);
        this.textField.setFocused(true);

        int buttonY = fieldY + 24;
        int buttonX = this.panelX + 10;
        if (this.isVariable) {
            this.btnPickTag = new GuiTexturedButton(
                    BUTTON_PICK_TAG,
                    buttonX,
                    buttonY,
                    100,
                    I18n.format("gui.ingameinfoxml.visualconfig.picktag"));
            buttonX += 104;
        }
        if (!this.value.isSimple()) {
            this.btnEditChildren = new GuiTexturedButton(
                    BUTTON_EDIT_CHILDREN,
                    buttonX,
                    buttonY,
                    100,
                    I18n.format("gui.ingameinfoxml.visualconfig.editchildren"));
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void save() {
        this.value.setRawValue(this.textField.getText(), true);
        InGameInfoCore.INSTANCE.refreshInfoTexts();
    }

    private void onTagPicked(String rawName) {
        this.currentText = rawName;
        this.textField.setText(rawName);
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        this.textField.mouseClicked(x, y, action);

        if (action == 0) {
            if (this.btnDone.mousePressed(x, y)) {
                save();
                this.mc.displayGuiScreen(this.parentScreen);
                return;
            }
            if (this.btnPickTag != null && this.btnPickTag.mousePressed(x, y)) {
                this.mc.displayGuiScreen(new GuiTags(this, this::onTagPicked));
                return;
            }
            if (this.btnEditChildren != null && this.btnEditChildren.mousePressed(x, y)) {
                save();
                this.mc.displayGuiScreen(new GuiValueList(this, this.value.values));
                return;
            }
        }
        super.mouseClicked(x, y, action);
    }

    @Override
    protected void keyTyped(char character, int code) {
        if (code == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        this.textField.textboxKeyTyped(character, code);
        this.currentText = this.textField.getText();
    }

    @Override
    public void updateScreen() {
        this.textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawPanel();
        drawTitle();

        this.textField.drawTextBox();

        if (this.btnPickTag != null) {
            this.btnPickTag.draw(this.fontRendererObj, mouseX, mouseY);
        }
        if (this.btnEditChildren != null) {
            this.btnEditChildren.draw(this.fontRendererObj, mouseX, mouseY);
        }
        this.btnDone.draw(this.fontRendererObj, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
