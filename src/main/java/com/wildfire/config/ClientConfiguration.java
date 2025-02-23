package com.wildfire.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientConfiguration {

    public static Configuration config;

    public static float bustSize = 0.6F;
    public static String gender = "Male";
    public static boolean breastPhysics = true;

    private static final String CLIENT_SETTINGS = "ClientSettings";
    private static final String BUST_SIZE_KEY = "Bust Size";
    private static final String GENDER_KEY = "Gender";
    private static final String BREAST_PHYSICS_KEY = "Breast Physics";

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        try {
            config.load();

            bustSize = config.getFloat(BUST_SIZE_KEY, CLIENT_SETTINGS, 0.6F, 0.0F, 1.0F, "Bust size");
            gender = config.getString(GENDER_KEY, CLIENT_SETTINGS, "Male", "Selected gender (Male, Female, Other)");
            breastPhysics = config.getBoolean(BREAST_PHYSICS_KEY, CLIENT_SETTINGS, true, "Enable or disable breast physics");

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
            config.get(CLIENT_SETTINGS, BUST_SIZE_KEY, 0.6).set(bustSize);
            config.get(CLIENT_SETTINGS, GENDER_KEY, "Male").set(gender);
            config.get(CLIENT_SETTINGS, BREAST_PHYSICS_KEY, true).set(breastPhysics);

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Error saving the configuration file: " + e.getMessage());
        }
    }
}
