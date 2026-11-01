package com.github.lunatrius.ingameinfo.client.gui;

import net.minecraft.client.Minecraft;

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

    /** In the order of {@link com.github.lunatrius.ingameinfo.Alignment}. */
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
