package com.wildfire.main.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class GenderConfig {
    private static Configuration config;
    private static final HashMap<UUID, PlayerGenderSettings> playerSettings = new HashMap<>();

    public static class PlayerGenderSettings {
        public String gender = "Male";
        public boolean breastsEnabled = false;
        public float breastSize = 50.0F;
        public float bounceMultiplier = 1.0F;
        public float separation = 0.0F;
        public float depth = -5.0F;
        public float height = 0.0F;
        public float rotation = 5.0F;
        public float breastsOffsetX = 0.5F;
        public float breastsOffsetY = -1.5F;
        public float breastsOffsetZ = -2.0F;
        public float breastsCleavage = 1.0F;
        public boolean breastsUniboob = false;
        public float stiffness = 0.1F;
        public float damping = 0.85F;
        public boolean showFirstTimeGui = true;
    }

    public static void loadConfig(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "genderConfig.cfg");
        config = new Configuration(configFile);

        try {
            config.load();
            PlayerGenderSettings defaults = new PlayerGenderSettings();
            defaults.gender = config.getString("Gender", "General", "Male", "Default gender (Male, Female, Other)");
            defaults.breastsEnabled = config.getBoolean("BreastsEnabled", "General", false, "Enable or disable breasts feature");
            defaults.breastSize = config.getFloat("BreastSize", "BreastSettings", 50.0F, 0.0F, 100.0F, "Breast size");
            defaults.bounceMultiplier = config.getFloat("BounceMultiplier", "BreastSettings", 1.0F, 0.0F, 3.0F, "Bounce multiplier for breast animations");
            defaults.separation = config.getFloat("Separation", "BreastSettings", 0.0F, -10.0F, 10.0F, "Separation between breasts");
            defaults.depth = config.getFloat("Depth", "BreastSettings", -5.0F, -10.0F, 0.0F, "Depth of breasts");
            defaults.height = config.getFloat("Height", "BreastSettings", 0.0F, -10.0F, 10.0F, "Height of breasts");
            defaults.rotation = config.getFloat("Rotation", "BreastSettings", 5.0F, 0.0F, 10.0F, "Rotation angle for breasts");
            defaults.breastsOffsetX = config.getFloat("BreastsOffsetX", "BreastSettings", 0.5F, -10.0F, 10.0F, "X offset for breasts");
            defaults.breastsOffsetY = config.getFloat("BreastsOffsetY", "BreastSettings", -1.5F, -10.0F, 10.0F, "Y offset for breasts");
            defaults.breastsOffsetZ = config.getFloat("BreastsOffsetZ", "BreastSettings", -2.0F, -10.0F, 10.0F, "Z offset for breasts");
            defaults.breastsCleavage = config.getFloat("BreastsCleavage", "BreastSettings", 1.0F, 0.0F, 10.0F, "Cleavage value for breasts");
            defaults.breastsUniboob = config.getBoolean("BreastsUniboob", "BreastSettings", false, "Enable or disable uniboob");
            defaults.stiffness = config.getFloat("Stiffness", "BreastSettings", 0.1F, 0.05F, 0.5F, "Physics stiffness for breast movement");
            defaults.damping = config.getFloat("Damping", "BreastSettings", 0.85F, 0.5F, 0.95F, "Physics damping for breast movement");

            for (String playerId : config.getCategory("PlayerSettings").getValues().keySet()) {
                try {
                    UUID uuid = UUID.fromString(playerId);
                    PlayerGenderSettings settings = new PlayerGenderSettings();
                    settings.gender = config.getString(playerId + ".Gender", "PlayerSettings", defaults.gender, "Player gender");
                    settings.breastsEnabled = config.getBoolean(playerId + ".BreastsEnabled", "PlayerSettings", defaults.breastsEnabled, "Player breasts enabled");
                    settings.breastSize = config.getFloat(playerId + ".BreastSize", "PlayerSettings", defaults.breastSize, 0.0F, 100.0F, "Player breast size");
                    settings.bounceMultiplier = config.getFloat(playerId + ".BounceMultiplier", "PlayerSettings", defaults.bounceMultiplier, 0.0F, 3.0F, "Player bounce multiplier");
                    settings.separation = config.getFloat(playerId + ".Separation", "PlayerSettings", defaults.separation, -10.0F, 10.0F, "Player separation");
                    settings.depth = config.getFloat(playerId + ".Depth", "PlayerSettings", defaults.depth, -10.0F, 0.0F, "Player depth");
                    settings.height = config.getFloat(playerId + ".Height", "PlayerSettings", defaults.height, -10.0F, 10.0F, "Player height");
                    settings.rotation = config.getFloat(playerId + ".Rotation", "PlayerSettings", defaults.rotation, 0.0F, 10.0F, "Player rotation");
                    settings.breastsOffsetX = config.getFloat(playerId + ".BreastsOffsetX", "PlayerSettings", defaults.breastsOffsetX, -10.0F, 10.0F, "Player X offset");
                    settings.breastsOffsetY = config.getFloat(playerId + ".BreastsOffsetY", "PlayerSettings", defaults.breastsOffsetY, -10.0F, 10.0F, "Player Y offset");
                    settings.breastsOffsetZ = config.getFloat(playerId + ".BreastsOffsetZ", "PlayerSettings", defaults.breastsOffsetZ, -10.0F, 10.0F, "Player Z offset");
                    settings.breastsCleavage = config.getFloat(playerId + ".BreastsCleavage", "PlayerSettings", defaults.breastsCleavage, 0.0F, 10.0F, "Player cleavage");
                    settings.breastsUniboob = config.getBoolean(playerId + ".BreastsUniboob", "PlayerSettings", defaults.breastsUniboob, "Player uniboob");
                    settings.stiffness = config.getFloat(playerId + ".Stiffness", "PlayerSettings", defaults.stiffness, 0.05F, 0.5F, "Player stiffness");
                    settings.damping = config.getFloat(playerId + ".Damping", "PlayerSettings", defaults.damping, 0.5F, 0.95F, "Player damping");
                    playerSettings.put(uuid, settings);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid UUID in config: " + playerId);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load gender config: " + e.getMessage());
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static void saveConfig() {
        try {
            for (UUID playerId : playerSettings.keySet()) {
                PlayerGenderSettings settings = playerSettings.get(playerId);
                config.get("PlayerSettings", playerId + ".Gender", "Male").set(settings.gender);
                config.get("PlayerSettings", playerId + ".BreastsEnabled", false).set(settings.breastsEnabled);
                config.get("PlayerSettings", playerId + ".BreastSize", 50.0F).set(settings.breastSize);
                config.get("PlayerSettings", playerId + ".BounceMultiplier", 1.0F).set(settings.bounceMultiplier);
                config.get("PlayerSettings", playerId + ".Separation", 0.0F).set(settings.separation);
                config.get("PlayerSettings", playerId + ".Depth", -5.0F).set(settings.depth);
                config.get("PlayerSettings", playerId + ".Height", 0.0F).set(settings.height);
                config.get("PlayerSettings", playerId + ".Rotation", 5.0F).set(settings.rotation);
                config.get("PlayerSettings", playerId + ".BreastsOffsetX", 0.5F).set(settings.breastsOffsetX);
                config.get("PlayerSettings", playerId + ".BreastsOffsetY", -1.5F).set(settings.breastsOffsetY);
                config.get("PlayerSettings", playerId + ".BreastsOffsetZ", -2.0F).set(settings.breastsOffsetZ);
                config.get("PlayerSettings", playerId + ".BreastsCleavage", 1.0F).set(settings.breastsCleavage);
                config.get("PlayerSettings", playerId + ".BreastsUniboob", false).set(settings.breastsUniboob);
                config.get("PlayerSettings", playerId + ".Stiffness", 0.1F).set(settings.stiffness);
                config.get("PlayerSettings", playerId + ".Damping", 0.85F).set(settings.damping);
            }
            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Failed to save gender config: " + e.getMessage());
        }
    }

    public static PlayerGenderSettings getPlayerSettings(EntityPlayer player) {
        return playerSettings.computeIfAbsent(player.getUniqueID(), k -> new PlayerGenderSettings());
    }

    public static void setGender(EntityPlayer player, String gender) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (gender != null && (gender.equals("Male") || gender.equals("Female") || gender.equals("Other"))) {
            settings.gender = gender;
            saveConfig();
        } else {
            settings.gender = "Male";
            saveConfig();
        }
    }

    public static String getGender(EntityPlayer player) {
        return getPlayerSettings(player).gender;
    }
}