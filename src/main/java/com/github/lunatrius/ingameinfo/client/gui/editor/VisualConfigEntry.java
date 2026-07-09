package com.github.lunatrius.ingameinfo.client.gui.editor;

import net.minecraft.client.gui.GuiScreen;

import com.github.lunatrius.ingameinfo.reference.Names;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

/**
 * A GuiConfig category row that opens the themed GuiConfigEditor instead of a nested property screen.
 */
public class VisualConfigEntry extends GuiConfigEntries.CategoryEntry {

    @SuppressWarnings("rawtypes")
    public VisualConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        return new GuiConfigEditor(this.owningScreen);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IConfigElement createConfigElement() {
        return new DummyConfigElement.DummyCategoryElement(
                "visualconfig",
                Names.Config.LANG_PREFIX + ".category.visualconfig",
                VisualConfigEntry.class);
    }
}
