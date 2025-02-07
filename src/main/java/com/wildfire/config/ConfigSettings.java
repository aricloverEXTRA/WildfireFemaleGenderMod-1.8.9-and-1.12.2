package com.wildfire.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigSettings {
    public static Configuration config;

    public static double breastSize = 50.0;
    public static double separation = 0.0;
    public static double depth = -5.0;
    public static double height = 0.0;
    public static double rotation = 5.0;

    public static String gender = "Male"; // Default gender
    public static boolean breastsEnabled = false; // Default state

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        breastSize = config.getFloat("Breast Size", "BreastSettings", 50.0F, 0.0F, 100.0F, "");
        separation = config.getFloat("Separation", "BreastSettings", 0.0F, -10.0F, 10.0F, "");
        depth = config.getFloat("Depth", "BreastSettings", -5.0F, -10.0F, 0.0F, "");
        height = config.getFloat("Height", "BreastSettings", 0.0F, -10.0F, 10.0F, "");
        rotation = config.getFloat("Rotation", "BreastSettings", 5.0F, 0.0F, 10.0F, "");

        gender = config.getString("Gender", "General", "Male", "Selected gender (Male, Female, Other)");
        breastsEnabled = config.getBoolean("Breasts Enabled", "General", false, "Are breasts enabled?");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void saveConfig() {
        config.get("BreastSettings", "Breast Size", 50.0).set(breastSize);
        config.get("BreastSettings", "Separation", 0.0).set(separation);
        config.get("BreastSettings", "Depth", -5.0).set(depth);
        config.get("BreastSettings", "Height", 0.0).set(height);
        config.get("BreastSettings", "Rotation", 5.0).set(rotation);

        config.get("General", "Gender", "Male").set(gender);
        config.get("General", "Breasts Enabled", false).set(breastsEnabled);

        if (config.hasChanged()) {
            config.save();
        }
    }
}
