package com.github.lunatrius.ingameinfo.handler;

import net.minecraftforge.common.MinecraftForge;

import com.github.lunatrius.ingameinfo.tag.Tag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class TickerManager {

    private Ticker ticker = null;

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (this.ticker == null) {
            this.ticker = new Ticker();
            MinecraftForge.EVENT_BUS.register(this.ticker);
            FMLCommonHandler.instance().bus().register(this.ticker);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (this.ticker != null) {
            MinecraftForge.EVENT_BUS.unregister(this.ticker);
            FMLCommonHandler.instance().bus().unregister(this.ticker);
            this.ticker = null;
            Tag.releaseResources();
            Tag.onClientDisconnect();
        }
    }

}
