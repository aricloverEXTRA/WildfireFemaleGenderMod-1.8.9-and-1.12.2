package com.wildfire.main;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class WildfireHelper {
    // Defensive accessor for armor slots. Valid slots are 0..3 in vanilla inventory armor array.
    public static ItemStack getArmor(EntityPlayer player, int slot) {
        if (player == null) return null;
        if (player.inventory == null) return null;
        if (slot < 0 || slot >= player.inventory.armorInventory.length) return null;
        return player.inventory.armorInventory[slot];
    }

    // Returns the armor stack or null when empty; keep callers null-safe.
    public static ItemStack getArmorOrEmpty(EntityPlayer player, int slot) {
        return getArmor(player, slot);
    }

    // Random float in [min, max]
    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max);
    }

    // Round to decimal places (useful for legacy helper)
    public static float round(float num, float decimalPlaces) {
        float factor = (float) Math.pow(10, decimalPlaces);
        return Math.round(num * factor) / factor;
    }
}