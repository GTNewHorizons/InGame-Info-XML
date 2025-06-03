package com.github.lunatrius.ingameinfo.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.ClientCommandHandler;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.command.InGameInfoCommand;
import com.github.lunatrius.ingameinfo.handler.ClientConfigurationHandler;
import com.github.lunatrius.ingameinfo.handler.KeyInputHandler;
import com.github.lunatrius.ingameinfo.handler.TickerManager;
import com.github.lunatrius.ingameinfo.integration.PluginLoader;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.tag.Tag;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import com.github.lunatrius.ingameinfo.value.registry.ValueRegistry;

import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class ClientProxy extends CommonProxy {

    private final InGameInfoCore core = InGameInfoCore.INSTANCE;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientConfigurationHandler.init(event.getSuggestedConfigurationFile());

        ValueRegistry.INSTANCE.init();

        PluginLoader.getInstance().preInit(event);

        this.core.moveConfig(event.getModConfigurationDirectory(), ClientConfigurationHandler.configName);
        this.core.setConfigDirectory(
                event.getModConfigurationDirectory().toPath().resolve(Names.Files.SUBDIRECTORY).toFile());
        this.core.setConfigFileWithLocale(ClientConfigurationHandler.configName);
        this.core.reloadConfig();

        ClientConfigurationHandler.propFileInterval.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        ClientConfigurationHandler.propscale.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        FMLCommonHandler.instance().bus().register(new TickerManager());
        FMLCommonHandler.instance().bus().register(ClientConfigurationHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
        ClientCommandHandler.instance.registerCommand(new InGameInfoCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(ClientConfigurationHandler.INSTANCE);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        PluginLoader.getInstance().postInit(event);
        TagRegistry.INSTANCE.init();
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        Tag.setServer(event.getServer());
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
        Tag.setServer(null);
    }
}
