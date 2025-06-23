package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

public class SimpleGenderArmor implements IGenderArmor {
    public static final SimpleGenderArmor FALLBACK = new SimpleGenderArmor(0.5f);
    public static final SimpleGenderArmor LEATHER = new SimpleGenderArmor(0.3f, 0.5f);
    public static final SimpleGenderArmor CHAINMAIL = new SimpleGenderArmor(0.5f, 0.2f);
    public static final SimpleGenderArmor GOLD = new SimpleGenderArmor(0.85f);
    public static final SimpleGenderArmor IRON = new SimpleGenderArmor(1.0f);
    public static final SimpleGenderArmor DIAMOND = new SimpleGenderArmor(1.0f);

    private final float physicsResistance;
    private final float tightness;

    public SimpleGenderArmor(float physicsResistance) {
        this(physicsResistance, 0f);
    }

    public SimpleGenderArmor(float physicsResistance, float tightness) {
        this.physicsResistance = physicsResistance;
        this.tightness = tightness;
    }

    @Override
    public float physicsResistance() {
        return physicsResistance;
    }

    @Override
    public float tightness() {
        return tightness;
    }

    @Override
    public boolean coversBreasts() {
        return true;
    }

    @Override
    public boolean alwaysHidesBreasts() {
        return false;
    }
}