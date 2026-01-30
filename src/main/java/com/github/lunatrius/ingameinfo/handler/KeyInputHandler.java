package com.github.lunatrius.ingameinfo.handler;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.github.lunatrius.ingameinfo.InGameInfoCore;
import com.github.lunatrius.ingameinfo.reference.Names;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class KeyInputHandler {

    private static final KeyBinding KEY_BINDING_TOGGLE = new KeyBinding(
            Names.Keys.TOGGLE,
            Keyboard.KEY_NONE,
            Names.Keys.CATEGORY);

    public KeyInputHandler() {
        ClientRegistry.registerKeyBinding(KEY_BINDING_TOGGLE);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (KEY_BINDING_TOGGLE.isPressed()) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                toggleHud();
            }
        }
    }

    private void toggleHud() {
        String primaryConfig = ClientConfigurationHandler.configName;
        String secondaryConfig = getSecondaryConfigName(primaryConfig);
        InGameInfoCore core = InGameInfoCore.INSTANCE;
        boolean hasSecondary = core.hasConfigFileWithLocale(secondaryConfig);

        if (!hasSecondary) {
            ConfigurationHandler.showHUD = !ConfigurationHandler.showHUD;
            ConfigurationHandler.saveHUDsettingToFile();
            return;
        }

        if (!ConfigurationHandler.showHUD) {
            ConfigurationHandler.showHUD = true;
            ConfigurationHandler.saveHUDsettingToFile();
            core.setConfigFileWithLocale(primaryConfig);
            core.reloadConfig();
            return;
        }

        String currentConfig = core.getBaseConfigFileName();
        if (currentConfig != null && secondaryConfig.equalsIgnoreCase(currentConfig)) {
            ConfigurationHandler.showHUD = false;
            ConfigurationHandler.saveHUDsettingToFile();
            return;
        }

        core.setConfigFileWithLocale(secondaryConfig);
        core.reloadConfig();
    }

    private static String getSecondaryConfigName(String primaryConfig) {
        if (primaryConfig == null || primaryConfig.isEmpty()) {
            return "InGameInfo2.xml";
        }

        int dotIndex = primaryConfig.lastIndexOf('.');
        if (dotIndex < 0) {
            return primaryConfig + "2";
        }

        return primaryConfig.substring(0, dotIndex) + "2" + primaryConfig.substring(dotIndex);
    }
}
