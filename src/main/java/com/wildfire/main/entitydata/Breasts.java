package com.wildfire.main.entitydata;

import net.minecraft.entity.player.EntityPlayer;
import com.wildfire.main.config.GenderConfig;

public class Breasts {
    private final EntityPlayer player;

    public Breasts() {
        this.player = null;
    }

    public Breasts(EntityPlayer player) {
        this.player = player;
    }

    private EntityPlayer requirePlayer() {
        if (player == null) throw new IllegalStateException("Breasts instance has no associated player");
        return player;
    }

    public float getXOffset() {
        EntityPlayer p = requirePlayer();
        return GenderConfig.getPlayerSettings(p).breastsOffsetX;
    }

    public boolean updateXOffset(float value) {
        EntityPlayer p = requirePlayer();
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(p).breastsOffsetX = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getYOffset() {
        EntityPlayer p = requirePlayer();
        return GenderConfig.getPlayerSettings(p).breastsOffsetY;
    }

    public boolean updateYOffset(float value) {
        EntityPlayer p = requirePlayer();
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(p).breastsOffsetY = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getZOffset() {
        EntityPlayer p = requirePlayer();
        return GenderConfig.getPlayerSettings(p).breastsOffsetZ;
    }

    public boolean updateZOffset(float value) {
        EntityPlayer p = requirePlayer();
        if (validateOffset(value)) {
            GenderConfig.getPlayerSettings(p).breastsOffsetZ = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public float getCleavage() {
        EntityPlayer p = requirePlayer();
        return GenderConfig.getPlayerSettings(p).breastsCleavage;
    }

    public boolean updateCleavage(float value) {
        EntityPlayer p = requirePlayer();
        if (validateCleavage(value)) {
            GenderConfig.getPlayerSettings(p).breastsCleavage = value;
            GenderConfig.saveConfig();
            return true;
        }
        return false;
    }

    public boolean isUniboob() {
        EntityPlayer p = requirePlayer();
        return GenderConfig.getPlayerSettings(p).breastsUniboob;
    }

    public boolean updateUniboob(boolean value) {
        EntityPlayer p = requirePlayer();
        GenderConfig.getPlayerSettings(p).breastsUniboob = value;
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
