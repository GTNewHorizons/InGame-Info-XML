package com.github.lunatrius.ingameinfo.integration.gregtech.tag;

import com.github.lunatrius.ingameinfo.tag.TagIntegration;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;

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
                if (GTWorldgenerator.oregenPattern == GTWorldgenerator.OregenPattern.EQUAL_SPACING) {
                    return "true";
                }
                if (GTWorldgenerator.oregenPattern == GTWorldgenerator.OregenPattern.AXISSYMMETRICAL) {
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
