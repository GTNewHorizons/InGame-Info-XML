package com.github.lunatrius.ingameinfo.proxy;

import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.handler.PlayerHandler;
import com.github.lunatrius.ingameinfo.handler.WorldHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new PlayerHandler());
        FMLCommonHandler.instance().bus().register(new WorldHandler());
    }
}
