package com.wildfire.config;

import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import java.util.UUID;

public class GenderConfig {
    private static final HashMap<UUID, String> playerGenders = new HashMap<>();

    public static void setGender(EntityPlayer player, String gender) {
        playerGenders.put(player.getUniqueID(), gender);
    }

    public static String getGender(EntityPlayer player) {
        return playerGenders.getOrDefault(player.getUniqueID(), "Male");
    }
}
