package com.wildfire.main;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class WildfireHelper {

    public static ItemStack getArmor(EntityPlayer player, int slot) {
        if (player == null || player.inventory == null) return ItemStack.EMPTY;
        if (slot < 0 || slot >= player.inventory.armorInventory.size()) return ItemStack.EMPTY;
        return player.inventory.armorInventory.get(slot);
    }

    public static ItemStack getArmorOrEmpty(EntityPlayer player, int slot) {
        return getArmor(player, slot);
    }

    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max);
    }

    public static float round(float num, float decimalPlaces) {
        float factor = (float) Math.pow(10, decimalPlaces);
        return Math.round(num * factor) / factor;
    }
}
