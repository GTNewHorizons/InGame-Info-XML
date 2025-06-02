package com.github.lunatrius.ingameinfo.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.tag.Tag;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class Ticker {

    private final Minecraft client = Minecraft.getMinecraft();
    private final InGameInfoCore core = InGameInfoCore.INSTANCE;

    @SubscribeEvent
    public void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
        if (ConfigurationHandler.showHUD && ConfigurationHandler.replaceDebug
                && event.type == RenderGameOverlayEvent.ElementType.DEBUG) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.client.mcProfiler.startSection("ingameinfo");
            if (isRunning()) {
                this.core.onTickClient();
            }
            Tag.releaseResources();
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
        if (!ConfigurationHandler.showHUD) return false;

        if (this.client.mcProfiler.profilingEnabled) {
            return true;
        }

        if (ConfigurationHandler.replaceDebug == this.client.gameSettings.showDebugInfo) {

            if (this.client.gameSettings.hideGUI) {
                return false;
            }

            if (!ConfigurationHandler.showOnPlayerList
                    && this.client.gameSettings.keyBindPlayerList.getIsKeyPressed()) {
                if (this.client.theWorld != null && this.client.thePlayer != null) {
                    final ScoreObjective scoreobjective = this.client.theWorld.getScoreboard().func_96539_a(0);
                    final NetHandlerPlayClient handler = this.client.thePlayer.sendQueue;
                    if (!this.client.isIntegratedServerRunning() || handler.playerInfoList.size() > 1
                            || scoreobjective != null) {
                        return false;
                    }
                }
            }

            if (this.client.currentScreen == null) {
                return true;
            }

            return ConfigurationHandler.showInChat && this.client.currentScreen instanceof GuiChat;
        }

        return false;
    }
}
