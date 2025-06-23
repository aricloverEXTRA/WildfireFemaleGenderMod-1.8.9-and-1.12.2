package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

public class EmptyGenderArmor implements IGenderArmor {
    public static final EmptyGenderArmor INSTANCE = new EmptyGenderArmor();

    private EmptyGenderArmor() {}

    @Override
    public float physicsResistance() {
        return 0f;
    }

    @Override
    public float tightness() {
        return 0f;
    }

    @Override
    public boolean coversBreasts() {
        return false;
    }

    @Override
    public boolean alwaysHidesBreasts() {
        return false;
    }
}