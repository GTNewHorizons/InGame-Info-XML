package com.github.lunatrius.ingameinfo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.handler.ClientConfigurationHandler;
import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Lets players move, resize and hide the text in Dashboard's HUD editor, one element per screen area lines can be
 * aligned to. The text comes from the player's config, so each area's size is measured from the lines it drew in the
 * last frame, and areas that never drew anything stay out of the editor. The only class using Dashboard, so only call
 * it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    /** In the order of {@link Alignment}. */
    private static final String[] AREA_NAMES = { "top_left", "top_center", "top_right", "middle_left", "middle_center",
            "middle_right", "bottom_left", "bottom_center", "bottom_right" };
    private static final int PLACEHOLDER_WIDTH = 40;

    private static final HudElement[] AREAS = new HudElement[AREA_NAMES.length];
    private static final HudBounds[] lastFrameBounds = new HudBounds[AREA_NAMES.length];
    private static final HudBounds[] currentFrameBounds = new HudBounds[AREA_NAMES.length];

    private DashboardHud() {}

    public static void register() {
        for (int area = 0; area < AREAS.length; area++) {
            int index = area;
            AREAS[area] = new HudElement(
                    "ingameinfoxml:" + AREA_NAMES[area],
                    "dashboard.element.ingameinfo." + AREA_NAMES[area],
                    (screenWidth, screenHeight) -> getAreaBounds(index, screenWidth, screenHeight))
                            .setActiveCheck(() -> lastFrameBounds[index] != null || currentFrameBounds[index] != null)
                            .setOwnSettings("dashboard.element.ingameinfo.own_settings");
            HudRegistry.register(AREAS[area]);
        }
    }

    /**
     * Measures a line into its area and moves it to where the player put the area. The position and width are in the
     * mod's scaled coordinates, the editor works in screen pixels.
     *
     * @return false if the player hid the area, so skip drawing the line and do not call {@link #endLine()}
     */
    public static boolean beginLine(Alignment alignment, int x, int y, int width) {
        int area = alignment.ordinal();
        float scale = getScale();
        HudBounds line = new HudBounds(
                Math.round(x * scale),
                Math.round(y * scale),
                Math.round(width * scale),
                Math.round(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * scale));
        currentFrameBounds[area] = union(currentFrameBounds[area], line);

        if (!HudRegistry.shouldRender(AREAS[area])) {
            return false;
        }
        ScaledResolution resolution = InGameInfoCore.INSTANCE.scaledResolution;
        GL11.glPushMatrix();
        // The line is drawn with the mod's scale applied, so the move is applied without it
        GL11.glScalef(1 / scale, 1 / scale, 1);
        HudRegistry.translateToPosition(AREAS[area], resolution.getScaledWidth(), resolution.getScaledHeight());
        GL11.glScalef(scale, scale, 1);
        return true;
    }

    public static void endLine() {
        HudRegistry.endRender();
    }

    /** Called after all text was drawn. Areas that drew nothing keep their last size. */
    public static void endFrame() {
        for (int area = 0; area < AREAS.length; area++) {
            if (currentFrameBounds[area] != null) {
                lastFrameBounds[area] = currentFrameBounds[area];
                currentFrameBounds[area] = null;
            }
        }
    }

    /** The editor counts as no screen being open, so players see the real text while moving it. */
    public static boolean isEditorOpen() {
        return HudRegistry.isLayoutScreenOpen();
    }

    /** The {@code Scale} config is in tenths, 10 being normal size. */
    private static float getScale() {
        float scale = ClientConfigurationHandler.Scale / 10f;
        return scale > 0 ? scale : 1;
    }

    private static HudBounds union(HudBounds a, HudBounds b) {
        if (a == null) {
            return b;
        }
        int x = Math.min(a.x, b.x);
        int y = Math.min(a.y, b.y);
        return new HudBounds(
                x,
                y,
                Math.max(a.getRight(), b.getRight()) - x,
                Math.max(a.getBottom(), b.getBottom()) - y);
    }

    /** Before the area drew anything, a small box at its corner or edge. */
    private static HudBounds getAreaBounds(int area, int screenWidth, int screenHeight) {
        if (lastFrameBounds[area] != null) {
            return lastFrameBounds[area];
        }
        if (currentFrameBounds[area] != null) {
            return currentFrameBounds[area];
        }
        int fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        int x = area % 3 * (screenWidth - PLACEHOLDER_WIDTH) / 2;
        int y = area / 3 * (screenHeight - fontHeight) / 2;
        return new HudBounds(x, y, PLACEHOLDER_WIDTH, fontHeight);
    }
}
