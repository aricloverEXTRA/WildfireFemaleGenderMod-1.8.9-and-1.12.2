package com.wildfire.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum ButtocksSide {
    LEFT(true), RIGHT(false);

    public final boolean isLeft;

    ButtocksSide(boolean isLeft) {
        this.isLeft = isLeft;
    }
}
