package com.github.lunatrius.ingameinfo.client.gui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.value.Value;

public class InfoText extends Info {

    private static final String ICON_START = "{ICON|";
    private final Map<String, Info> attachedValues = new LinkedHashMap<>();
    private String text;
    private final List<Value> values;
    private final Alignment alignment;
    private final int index;

    public InfoText(int index, Alignment alignment, List<Value> values) {
        super(0, 0);
        this.values = values;
        this.alignment = alignment;
        this.index = index;
        for (Value value : values) {
            value.setParent(this);
        }
    }

    public void update() {
        StringBuilder builder = new StringBuilder();
        for (Value value : this.values) {
            builder.append(getValue(value));
        }

        updateChildren(builder);
        text = builder.toString();
        updatePosition();
    }

    @Override
    public void drawInfo() {
        fontRenderer.drawStringWithShadow(text, getX(), getY(), 0x00FFFFFF);

        for (Info child : attachedValues.values()) {
            child.offsetX = x;
            child.offsetY = y;
            child.draw();
        }
    }

    private void updateChildren(StringBuilder builder) {
        if (builder.length() == 0 && !attachedValues.isEmpty()) {
            attachedValues.clear();
            return;
        }

        if (builder.indexOf(ICON_START) == -1) {
            return;
        }

        for (Info child : attachedValues.values()) {
            if (child.hasPosition) continue;
            int iconStart = builder.indexOf(ICON_START);
            int widthStart = builder.indexOf("|", iconStart) + 1;
            child.hasPosition = true;
            child.x = fontRenderer.getStringWidth(builder.substring(0, iconStart));
            builder.replace(iconStart, widthStart, "");
            builder.deleteCharAt(builder.indexOf("}"));
        }
    }

    private void updatePosition() {
        int scaledWidth = InGameInfoCore.INSTANCE.scaledWidth;
        int scaledHeight = InGameInfoCore.INSTANCE.scaledHeight;
        x = alignment.getX(scaledWidth, fontRenderer.getStringWidth(text));
        y = alignment.getY(scaledHeight, getHeight());
    }

    public @Nullable Info getAttachedValue(String tag) {
        return attachedValues.get(tag);
    }

    public void removeAttachedValue(String tag) {
        attachedValues.remove(tag);
    }

    public void attachValue(@NotNull String tag, @NotNull Info value) {
        attachedValues.put(tag, value);
    }

    @Override
    public int getWidth() {
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public int getHeight() {
        if (alignment.ordinal() >= Alignment.BOTTOMLEFT.ordinal()) {
            return (index + 1) * (fontRenderer.FONT_HEIGHT + 1);
        }
        return index * (fontRenderer.FONT_HEIGHT + 1);
    }

    private String getValue(Value value) {
        try {
            if (value.isValidSize()) {
                return value.getReplacedValue();
            }
        } catch (Exception e) {
            Reference.logger.debug("Failed to get value!", e);
            return "null";
        }

        return "";
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
