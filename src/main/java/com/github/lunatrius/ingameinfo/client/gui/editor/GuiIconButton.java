package com.github.lunatrius.ingameinfo.client.gui.editor;

public class GuiIconButton {

    private static final int SIZE = 16;

    public int x;
    public int y;
    public boolean enabled = true;
    private final VisualConfigTheme.RowIcon icon;
    private final String tooltip;

    public GuiIconButton(int x, int y, VisualConfigTheme.RowIcon icon, String tooltip) {
        this.x = x;
        this.y = y;
        this.icon = icon;
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return this.tooltip;
    }

    public boolean containsMouse(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + SIZE && mouseY < this.y + SIZE;
    }

    public boolean mousePressed(int mouseX, int mouseY) {
        if (!this.enabled || !containsMouse(mouseX, mouseY)) {
            return false;
        }

        VisualConfigTheme.playClickSound();
        return true;
    }

    public void draw(int mouseX, int mouseY) {
        boolean hovered = this.enabled && containsMouse(mouseX, mouseY);
        VisualConfigTheme.ButtonState state = !this.enabled ? VisualConfigTheme.ButtonState.DISABLED
                : hovered ? VisualConfigTheme.ButtonState.HOVERED : VisualConfigTheme.ButtonState.NORMAL;
        VisualConfigTheme.drawButton(this.x, this.y, SIZE, state);
        VisualConfigTheme.drawIcon(this.x, this.y, this.icon, this.enabled);
    }
}
