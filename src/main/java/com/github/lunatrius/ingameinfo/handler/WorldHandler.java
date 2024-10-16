package com.github.lunatrius.ingameinfo.handler;

import net.minecraft.world.World;

import com.github.lunatrius.ingameinfo.network.PacketHandler;
import com.github.lunatrius.ingameinfo.network.message.MessageNextRain;
import com.github.lunatrius.ingameinfo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class WorldHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (world.playerEntities.isEmpty() || world.getTotalWorldTime() % 20 != 0) {
            return;
        }

        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            try {
                PacketHandler.INSTANCE.sendToDimension(
                        new MessageNextRain(world.getWorldInfo().getRainTime()),
                        world.provider.dimensionId);
            } catch (Exception ex) {
                Reference.logger.error("Failed to get rain!", ex);
            }
        }
    }
}
