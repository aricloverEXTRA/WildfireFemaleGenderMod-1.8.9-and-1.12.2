package com.wildfire.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

public class BreastPresetConfiguration {

    public static Configuration config;

    public static List<String> presets = new ArrayList<>();

    private static final String PRESETS_SETTINGS = "Presets";
    private static final String PRESET_KEY_PREFIX = "Preset_";

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        try {
            config.load();

            int presetCount = config.getInt("Preset Count", PRESETS_SETTINGS, 0, 0, Integer.MAX_VALUE, "Number of preset configurations");

            for (int i = 0; i < presetCount; i++) {
                String preset = config.getString(PRESET_KEY_PREFIX + i, PRESETS_SETTINGS, "DefaultPreset" + (i + 1), "Preset name");
                presets.add(preset);
            }

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
            config.get(PRESETS_SETTINGS, "Preset Count", 0).set(presets.size());

            for (int i = 0; i < presets.size(); i++) {
                config.get(PRESETS_SETTINGS, PRESET_KEY_PREFIX + i, "DefaultPreset" + (i + 1)).set(presets.get(i));
            }

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Error saving the configuration file: " + e.getMessage());
        }
    }
}
