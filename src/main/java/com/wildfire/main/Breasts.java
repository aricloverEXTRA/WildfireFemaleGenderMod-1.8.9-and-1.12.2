package com.wildfire.main;

import net.minecraft.entity.player.EntityPlayer;
import com.wildfire.main.config.GenderConfig;

public class Breasts {
    private final EntityPlayer player;

    public Breasts(EntityPlayer player) {
        this.player = player;
    }

    public float getXOffset() {
        return GenderConfig.getPlayerSettings(player).breastsOffsetX;
    }

    public boolean updateXOffset(float value) {
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(player).breastsOffsetX = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getYOffset() {
        return GenderConfig.getPlayerSettings(player).breastsOffsetY;
    }

    public boolean updateYOffset(float value) {
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(player).breastsOffsetY = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getZOffset() {
        return GenderConfig.getPlayerSettings(player).breastsOffsetZ;
    }

    public boolean updateZOffset(float value) {
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(player).breastsOffsetZ = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getCleavage() {
        return GenderConfig.getPlayerSettings(player).breastsCleavage;
    }

    public boolean updateCleavage(float value) {
        if (validateCleavage(value)) {
            GenderConfig.getPlayerSettings(player).breastsCleavage = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public boolean isUniboob() {
        return GenderConfig.getPlayerSettings(player).breastsUniboob;
    }

    public boolean updateUniboob(boolean value) {
        GenderConfig.getPlayerSettings(player).breastsUniboob = value;
        GenderConfig.saveConfig();
        return true;
    }

    private boolean validateOffset(float value) {
        return value >= -10.0F && value <= 10.0F;
    }

    private boolean validateCleavage(float value) {
        return value >= 0.0F && value <= 10.0F;
    }
}