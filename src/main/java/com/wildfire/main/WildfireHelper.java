package com.wildfire.main;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class WildfireHelper {
    public static ItemStack getArmor(EntityPlayer player, int slot) {
        return player.inventory.armorInventory[slot];
    }

    public static ItemStack getArmorOrEmpty(EntityPlayer player, int slot) {
        return player.inventory.armorInventory[slot];
    }
}