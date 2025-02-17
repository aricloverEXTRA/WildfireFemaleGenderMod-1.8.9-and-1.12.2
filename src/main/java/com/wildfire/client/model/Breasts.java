package com.wildfire.client.model;

import com.wildfire.config.ConfigSettings;

public class Breasts {
    private float xOffset;
    private float yOffset;
    private float zOffset;
    private float cleavage;
    private boolean uniboob;

    public Breasts() {
        this.xOffset = ConfigSettings.breastsOffsetX;
        this.yOffset = ConfigSettings.breastsOffsetY;
        this.zOffset = ConfigSettings.breastsOffsetZ;
        this.cleavage = ConfigSettings.breastsCleavage;
        this.uniboob = ConfigSettings.breastsUniboob;
    }

    public float getXOffset() {
        return xOffset;
    }

    public boolean updateXOffset(float value) {
        if (validateOffset(value)) {
            this.xOffset = value;
            ConfigSettings.breastsOffsetX = value;
            return true;
        }
        return false;
    }

    public float getYOffset() {
        return yOffset;
    }

    public boolean updateYOffset(float value) {
        if (validateOffset(value)) {
            this.yOffset = value;
            ConfigSettings.breastsOffsetY = value;
            return true;
        }
        return false;
    }

    public float getZOffset() {
        return zOffset;
    }

    public boolean updateZOffset(float value) {
        if (validateOffset(value)) {
            this.zOffset = value;
            ConfigSettings.breastsOffsetZ = value;
            return true;
        }
        return false;
    }

    public float getCleavage() {
        return cleavage;
    }

    public boolean updateCleavage(float value) {
        if (validateCleavage(value)) {
            this.cleavage = value;
            ConfigSettings.breastsCleavage = value;
            return true;
        }
        return false;
    }

    public boolean isUniboob() {
        return uniboob;
    }

    public boolean updateUniboob(boolean value) {
        this.uniboob = value;
        ConfigSettings.breastsUniboob = value;
        return true;
    }

    private boolean validateOffset(float value) {
        return value >= -10.0F && value <= 10.0F;
    }

    private boolean validateCleavage(float value) {
        return value >= 0.0F && value <= 10.0F;
    }
}
