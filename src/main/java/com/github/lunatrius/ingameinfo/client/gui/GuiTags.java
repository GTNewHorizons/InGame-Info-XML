package com.github.lunatrius.ingameinfo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiTags extends GuiScreen {

    private GuiTagList guiTagList;
    private GuiTextField guiTextField;
    private GuiButton btnDone;

    private final String strTagList = I18n.format("gui.ingameinfoxml.taglist");

    @Override
    public void initGui() {
        this.guiTagList = new GuiTagList(this, Minecraft.getMinecraft());
        this.guiTextField = new GuiTextField(this.fontRendererObj, this.width / 2 - 155, this.height - 24, 150, 18);
        this.btnDone = new GuiButton(0, this.width / 2 + 5, this.height - 25, 150, 20, I18n.format("gui.done"));
        this.buttonList.add(this.btnDone);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == this.btnDone.id) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int action) {
        this.guiTextField.mouseClicked(x, y, action);
        if (action != 0 || !this.guiTagList.func_148179_a(x, y, action)) {
            super.mouseClicked(x, y, action);
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int mouseEvent) {
        if (mouseEvent != 0 || !this.guiTagList.func_148181_b(x, y, mouseEvent)) {
            super.mouseMovedOrUp(x, y, mouseEvent);
        }
    }

    @Override
    protected void keyTyped(char character, int code) {
        this.guiTextField.textboxKeyTyped(character, code);
        this.guiTagList.filter(this.guiTextField.getText().toLowerCase());
        super.keyTyped(character, code);
    }

    @Override
    public void updateScreen() {
        this.guiTextField.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        this.guiTagList.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.fontRendererObj, this.strTagList, this.width / 2, 5, 0xFFFFFF);
        this.guiTextField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
