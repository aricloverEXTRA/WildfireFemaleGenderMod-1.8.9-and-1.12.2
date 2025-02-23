package com.wildfire.renderer.armor;

import com.wildfire.api.IGenderArmor;

/**
 * An empty gender armor implementation that always returns default "no armor" behavior.
 */
public class EmptyGenderArmor implements IGenderArmor {

    // Singleton instance for Empty Armor
    public static final EmptyGenderArmor INSTANCE = new EmptyGenderArmor();

    // Private constructor to prevent instantiation outside of the singleton instance
    private EmptyGenderArmor() {
    }

    @Override
    public boolean coversBreasts() {
        return false; // No coverage, since this represents "no armor"
    }

    @Override
    public boolean alwaysHidesBreasts() {
        return false; // No special handling for hiding breasts
    }

    @Override
    public float physicsResistance() {
        return 0; // No resistance
    }

    @Override
    public float tightness() {
        return 0; // No tightness (since this represents "no armor")
    }
}
