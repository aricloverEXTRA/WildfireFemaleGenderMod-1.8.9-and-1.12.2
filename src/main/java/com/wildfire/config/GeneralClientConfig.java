package com.wildfire.main.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class GeneralClientConfig {

    public static Configuration config;

    public static boolean disableRendering = false;
    public static boolean disableSoundReplacement = false;

    private static final String CLIENT_SETTINGS = "ClientSettings";
    private static final String DISABLE_RENDERING_KEY = "Disable Rendering";
    private static final String DISABLE_SOUND_REPLACEMENT_KEY = "Disable Sound Replacement";

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        try {
            config.load();

            disableRendering = config.getBoolean(DISABLE_RENDERING_KEY, CLIENT_SETTINGS, false, "Disable all rendering related to the mod (including in gender menus)");
            disableSoundReplacement = config.getBoolean(DISABLE_SOUND_REPLACEMENT_KEY, CLIENT_SETTINGS, false, "Disable replacing sounds of players with female variants");

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Error loading the configuration file: " + e.getMessage());
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static void saveConfig() {
        try {
            config.get(CLIENT_SETTINGS, DISABLE_RENDERING_KEY, false).set(disableRendering);
            config.get(CLIENT_SETTINGS, DISABLE_SOUND_REPLACEMENT_KEY, false).set(disableSoundReplacement);

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Error saving the configuration file: " + e.getMessage());
        }
    }
}
