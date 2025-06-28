package com.github.lunatrius.ingameinfo.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import com.github.lunatrius.ingameinfo.handler.ConfigurationHandler;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.reference.Reference;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class GuiModConfig extends GuiConfig {

    public GuiModConfig(GuiScreen guiScreen) {
        super(
                guiScreen,
                getConfigElements(),
                Reference.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ConfigurationHandler.configuration.toString()));
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = new ArrayList<>();
        for (String name : ConfigurationHandler.configuration.getCategoryNames()) {
            elements.add(
                    new ConfigElement(
                            ConfigurationHandler.configuration.getCategory(name)
                                    .setLanguageKey(Names.Config.LANG_PREFIX + ".category." + name)));
        }
        return elements;
    }
}
