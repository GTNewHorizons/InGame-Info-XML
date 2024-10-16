package com.github.lunatrius.ingameinfo.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.github.lunatrius.ingameinfo.Alignment;
import com.github.lunatrius.ingameinfo.reference.Names;
import com.github.lunatrius.ingameinfo.reference.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigurationHandler {

    public static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static Configuration configuration;

    public static final String CONFIG_NAME_DEFAULT = Names.Files.FILE_XML;
    public static final boolean SHOW_HUD_DEFAULT = true;
    public static final boolean REPLACE_DEBUG_DEFAULT = false;
    public static final boolean SHOW_IN_CHAT_DEFAULT = true;
    public static final boolean SHOW_ON_PLAYER_LIST_DEFAULT = true;
    public static final int DEFAULT_SCALE = 10;
    public static final int FILE_INTERVAL_DEFAULT = 5;

    public static String configName = CONFIG_NAME_DEFAULT;
    public static boolean showHUD = SHOW_HUD_DEFAULT;
    public static boolean replaceDebug = REPLACE_DEBUG_DEFAULT;
    public static boolean showInChat = SHOW_IN_CHAT_DEFAULT;
    public static boolean showOnPlayerList = SHOW_ON_PLAYER_LIST_DEFAULT;
    public static float Scale = (float) DEFAULT_SCALE;
    public static int fileInterval = FILE_INTERVAL_DEFAULT;

    public static Property propConfigName = null;
    public static Property propShowHUD = null;
    public static Property propReplaceDebug = null;
    public static Property propShowInChat = null;
    public static Property propShowOnPlayerList = null;
    public static Property propscale = null;
    public static Property propFileInterval = null;
    public static final Map<Alignment, Property> propAlignments = new HashMap<>();

    protected ConfigurationHandler() {}

    public static void init(File configFile) {
        if (configuration == null) {
            configuration = new Configuration(configFile);
            loadConfiguration();
        }
    }

    protected static void loadConfiguration() {

        // spotless:off

        propConfigName = configuration.get(Names.Config.Category.GENERAL, Names.Config.FILENAME, CONFIG_NAME_DEFAULT, Names.Config.FILENAME_DESC);
        propConfigName.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.FILENAME);
        propConfigName.setRequiresMcRestart(true);
        configName = propConfigName.getString();

        propShowHUD = configuration.get(Names.Config.Category.GENERAL, Names.Config.SHOW_HUD, SHOW_HUD_DEFAULT, Names.Config.SHOW_HUD_DESC);
        propShowHUD.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SHOW_HUD);
        propShowHUD.setRequiresMcRestart(false);
        showHUD = propShowHUD.getBoolean(SHOW_HUD_DEFAULT);

        propReplaceDebug = configuration.get(Names.Config.Category.GENERAL, Names.Config.REPLACE_DEBUG, REPLACE_DEBUG_DEFAULT, Names.Config.REPLACE_DEBUG_DESC);
        propReplaceDebug.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.REPLACE_DEBUG);
        replaceDebug = propReplaceDebug.getBoolean(REPLACE_DEBUG_DEFAULT);

        propShowInChat = configuration.get(Names.Config.Category.GENERAL, Names.Config.SHOW_IN_CHAT, SHOW_IN_CHAT_DEFAULT, Names.Config.SHOW_IN_CHAT_DESC);
        propShowInChat.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SHOW_IN_CHAT);
        showInChat = propShowInChat.getBoolean(SHOW_IN_CHAT_DEFAULT);

        propShowOnPlayerList = configuration.get(Names.Config.Category.GENERAL, Names.Config.SHOW_ON_PLAYER_LIST, SHOW_ON_PLAYER_LIST_DEFAULT, Names.Config.SHOW_ON_PLAYER_LIST_DESC);
        propShowOnPlayerList.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SHOW_ON_PLAYER_LIST);
        showOnPlayerList = propShowOnPlayerList.getBoolean(SHOW_ON_PLAYER_LIST_DEFAULT);

        propscale = configuration.get(Names.Config.Category.GENERAL, Names.Config.SCALE_NAME, DEFAULT_SCALE, Names.Config.SCALE_DESCRIPTION, 1, 20);
        propscale.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SCALE_NAME);
        Scale = (float) propscale.getInt(DEFAULT_SCALE);

        propFileInterval = configuration.get(Names.Config.Category.GENERAL, Names.Config.FILE_INTERVAL, FILE_INTERVAL_DEFAULT, Names.Config.FILE_INTERVAL_DESC, 1, 60);
        propFileInterval.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.FILE_INTERVAL);
        fileInterval = propFileInterval.getInt(FILE_INTERVAL_DEFAULT);

        for (Alignment alignment : Alignment.values()) {
            Property property = configuration.get(Names.Config.Category.ALIGNMENT, alignment.toString().toLowerCase(), alignment.getDefaultXY(), String.format(Names.Config.ALIGNMENT_DESC, alignment));
            property.setLanguageKey(Names.Config.LANG_PREFIX + "." + alignment.toString().toLowerCase());
            property.setValidationPattern(Pattern.compile("-?\\d+ -?\\d+"));
            propAlignments.put(alignment, property);
            alignment.setXY(property.getString());
        }

        // spotless:on

        save();
    }

    public static void reload() {
        loadConfiguration();
        save();
    }

    public static void saveHUDsettingToFile() {
        propShowHUD = configuration.get(
                Names.Config.Category.GENERAL,
                Names.Config.SHOW_HUD,
                SHOW_HUD_DEFAULT,
                Names.Config.SHOW_HUD_DESC);
        propShowHUD.set(showHUD);
        save();
    }

    public static void save() {
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void setConfigName(String name) {
        propConfigName.set(name);
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(Reference.MODID)) {
            loadConfiguration();
        }
    }
}
