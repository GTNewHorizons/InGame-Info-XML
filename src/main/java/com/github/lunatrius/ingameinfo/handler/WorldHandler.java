package com.github.lunatrius.ingameinfo.handler;

import com.github.lunatrius.ingameinfo.network.PacketHandler;
import com.github.lunatrius.ingameinfo.network.message.MessageNextRain;
import com.github.lunatrius.ingameinfo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class WorldHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            try {
                PacketHandler.INSTANCE.sendToDimension(
                        new MessageNextRain(event.world.getWorldInfo().getRainTime()),
                        event.world.provider.dimensionId);
            } catch (Exception ex) {
                Reference.logger.error("Failed to get rain!", ex);
            }
        }
    }
}
