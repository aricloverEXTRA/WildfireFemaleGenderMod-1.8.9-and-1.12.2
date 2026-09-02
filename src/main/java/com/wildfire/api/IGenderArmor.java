package com.wildfire.api;

public interface IGenderArmor {

    default boolean coversBreasts() {
        return true;
    }

    default boolean alwaysHidesBreasts() {
        return false;
    }

    default float physicsResistance() {
        return 0;
    }

    default float tightness() {
        return 0;
    }
}
