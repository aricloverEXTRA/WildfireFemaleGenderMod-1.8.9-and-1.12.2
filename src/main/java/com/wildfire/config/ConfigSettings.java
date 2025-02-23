package com.wildfire.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigSettings {
    public static Configuration config;

    public static float breastSize = 50.0F;
    public static float bounceMultiplier = 1.0F;
    public static float separation = 0.0F;
    public static float depth = -5.0F;
    public static float height = 0.0F;
    public static float rotation = 5.0F;
    public static float breastsOffsetX = 0.5F;
    public static float breastsOffsetY = -1.5F;
    public static float breastsOffsetZ = -2.0F;
    public static float breastsCleavage = 1.0F;
    public static boolean breastsUniboob = false;

    public static String gender = "Male";
    public static boolean breastsEnabled = false;

    private static final String BREAST_SETTINGS = "BreastSettings";
    private static final String GENERAL_SETTINGS = "General";
    private static final String BREAST_SIZE_KEY = "Breast Size";
    private static final String BOUNCE_MULTIPLIER_KEY = "Bounce Multiplier";
    private static final String SEPARATION_KEY = "Separation";
    private static final String DEPTH_KEY = "Depth";
    private static final String HEIGHT_KEY = "Height";
    private static final String ROTATION_KEY = "Rotation";
    private static final String BREASTS_OFFSET_X_KEY = "Breast Offset X";
    private static final String BREASTS_OFFSET_Y_KEY = "Breast Offset Y";
    private static final String BREASTS_OFFSET_Z_KEY = "Breast Offset Z";
    private static final String BREASTS_CLEAVAGE_KEY = "Cleavage";
    private static final String BREASTS_UNIBOOB_KEY = "Uniboob";
    private static final String GENDER_KEY = "Gender";
    private static final String BREASTS_ENABLED_KEY = "Breasts Enabled";

    public static void loadConfig(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        try {
            config.load();

            breastSize = config.getFloat(BREAST_SIZE_KEY, BREAST_SETTINGS, 50.0F, 0.0F, 100.0F, "Breast size");
            bounceMultiplier = config.getFloat(BOUNCE_MULTIPLIER_KEY, BREAST_SETTINGS, 1.0F, 0.0F, 3.0F, "Bounce multiplier for breast animations");
            separation = config.getFloat(SEPARATION_KEY, BREAST_SETTINGS, 0.0F, -10.0F, 10.0F, "Separation between breasts");
            depth = config.getFloat(DEPTH_KEY, BREAST_SETTINGS, -5.0F, -10.0F, 0.0F, "Depth of breasts");
            height = config.getFloat(HEIGHT_KEY, BREAST_SETTINGS, 0.0F, -10.0F, 10.0F, "Height of breasts");
            rotation = config.getFloat(ROTATION_KEY, BREAST_SETTINGS, 5.0F, 0.0F, 10.0F, "Rotation angle for breasts");

            breastsOffsetX = config.getFloat(BREASTS_OFFSET_X_KEY, BREAST_SETTINGS, 0.5F, -10.0F, 10.0F, "X offset for breasts");
            breastsOffsetY = config.getFloat(BREASTS_OFFSET_Y_KEY, BREAST_SETTINGS, -1.5F, -10.0F, 10.0F, "Y offset for breasts");
            breastsOffsetZ = config.getFloat(BREASTS_OFFSET_Z_KEY, BREAST_SETTINGS, -2.0F, -10.0F, 10.0F, "Z offset for breasts");

            breastsCleavage = config.getFloat(BREASTS_CLEAVAGE_KEY, BREAST_SETTINGS, 1.0F, 0.0F, 10.0F, "Cleavage value for breasts");
            breastsUniboob = config.getBoolean(BREASTS_UNIBOOB_KEY, BREAST_SETTINGS, false, "Enable or disable uniboob");

            gender = config.getString(GENDER_KEY, GENERAL_SETTINGS, "Male", "Selected gender (Male, Female, Other)");
            breastsEnabled = config.getBoolean(BREASTS_ENABLED_KEY, GENERAL_SETTINGS, false, "Enable or disable breasts feature");

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
            config.get(BREAST_SETTINGS, BREAST_SIZE_KEY, 50.0).set(breastSize);
            config.get(BREAST_SETTINGS, BOUNCE_MULTIPLIER_KEY, 1.0).set(bounceMultiplier);
            config.get(BREAST_SETTINGS, SEPARATION_KEY, 0.0).set(separation);
            config.get(BREAST_SETTINGS, DEPTH_KEY, -5.0).set(depth);
            config.get(BREAST_SETTINGS, HEIGHT_KEY, 0.0).set(height);
            config.get(BREAST_SETTINGS, ROTATION_KEY, 5.0).set(rotation);

            config.get(BREAST_SETTINGS, BREASTS_OFFSET_X_KEY, 0.5).set(breastsOffsetX);
            config.get(BREAST_SETTINGS, BREASTS_OFFSET_Y_KEY, -1.5).set(breastsOffsetY);
            config.get(BREAST_SETTINGS, BREASTS_OFFSET_Z_KEY, -2.0).set(breastsOffsetZ);

            config.get(BREAST_SETTINGS, BREASTS_CLEAVAGE_KEY, 1.0).set(breastsCleavage);
            config.get(BREAST_SETTINGS, BREASTS_UNIBOOB_KEY, false).set(breastsUniboob);

            config.get(GENERAL_SETTINGS, GENDER_KEY, "Male").set(gender);
            config.get(GENERAL_SETTINGS, BREASTS_ENABLED_KEY, false).set(breastsEnabled);

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Error saving the configuration file: " + e.getMessage());
        }
    }
}