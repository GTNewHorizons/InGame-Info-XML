package com.github.lunatrius.ingameinfo.client.gui;

import net.minecraft.client.gui.FontRenderer;

import com.github.lunatrius.core.client.gui.FontRendererHelper;

public class InfoText extends Info {

    private final FontRenderer fontRenderer;
    private final String text;

    public InfoText(FontRenderer fontRenderer, String text) {
        this(fontRenderer, text, 0, 0);
    }

    public InfoText(FontRenderer fontRenderer, String text, int x, int y) {
        super(x, y);
        this.fontRenderer = fontRenderer;
        this.text = text;
    }

    @Override
    public void drawInfo() {
        FontRendererHelper.drawLeftAlignedString(this.fontRenderer, this.text, getX(), getY(), 0x00FFFFFF);
    }

    @Override
    public int getWidth() {
        return this.fontRenderer.getStringWidth(this.text);
    }

    @Override
    public int getHeight() {
        return this.fontRenderer.FONT_HEIGHT;
    }

    @Override
    public String toString() {
        return String.format(
                "InfoText{text: %s, x: %d, y: %d, offsetX: %d, offsetY: %d, children: %s}",
                this.text,
                this.x,
                this.y,
                this.offsetX,
                this.offsetY,
                this.children);
    }
}
