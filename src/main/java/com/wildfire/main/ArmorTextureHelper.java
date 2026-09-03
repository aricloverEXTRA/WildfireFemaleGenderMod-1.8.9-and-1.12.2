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
            if (uuid == null) return null;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) return null;
            net.minecraft.client.entity.AbstractClientPlayer target = null;
            if (mc.theWorld != null) {
                for (Object o : mc.theWorld.playerEntities) {
                    if (o instanceof net.minecraft.client.entity.AbstractClientPlayer) {
                        net.minecraft.client.entity.AbstractClientPlayer p = (net.minecraft.client.entity.AbstractClientPlayer) o;
                        if (uuid.equals(p.getUniqueID())) { target = p; break; }
                    }
                }
            }
            EntityPlayer lp = target != null ? target : mc.thePlayer;
            if (lp == null || !uuid.equals(lp.getUniqueID())) {
                if (mc.thePlayer != null && uuid.equals(mc.thePlayer.getUniqueID())) lp = mc.thePlayer;
                else return null;
            }
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

    public static ResourceLocation getArmorTextureForPlayer(net.minecraft.client.entity.AbstractClientPlayer player, boolean overlay) {
        try {
            if (player == null) return null;
            ItemStack chest = player.inventory.armorInventory[2];
            if (chest == null || chest.getItem() == null) return null;
            if (!(chest.getItem() instanceof ItemArmor)) return null;
            ItemArmor ia = (ItemArmor) chest.getItem();
            String texPath = ia.getArmorTexture(chest, player, 2, overlay ? "overlay" : null);
            if (texPath == null && overlay) texPath = ia.getArmorTexture(chest, player, 2, null);
            if (texPath == null) return null;
            return new ResourceLocation(texPath);
        } catch (Throwable t) { return null; }
    }
}
