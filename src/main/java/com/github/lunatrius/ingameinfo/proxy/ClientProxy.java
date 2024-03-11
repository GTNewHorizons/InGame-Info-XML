package com.github.lunatrius.ingameinfo.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.command.InGameInfoCommand;
import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.handler.KeyInputHandler;
import com.github.lunatrius.ingameinfo.handler.Ticker;
import com.github.lunatrius.ingameinfo.integration.PluginLoader;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.tag.Tag;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;
import com.github.lunatrius.ingameinfo.value.registry.ValueRegistry;

import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.registry.ClientRegistry;
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

        ValueRegistry.INSTANCE.init();

        PluginLoader.getInstance().preInit(event);

        this.core.moveConfig(event.getModConfigurationDirectory(), ConfigurationHandler.configName);
        this.core.setConfigDirectory(
                event.getModConfigurationDirectory().toPath().resolve(Names.Files.SUBDIRECTORY).toFile());
        this.core.setConfigFileWithLocale(ConfigurationHandler.configName);
        this.core.reloadConfig();

        ConfigurationHandler.propFileInterval.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
        ConfigurationHandler.propscale.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

        for (KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(Ticker.INSTANCE);
        FMLCommonHandler.instance().bus().register(Ticker.INSTANCE);
        FMLCommonHandler.instance().bus().register(ConfigurationHandler.INSTANCE);
        FMLCommonHandler.instance().bus().register(KeyInputHandler.INSTANCE);
        ClientCommandHandler.instance.registerCommand(InGameInfoCommand.INSTANCE);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(ConfigurationHandler.INSTANCE);
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
