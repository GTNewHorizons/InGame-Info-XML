package com.github.lunatrius.ingameinfo.handler;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.tag.Tag;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class Ticker {

    public static final Ticker INSTANCE = new Ticker();
    private final Minecraft client = Minecraft.getMinecraft();
    private final InGameInfoCore core = InGameInfoCore.INSTANCE;

    private Ticker() {}

    @SubscribeEvent
    public void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
        if (ConfigurationHandler.showHUD
                && ConfigurationHandler.replaceDebug
                && event.type == RenderGameOverlayEvent.ElementType.DEBUG) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
            this.client.mcProfiler.startSection("ingameinfo");
            if (isRunning()) {
                this.core.onTickClient();
            }
            if (!ConfigurationHandler.showHUD || this.client.gameSettings == null) {
                Tag.setServer(null);
                Tag.releaseResources();
            }
            this.client.mcProfiler.endSection();
        }
    }

    @SubscribeEvent
    public void onRenderGUI(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            this.client.mcProfiler.startSection("ingameinfo");
            if (isRunning()) {
                this.core.onTickRender(event.resolution);
            }
            this.client.mcProfiler.endSection();
        }
    }

    private boolean isRunning() {
        if (ConfigurationHandler.showHUD) {
            if (this.client.mcProfiler.profilingEnabled) {
                return true;
            }

            // a && b || !a && !b  -->  a == b
            if (this.client.gameSettings != null
                    && ConfigurationHandler.replaceDebug == this.client.gameSettings.showDebugInfo) {
                if (!ConfigurationHandler.showOnPlayerList
                        && this.client.gameSettings.keyBindPlayerList.getIsKeyPressed()) {
                    return false;
                }

                if (this.client.gameSettings.hideGUI) {
                    return false;
                }

                if (this.client.currentScreen == null) {
                    return true;
                }

                if (ConfigurationHandler.showInChat && this.client.currentScreen instanceof GuiChat) {
                    return true;
                }
            }
        }

        return false;
    }
}
