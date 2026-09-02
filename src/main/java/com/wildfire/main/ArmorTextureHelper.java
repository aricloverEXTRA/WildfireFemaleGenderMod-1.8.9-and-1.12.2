package com.wildfire.main;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public final class ArmorTextureHelper {

    private ArmorTextureHelper() {}

    public static ResourceLocation getArmorTextureForPlayerUUID(UUID uuid) {
        return getArmorTextureForPlayerUUID(uuid, false);
    }

    public static ResourceLocation getArmorTextureForPlayerUUID(UUID uuid, boolean overlay) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || uuid == null) return null;
            EntityPlayer lp = mc.thePlayer;
            if (!lp.getUniqueID().equals(uuid)) return null;

            ItemStack chest = null;
            try {
                chest = lp.inventory.armorInventory[2];
            } catch (Throwable ignored) {}

            if (chest == null || chest.getItem() == null) return null;
            if (!(chest.getItem() instanceof ItemArmor)) return null;

            ItemArmor ia = (ItemArmor) chest.getItem();
            String texPath = ia.getArmorTexture(chest, lp, 2, overlay ? "overlay" : null);
            if (texPath == null && overlay) {
                texPath = ia.getArmorTexture(chest, lp, 2, null);
            }
            if (texPath == null) return null;
            return new ResourceLocation(texPath);
        } catch (Throwable t) {
            return null;
        }
    }
}
