package com.wildfire.main.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class GenderConfig {
    private static final HashMap<UUID, String> playerGenders = new HashMap<>();
    private static Configuration config;

    // This method will be used to initialize the configuration and load the saved genders
    public static void loadGenderConfig(FMLPreInitializationEvent event) {
        File genderConfigFile = new File(event.getModConfigurationDirectory(), "genderConfig.cfg");
        config = new Configuration(genderConfigFile);

        try {
            config.load();
            // Load the saved gender values
            for (UUID playerId : playerGenders.keySet()) {
                String savedGender = config.getString(playerId.toString(), "PlayerGenders", "Male", "The gender of the player");
                playerGenders.put(playerId, savedGender);
            }
        } catch (Exception e) {
            System.err.println("Failed to load gender config.");
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    // This method will save the current gender data to a config file
    public static void saveGenderConfig() {
        try {
            // Save all player gender data
            for (UUID playerId : playerGenders.keySet()) {
                String gender = playerGenders.get(playerId);
                config.get("PlayerGenders", playerId.toString(), "Male").set(gender); // Save gender under player ID
            }
            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception e) {
            System.err.println("Failed to save gender config.");
            e.printStackTrace();
        }
    }

    // Set the gender for a player (called when gender changes)
    public static void setGender(EntityPlayer player, String gender) {
        if (gender != null && (gender.equals("Male") || gender.equals("Female") || gender.equals("Other"))) {
            playerGenders.put(player.getUniqueID(), gender);
            saveGenderConfig(); // Immediately save gender change
        } else {
            System.out.println("Invalid gender specified for player " + player.getName() + ". Defaulting to 'Male'.");
            playerGenders.put(player.getUniqueID(), "Male");
            saveGenderConfig();
        }
    }

    // Get the gender for a player (called to retrieve gender for rendering, etc.)
    public static String getGender(EntityPlayer player) {
        return playerGenders.getOrDefault(player.getUniqueID(), "Male");
    }
}
