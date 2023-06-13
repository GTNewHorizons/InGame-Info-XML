package com.github.lunatrius.ingameinfo.integration.gregtech;

import com.github.lunatrius.ingameinfo.integration.Plugin;
import com.github.lunatrius.ingameinfo.integration.gregtech.tag.TagGregtech;
import com.github.lunatrius.ingameinfo.reference.Names;

@SuppressWarnings("UnusedDeclaration")
public class Gregtech extends Plugin {

    @Override
    public String getDependency() {
        return Names.Mods.GREGTECH_MODID;
    }

    @Override
    public String getDependencyName() {
        return Names.Mods.GREGTECH_NAME;
    }

    @Override
    public void load() {
        TagGregtech.register();
    }
}
