package com.wildfire.main.config;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class GenderConfig {
    private static Configuration config;
    private static PlayerGenderSettings localPlayerSettings;

    public static class PlayerGenderSettings {
        public String gender = "Male";
        public boolean breastsEnabled = false;
        public float breastSize = 50.0F;
        public float bounceMultiplier = 1.0F;
        public float separation = 0.0F;
        public float depth = 0.0F;
        public float height = 0.0F;
        public float rotation = 0.0F;
        public float breastsOffsetX = 0.5F;
        public float breastsOffsetY = -1.5F;
        public float breastsOffsetZ = -2.0F;
        public float breastsCleavage = 1.0F;
        public boolean breastsUniboob = false;
        public float stiffness = 0.1F;
        public float damping = 0.85F;
        public boolean showFirstTimeGui = true;
        public boolean hurtSoundsEnabled = false;
        public boolean physicsEnabled = true;
        public float intensity = 100.0F;
        public float momentum = 50.0F;
        public float voicePitch = 100.0F;
        public boolean darkMode = false;

        public boolean overrideArmorPhysics = false;
        public boolean showArmorTooltip = true;
        public boolean hideInArmor = false;
        public boolean holidayThemes = true;
    }

    public static void loadConfig(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "genderConfig.cfg");
        config = new Configuration(configFile);

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config.load();

            localPlayerSettings = new PlayerGenderSettings();
            localPlayerSettings.gender = config.getString("Gender", "General", "Male", "Default gender (Male, Female, Other)");
            localPlayerSettings.breastsEnabled = config.getBoolean("BreastsEnabled", "General", false, "Enable or disable breasts feature");
            localPlayerSettings.breastSize = config.getFloat("BreastSize", "General", 50.0F, 0.0F, 100.0F, "Breast size (0-100%)");
            localPlayerSettings.bounceMultiplier = config.getFloat("BounceMultiplier", "General", 1.0F, 0.0F, 3.0F, "Default bounce multiplier");
            localPlayerSettings.separation = config.getFloat("Separation", "General", 0.0F, -10.0F, 10.0F, "Separation (-10 to 10)");
            localPlayerSettings.depth = config.getFloat("Depth", "General", 0.0F, -10.0F, 0.0F, "Depth (0 to -10, inward push)");
            localPlayerSettings.height = config.getFloat("Height", "General", 0.0F, -10.0F, 10.0F, "Height (-10 to 10)");
            localPlayerSettings.rotation = config.getFloat("Rotation", "General", 0.0F, 0.0F, 10.0F, "Rotation (0-10°)");
            localPlayerSettings.breastsOffsetX = config.getFloat("BreastsOffsetX", "General", 0.5F, -10.0F, 10.0F, "Default X offset");
            localPlayerSettings.breastsOffsetY = config.getFloat("BreastsOffsetY", "General", -1.5F, -10.0F, 10.0F, "Default Y offset");
            localPlayerSettings.breastsOffsetZ = config.getFloat("BreastsOffsetZ", "General", -2.0F, -10.0F, 10.0F, "Default Z offset");
            localPlayerSettings.breastsCleavage = config.getFloat("BreastsCleavage", "General", 1.0F, 0.0F, 10.0F, "Default cleavage");
            localPlayerSettings.breastsUniboob = config.getBoolean("BreastsUniboob", "General", false, "Default uniboob");
            localPlayerSettings.stiffness = config.getFloat("Stiffness", "General", 0.1F, 0.05F, 0.5F, "Default stiffness");
            localPlayerSettings.damping = config.getFloat("Damping", "General", 0.85F, 0.5F, 0.95F, "Default damping");
            localPlayerSettings.showFirstTimeGui = config.getBoolean("ShowFirstTimeGui", "General", true, "Show first-time GUI");
            localPlayerSettings.hurtSoundsEnabled = config.getBoolean("HurtSoundsEnabled", "General", false, "Enable female hurt sounds");
            localPlayerSettings.physicsEnabled = config.getBoolean("PhysicsEnabled", "General", true, "Enable breast physics");
            localPlayerSettings.intensity = config.getFloat("Intensity", "General", 100.0F, 0.0F, 150.0F, "Physics intensity (0-150%)");
            localPlayerSettings.momentum = config.getFloat("Momentum", "General", 50.0F, 25.0F, 100.0F, "Physics momentum (25-100%)");
            localPlayerSettings.voicePitch = config.getFloat("VoicePitch", "General", 100.0F, 80.0F, 120.0F, "Voice pitch (80-120%)");
            localPlayerSettings.darkMode = config.getBoolean("DarkMode", "General", false, "Enable dark mode theme");

            localPlayerSettings.overrideArmorPhysics = config.getBoolean("OverrideArmorPhysics", "General", false, "Override armor interaction with breast physics");
            localPlayerSettings.showArmorTooltip = config.getBoolean("ShowArmorTooltip", "General", true, "Show armor stats tooltip for breast armor");
            localPlayerSettings.hideInArmor = config.getBoolean("HideInArmor", "General", false, "Hide breasts visually while wearing armor");
            localPlayerSettings.holidayThemes = config.getBoolean("HolidayThemes", "General", true, "Enable holiday-themed cosmetics in GUI previews");
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
            if (localPlayerSettings == null) {
                localPlayerSettings = new PlayerGenderSettings();
            }
            config.get("General", "Gender", "Male").set(localPlayerSettings.gender);
            config.get("General", "BreastsEnabled", false).set(localPlayerSettings.breastsEnabled);
            config.get("General", "BreastSize", 50.0F).set(localPlayerSettings.breastSize);
            config.get("General", "BounceMultiplier", 1.0F).set(localPlayerSettings.bounceMultiplier);
            config.get("General", "Separation", 0.0F).set(localPlayerSettings.separation);
            config.get("General", "Depth", 0.0F).set(localPlayerSettings.depth);
            config.get("General", "Height", 0.0F).set(localPlayerSettings.height);
            config.get("General", "Rotation", 0.0F).set(localPlayerSettings.rotation);
            config.get("General", "BreastsOffsetX", 0.5F).set(localPlayerSettings.breastsOffsetX);
            config.get("General", "BreastsOffsetY", -1.5F).set(localPlayerSettings.breastsOffsetY);
            config.get("General", "BreastsOffsetZ", -2.0F).set(localPlayerSettings.breastsOffsetZ);
            config.get("General", "BreastsCleavage", 1.0F).set(localPlayerSettings.breastsCleavage);
            config.get("General", "BreastsUniboob", false).set(localPlayerSettings.breastsUniboob);
            config.get("General", "Stiffness", 0.1F).set(localPlayerSettings.stiffness);
            config.get("General", "Damping", 0.85F).set(localPlayerSettings.damping);
            config.get("General", "ShowFirstTimeGui", true).set(localPlayerSettings.showFirstTimeGui);
            config.get("General", "HurtSoundsEnabled", false).set(localPlayerSettings.hurtSoundsEnabled);
            config.get("General", "PhysicsEnabled", true).set(localPlayerSettings.physicsEnabled);
            config.get("General", "Intensity", 100.0F).set(localPlayerSettings.intensity);
            config.get("General", "Momentum", 50.0F).set(localPlayerSettings.momentum);
            config.get("General", "VoicePitch", 100.0F).set(localPlayerSettings.voicePitch);
            config.get("General", "DarkMode", false).set(localPlayerSettings.darkMode);

            config.get("General", "OverrideArmorPhysics", false).set(localPlayerSettings.overrideArmorPhysics);
            config.get("General", "ShowArmorTooltip", true).set(localPlayerSettings.showArmorTooltip);
            config.get("General", "HideInArmor", false).set(localPlayerSettings.hideInArmor);
            config.get("General", "HolidayThemes", true).set(localPlayerSettings.holidayThemes);

            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Failed to save gender config: " + e.getMessage());
        }
    }

    public static PlayerGenderSettings getPlayerSettings(EntityPlayer player) {
        if (player != Minecraft.getMinecraft().player) {
            return null;
        }
        if (localPlayerSettings == null) {
            localPlayerSettings = new PlayerGenderSettings();
            localPlayerSettings.showFirstTimeGui = true;
        }
        return localPlayerSettings;
    }

    public static void setOverrideArmorPhysics(EntityPlayer player, boolean enabled) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (settings != null) {
            settings.overrideArmorPhysics = enabled;
            saveConfig();
        }
    }

    public static boolean getOverrideArmorPhysics(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.overrideArmorPhysics : false;
    }

    public static void setShowArmorTooltip(EntityPlayer player, boolean enabled) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (settings != null) {
            settings.showArmorTooltip = enabled;
            saveConfig();
        }
    }

    public static boolean getShowArmorTooltip(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.showArmorTooltip : true;
    }

    public static void setHideInArmor(EntityPlayer player, boolean enabled) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (settings != null) {
            settings.hideInArmor = enabled;
            saveConfig();
        }
    }

    public static boolean getHideInArmor(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.hideInArmor : false;
    }

    public static void setHolidayThemes(EntityPlayer player, boolean enabled) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (settings != null) {
            settings.holidayThemes = enabled;
            saveConfig();
        }
    }

    public static boolean getHolidayThemes(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.holidayThemes : true;
    }

    public static void setGender(EntityPlayer player, String gender) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (gender != null && (gender.equals("Male") || gender.equals("Female") || gender.equals("Other"))) {
            settings.gender = gender;
            saveConfig();
        }
    }

    public static String getGender(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.gender : "Male";
    }

    public static void setDarkMode(EntityPlayer player, boolean darkMode) {
        if (player != Minecraft.getMinecraft().player) return;
        PlayerGenderSettings settings = getPlayerSettings(player);
        if (settings != null) {
            settings.darkMode = darkMode;
            saveConfig();
        }
    }

    public static boolean getDarkMode(EntityPlayer player) {
        PlayerGenderSettings settings = getPlayerSettings(player);
        return settings != null ? settings.darkMode : false;
    }
}