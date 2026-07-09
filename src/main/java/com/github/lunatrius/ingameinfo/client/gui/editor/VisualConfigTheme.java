package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.ingameinfo.reference.Names;

public final class VisualConfigTheme {

    public static final ResourceLocation TEXTURE = new ResourceLocation("ingameinfo", "textures/gui/visual_config.png");

    private static final int TEXTURE_SIZE = 256;

    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;
    private static final int PANEL_SIZE = 21;
    private static final int PANEL_CORNER = 7;

    private static final int BUTTON_WIDTH = 16;
    public static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_CAP = 2;

    private static final int SCROLLBAR_THUMB_WIDTH = 7;
    public static final int SCROLLBAR_RAIL_WIDTH = 9;
    private static final int SCROLLBAR_HEIGHT = 12;
    private static final int SCROLLBAR_CAP = 4;
    private static final int SCROLLBAR_THUMB_MIN_HEIGHT = 10;

    private static final int ICON_SIZE = 16;
    private static final int ICON_V_NORMAL = 64;
    private static final int ICON_V_DISABLED = 80;

    private static final int ROW_BACKGROUND_U_NORMAL = 0;
    private static final int ROW_BACKGROUND_U_HOVERED = 20;
    private static final int ROW_BACKGROUND_V = 32;
    private static final int ROW_BACKGROUND_SIZE = 20;

    public enum RowIcon {

        UP(0),
        DOWN(16),
        DELETE(32);

        private final int u;

        RowIcon(int u) {
            this.u = u;
        }
    }

    public enum ButtonState {

        NORMAL(21, 0),
        HOVERED(37, 0),
        DISABLED(53, 0),
        PRESSED(21, 16),
        PRESSED_HOVERED(37, 16);

        private final int u;
        private final int v;

        ButtonState(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    private VisualConfigTheme() {}

    public static String colorize(String text, boolean enabled) {
        String colorPrefix = I18n.format(enabled ? Names.VisualConfig.TEXT_ENABLED : Names.VisualConfig.TEXT_DISABLED);
        return colorPrefix + text;
    }

    public static void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler()
                .playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public static void bind() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        // Stop blinking machine 5000, patent pending
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawIcon(int x, int y, RowIcon icon, boolean enabled) {
        bind();
        int v = enabled ? ICON_V_NORMAL : ICON_V_DISABLED;
        drawStretched(x, y, ICON_SIZE, ICON_SIZE, icon.u, v, ICON_SIZE, ICON_SIZE);
    }

    /**
     * Tiles the 20x20 row-background pattern across the given width instead of stretching it, so the pattern repeats
     * cleanly regardless of row width. The trailing partial tile (if width isn't a multiple of 20) is cropped from the
     * source rather than squashed.
     */
    public static void drawLineBackground(int x, int y, int width, boolean hovered) {
        bind();
        int u = hovered ? ROW_BACKGROUND_U_HOVERED : ROW_BACKGROUND_U_NORMAL;
        int fullTiles = width / ROW_BACKGROUND_SIZE;
        int remainder = width % ROW_BACKGROUND_SIZE;

        int drawX = x;
        for (int i = 0; i < fullTiles; i++) {
            drawStretched(
                    drawX,
                    y,
                    ROW_BACKGROUND_SIZE,
                    ROW_BACKGROUND_SIZE,
                    u,
                    ROW_BACKGROUND_V,
                    ROW_BACKGROUND_SIZE,
                    ROW_BACKGROUND_SIZE);
            drawX += ROW_BACKGROUND_SIZE;
        }
        if (remainder > 0) {
            drawStretched(
                    drawX,
                    y,
                    remainder,
                    ROW_BACKGROUND_SIZE,
                    u,
                    ROW_BACKGROUND_V,
                    remainder,
                    ROW_BACKGROUND_SIZE);
        }
    }

    public static void drawPanel(int x, int y, int width, int height) {
        bind();
        drawSliced(
                x,
                y,
                width,
                height,
                PANEL_U,
                PANEL_V,
                PANEL_SIZE,
                PANEL_SIZE,
                PANEL_CORNER,
                PANEL_CORNER,
                PANEL_CORNER,
                PANEL_CORNER);
    }

    public static void drawButton(int x, int y, int width, ButtonState state) {
        bind();
        drawSliced(
                x,
                y,
                width,
                BUTTON_HEIGHT,
                state.u,
                state.v,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                BUTTON_CAP,
                BUTTON_CAP,
                0,
                0);
    }

    public static void drawScrollbarThumb(int x, int y, int height, boolean active) {
        bind();
        int u = active ? 69 : 76;
        drawSliced(
                x,
                y,
                SCROLLBAR_THUMB_WIDTH,
                height,
                u,
                0,
                SCROLLBAR_THUMB_WIDTH,
                SCROLLBAR_HEIGHT,
                0,
                0,
                SCROLLBAR_CAP,
                SCROLLBAR_CAP);
    }

    public static void drawScrollbarRail(int x, int y, int height) {
        bind();
        drawSliced(
                x,
                y,
                SCROLLBAR_RAIL_WIDTH,
                height,
                83,
                0,
                SCROLLBAR_RAIL_WIDTH,
                SCROLLBAR_HEIGHT,
                0,
                0,
                SCROLLBAR_CAP,
                SCROLLBAR_CAP);
    }

    /**
     * Draws a rail spanning the full content height, plus a thumb sized/positioned to reflect how much of totalItems is
     * visible and how far scrolled. Draws no thumb if everything already fits.
     */
    public static void drawScrollbar(int x, int y, int height, int totalItems, int visibleItems, int scrollOffset) {
        drawScrollbarRail(x, y, height);

        if (totalItems <= visibleItems) {
            return;
        }

        int thumbX = x + (SCROLLBAR_RAIL_WIDTH - SCROLLBAR_THUMB_WIDTH) / 2;
        int margin = 1;
        int travelHeight = height - margin * 2;
        int thumbHeight = Math
                .min(travelHeight, Math.max(SCROLLBAR_THUMB_MIN_HEIGHT, travelHeight * visibleItems / totalItems));

        int maxOffset = totalItems - visibleItems;
        int thumbY = y + margin + (travelHeight - thumbHeight) * scrollOffset / maxOffset;
        drawScrollbarThumb(thumbX, thumbY, thumbHeight, false);
    }

    /**
     * Inverse of drawScrollbar's thumb positioning: given a click/drag Y on the track, returns the scrollOffset that
     * would center the thumb under that point. Mirrors the same thumb-size math so dragging feels 1:1 with what's
     * rendered.
     */
    public static int scrollOffsetForY(int mouseY, int trackY, int trackHeight, int totalItems, int visibleItems) {
        int maxOffset = totalItems - visibleItems;
        if (maxOffset <= 0) {
            return 0;
        }

        int margin = 1;
        int travelHeight = trackHeight - margin * 2;
        int thumbHeight = Math
                .min(travelHeight, Math.max(SCROLLBAR_THUMB_MIN_HEIGHT, travelHeight * visibleItems / totalItems));
        int usableTravel = Math.max(1, travelHeight - thumbHeight);

        int relativeY = mouseY - trackY - margin - thumbHeight / 2;
        float ratio = Math.max(0F, Math.min(1F, relativeY / (float) usableTravel));
        return Math.round(ratio * maxOffset);
    }

    /**
     * Draws a 9-slice: fixed-size corners, edges stretched along one axis, center stretched along both. A cap of 0 on
     * an axis collapses that axis down to a single stretched strip (used for 3-slice buttons/scrollbar).
     */
    private static void drawSliced(int x, int y, int width, int height, int u, int v, int srcWidth, int srcHeight,
            int capLeft, int capRight, int capTop, int capBottom) {
        int midSrcW = srcWidth - capLeft - capRight;
        int midSrcH = srcHeight - capTop - capBottom;
        int midDstW = width - capLeft - capRight;
        int midDstH = height - capTop - capBottom;

        if (capLeft > 0 && capTop > 0) {
            drawStretched(x, y, capLeft, capTop, u, v, capLeft, capTop);
        }
        if (capRight > 0 && capTop > 0) {
            drawStretched(x + width - capRight, y, capRight, capTop, u + srcWidth - capRight, v, capRight, capTop);
        }
        if (capLeft > 0 && capBottom > 0) {
            drawStretched(
                    x,
                    y + height - capBottom,
                    capLeft,
                    capBottom,
                    u,
                    v + srcHeight - capBottom,
                    capLeft,
                    capBottom);
        }
        if (capRight > 0 && capBottom > 0) {
            drawStretched(
                    x + width - capRight,
                    y + height - capBottom,
                    capRight,
                    capBottom,
                    u + srcWidth - capRight,
                    v + srcHeight - capBottom,
                    capRight,
                    capBottom);
        }

        if (capTop > 0 && midDstW > 0) {
            drawStretched(x + capLeft, y, midDstW, capTop, u + capLeft, v, midSrcW, capTop);
        }
        if (capBottom > 0 && midDstW > 0) {
            drawStretched(
                    x + capLeft,
                    y + height - capBottom,
                    midDstW,
                    capBottom,
                    u + capLeft,
                    v + srcHeight - capBottom,
                    midSrcW,
                    capBottom);
        }
        if (capLeft > 0 && midDstH > 0) {
            drawStretched(x, y + capTop, capLeft, midDstH, u, v + capTop, capLeft, midSrcH);
        }
        if (capRight > 0 && midDstH > 0) {
            drawStretched(
                    x + width - capRight,
                    y + capTop,
                    capRight,
                    midDstH,
                    u + srcWidth - capRight,
                    v + capTop,
                    capRight,
                    midSrcH);
        }

        if (midDstW > 0 && midDstH > 0) {
            drawStretched(x + capLeft, y + capTop, midDstW, midDstH, u + capLeft, v + capTop, midSrcW, midSrcH);
        }
    }

    private static void drawStretched(double x, double y, double destWidth, double destHeight, double u, double v,
            double srcWidth, double srcHeight) {
        float texel = 1F / TEXTURE_SIZE;
        float u0 = (float) (u * texel);
        float u1 = (float) ((u + srcWidth) * texel);
        float v0 = (float) (v * texel);
        float v1 = (float) ((v + srcHeight) * texel);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + destHeight, 0, u0, v1);
        tessellator.addVertexWithUV(x + destWidth, y + destHeight, 0, u1, v1);
        tessellator.addVertexWithUV(x + destWidth, y, 0, u1, v0);
        tessellator.addVertexWithUV(x, y, 0, u0, v0);
        tessellator.draw();
    }
}
