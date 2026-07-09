package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class GuiTexturedButton {

    public final int id;
    public int x;
    public int y;
    public int width;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean selected = false;
    private String label;

    public GuiTexturedButton(int id, int x, int y, int width, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.visible && this.enabled
                && mouseX >= this.x
                && mouseY >= this.y
                && mouseX < this.x + this.width
                && mouseY < this.y + VisualConfigTheme.BUTTON_HEIGHT;
    }

    public boolean mousePressed(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        Minecraft.getMinecraft().getSoundHandler()
                .playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
        return true;
    }

    public void draw(FontRenderer fontRenderer, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        boolean hovered = isMouseOver(mouseX, mouseY);
        VisualConfigTheme.ButtonState state;
        if (!this.enabled) {
            state = VisualConfigTheme.ButtonState.DISABLED;
        } else if (this.selected) {
            state = hovered ? VisualConfigTheme.ButtonState.PRESSED_HOVERED : VisualConfigTheme.ButtonState.PRESSED;
        } else {
            state = hovered ? VisualConfigTheme.ButtonState.HOVERED : VisualConfigTheme.ButtonState.NORMAL;
        }
        VisualConfigTheme.drawButton(this.x, this.y, this.width, state);

        String text = VisualConfigTheme.colorize(this.label, this.enabled);
        fontRenderer.drawStringWithShadow(
                text,
                this.x + (this.width - fontRenderer.getStringWidth(text)) / 2,
                this.y + (VisualConfigTheme.BUTTON_HEIGHT - fontRenderer.FONT_HEIGHT) / 2 + 1,
                0xFFFFFF);
    }
}
