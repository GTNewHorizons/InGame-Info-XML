package com.github.lunatrius.ingameinfo.integration.gregtech.tag;

import com.github.lunatrius.ingameinfo.tag.TagIntegration;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.common.GTWorldgenerator;

public abstract class TagGregtech extends TagIntegration {

    @Override
    public String getCategory() {
        return "gregtech";
    }

    public static class useNewOregenPattern extends TagGregtech {

        @Override
        public String getValue() {
            try {
                GTWorldgenerator.OregenPattern pattern = FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                        ? GTWorldgenerator.getServerOregenPattern()
                        : GTWorldgenerator.getClientOregenPattern();
                if (pattern == GTWorldgenerator.OregenPattern.EQUAL_SPACING) {
                    return "true";
                }
                if (pattern == GTWorldgenerator.OregenPattern.AXISSYMMETRICAL) {
                    return "false";
                }
            } catch (Throwable e) {
                log(this, e);
            }
            return "false";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new useNewOregenPattern().setName("gtnewore").setAliases("gtneworegenpattern"));
    }
}
