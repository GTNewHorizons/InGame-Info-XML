package com.github.lunatrius.ingameinfo.handler;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import com.github.lunatrius.ingameinfo.InGameInfoCore;

public class ClientConfigurationHandler extends ConfigurationHandler implements IResourceManagerReloadListener {

    public static final ClientConfigurationHandler INSTANCE = new ClientConfigurationHandler();

    private ClientConfigurationHandler() {
        super();
    }

    @Override
    public void onResourceManagerReload(IResourceManager p_110549_1_) {
        InGameInfoCore.INSTANCE.setConfigFileWithLocale();
        InGameInfoCore.INSTANCE.reloadConfig();
    }
}
