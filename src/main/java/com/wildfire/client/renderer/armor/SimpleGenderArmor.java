package com.wildfire.renderer.armor;

import com.wildfire.api.IGenderArmor;

/**
 * Base class to help define default implementations of {@link IGenderArmor}.
 */
public class SimpleGenderArmor implements IGenderArmor {

    private final float physicsResistance;
    private final float tightness;

    // Constants for different armor types
    public static final SimpleGenderArmor FALLBACK = new SimpleGenderArmor(0.5F);
    public static final SimpleGenderArmor LEATHER = new SimpleGenderArmor(0.3F, 0.5F);
    public static final SimpleGenderArmor CHAIN_MAIL = new SimpleGenderArmor(0.5F, 0.2F);
    public static final SimpleGenderArmor GOLD = new SimpleGenderArmor(0.85F);
    public static final SimpleGenderArmor IRON = new SimpleGenderArmor(1);
    public static final SimpleGenderArmor DIAMOND = new SimpleGenderArmor(1);
    public static final SimpleGenderArmor NETHERITE = new SimpleGenderArmor(1);

    // Constructor with only physics resistance
    public SimpleGenderArmor(float physicsResistance) {
        this(physicsResistance, 0);
    }

    // Constructor with both physics resistance and tightness
    public SimpleGenderArmor(float physicsResistance, float tightness) {
        this.physicsResistance = physicsResistance;
        this.tightness = tightness;
    }

    // Getter for physics resistance
    @Override
    public float physicsResistance() {
        return this.physicsResistance;
    }

    // Getter for tightness
    @Override
    public float tightness() {
        return this.tightness;
    }

    // Default implementation for other methods
    @Override
    public boolean coversBreasts() {
        return true; // Default is to cover breasts
    }

    @Override
    public boolean alwaysHidesBreasts() {
        return false; // No special behavior for hiding breasts
    }
}
