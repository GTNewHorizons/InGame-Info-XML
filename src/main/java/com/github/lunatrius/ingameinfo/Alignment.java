package com.github.lunatrius.ingameinfo;

import java.util.ArrayList;
import java.util.Collection;

import com.github.lunatrius.ingameinfo.client.gui.InfoText;

public enum Alignment {

    TOPLEFT(2, 2),
    TOPCENTER(0, 2),
    TOPRIGHT(-2, 2),
    MIDDLELEFT(2, 0),
    MIDDLECENTER(0, 0),
    MIDDLERIGHT(-2, 0),
    BOTTOMLEFT(2, -2),
    BOTTOMCENTER(0, -45),
    BOTTOMRIGHT(-2, -2);

    public static final Alignment[] VALUES = values();

    private static final int MASK_X = 0x0C;
    private static final int MASK_Y = 0x03;

    private static final int TOP = 0x01;
    private static final int MIDDLE = 0x03;
    private static final int BOTTOM = 0x02;

    private static final int LEFT = 0x04;
    private static final int CENTER = 0x0C;
    private static final int RIGHT = 0x08;

    private final Collection<InfoText> lines = new ArrayList<>();
    private int alignment;
    public final int defaultX;
    public final int defaultY;
    public int x;
    public int y;

    Alignment(int x, int y) {
        this.defaultX = x;
        this.defaultY = y;
        this.x = x;
        this.y = y;
        this.alignment = 0;
    }

    public static Alignment parse(String str) {
        String value = "";

        str = str.toLowerCase();

        if (str.startsWith("top")) {
            value = "TOP";
        } else if (str.startsWith("mid")) {
            value = "MIDDLE";
        } else if (str.startsWith("bot")) {
            value = "BOTTOM";
        }

        if (str.endsWith("left")) {
            value += "LEFT";
        } else if (str.endsWith("center")) {
            value += "CENTER";
        } else if (str.endsWith("right")) {
            value += "RIGHT";
        }

        try {
            return valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    public int getX(int screenwidth, int textwidth) {
        return switch (this.alignment & MASK_X) {
            case LEFT -> this.x;
            case CENTER -> this.x + (screenwidth - textwidth) / 2;
            case RIGHT -> this.x + screenwidth - textwidth;
            default -> 0;
        };

    }

    public int getY(int screenheight, int textheight) {
        return switch (this.alignment & MASK_Y) {
            case TOP -> this.y + textheight;
            case MIDDLE -> this.y + textheight + screenheight / 2;
            case BOTTOM -> this.y + screenheight - textheight;
            default -> 0;
        };

    }

    public String getDefaultXY() {
        return String.format("%d %d", this.defaultX, this.defaultY);
    }

    public String getXY() {
        return String.format("%d %d", this.x, this.y);
    }

    public void setXY(String str) {
        try {
            String[] parts = str.split(" ");
            if (parts.length == 2) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                this.x = x;
                this.y = y;
            }
        } catch (Exception ignored) {}
    }

    public Collection<InfoText> getLines() {
        return this.lines;
    }

    static {
        TOPLEFT.alignment = TOP | LEFT;
        TOPCENTER.alignment = TOP | CENTER;
        TOPRIGHT.alignment = TOP | RIGHT;
        MIDDLELEFT.alignment = MIDDLE | LEFT;
        MIDDLECENTER.alignment = MIDDLE | CENTER;
        MIDDLERIGHT.alignment = MIDDLE | RIGHT;
        BOTTOMLEFT.alignment = BOTTOM | LEFT;
        BOTTOMCENTER.alignment = BOTTOM | CENTER;
        BOTTOMRIGHT.alignment = BOTTOM | RIGHT;
    }
}
