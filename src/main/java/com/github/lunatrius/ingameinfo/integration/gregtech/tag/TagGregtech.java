package com.github.lunatrius.ingameinfo.integration.gregtech.tag;

import com.github.lunatrius.ingameinfo.tag.TagIntegration;
import com.github.lunatrius.ingameinfo.tag.registry.TagRegistry;

import gregtech.common.GT_Worldgenerator;

public abstract class TagBloodMagic extends TagIntegration {

    @Override
    public String getCategory() {
        return "gregtech";
    }

    public static class useNewOregenPattern extends TagGregtech {

        @Override
        public String getValue() {
            try {
                return String.valueOf(GT_Worldgenerator.useNewOregenPattern);
            } catch (Throwable e) {
                log(this, e);
            }
            return "-1";
        }
    }

    public static void register() {
        TagRegistry.INSTANCE.register(new useNewOregenPattern().setName("gtnewore").setAliases("gtneworegenpattern"));
    }
}
