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
                return Boolean
                        .toString(GTWorldgenerator.getOregenPattern() == GTWorldgenerator.OregenPattern.EQUAL_SPACING);
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
