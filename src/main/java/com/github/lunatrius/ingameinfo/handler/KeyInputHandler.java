package com.github.lunatrius.ingameinfo.handler;

import static cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

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
                ConfigurationHandler.showHUD = !ConfigurationHandler.showHUD;
                ConfigurationHandler.saveHUDsettingToFile();
            }
        }
    }
}
