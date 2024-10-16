package com.github.lunatrius.ingameinfo.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.github.lunatrius.core.client.gui.FontRendererHelper;
import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.reference.Reference;
import com.github.lunatrius.ingameinfo.value.Value;

public class InfoText extends Info {

    private static final Pattern ICON_PATTERN = Pattern.compile("\\{ICON\\|( *)\\}", Pattern.CASE_INSENSITIVE);
    private static final Matcher ICON_MATCHER = ICON_PATTERN.matcher("");
    private final Map<String, Info> attachedValues = new HashMap<>();
    private String text;
    private final List<Value> values;
    private final Alignment alignment;
    private final int index;
    private boolean needsUpdate = true;

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
        text = builder.toString();
        updatePosition();
    }

    @Override
    public void drawInfo() {
        if (needsUpdate) {
            updateChildren();
            needsUpdate = false;
        }

        FontRendererHelper.drawLeftAlignedString(fontRenderer, text, getX(), getY(), 0x00FFFFFF);

        for (Info child : attachedValues.values()) {
            child.offsetX = x;
            child.offsetY = y;
            child.draw();
        }
    }

    private void updateChildren() {
        if (attachedValues.isEmpty()) {
            return;
        }

        ICON_MATCHER.reset(text);
        for (Info child : attachedValues.values()) {
            if (!ICON_MATCHER.find()) break;
            int newX = fontRenderer.getStringWidth(text.substring(0, ICON_MATCHER.start()));
            if (newX == 0) {
                offsetX = child.getWidth();
            }

            child.x = newX;
            text = text.replaceFirst(Pattern.quote(ICON_MATCHER.group(0)), ICON_MATCHER.group(1));
            ICON_MATCHER.reset(text);
        }
        updatePosition();
    }

    private void updatePosition() {
        int scaledWidth = InGameInfoCore.INSTANCE.scaledWidth;
        int scaledHeight = InGameInfoCore.INSTANCE.scaledHeight;
        x = alignment.getX(scaledWidth, getWidth());
        y = alignment.getY(scaledHeight, getHeight()) + getHeight();
    }

    public @Nullable Info getAttachedValue(String tag) {
        return attachedValues.get(tag);
    }

    public void removeAttachedValue(String tag) {
        attachedValues.remove(tag);
    }

    public void attachValue(@NotNull String tag, @NotNull Info value) {
        Info old = attachedValues.get(tag);
        if (old != null) {
            value.y = old.y;
            value.x = old.x;
        } else {
            needsUpdate = true;
        }

        if (value.x == 0) {
            offsetX = value.getWidth();
        }
        attachedValues.put(tag, value);
    }

    @Override
    public int getWidth() {
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public int getHeight() {
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
