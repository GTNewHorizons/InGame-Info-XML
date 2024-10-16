package com.github.lunatrius.ingameinfo.handler;

import static com.github.lunatrius.ingameinfo.proxy.ClientProxy.KEY_BINDING_TOGGLE;
import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class KeyInputHandler {

    public static final KeyInputHandler INSTANCE = new KeyInputHandler();

    private KeyInputHandler() {}

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (KEY_BINDING_TOGGLE.isPressed()) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                ConfigurationHandler.showHUD = !ConfigurationHandler.showHUD;
                ConfigurationHandler.saveHUDsettingToFile();
            }
        }
    }
}
