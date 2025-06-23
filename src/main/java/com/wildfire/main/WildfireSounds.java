package com.wildfire.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WildfireSounds {
    private static final ResourceLocation FEMALE_DAMAGE = new ResourceLocation("wildfire_gender", "female_damage");
    private static final ResourceLocation FEMALE_DAMAGE2 = new ResourceLocation("wildfire_gender", "female_damage2");

    public static void playSound(net.minecraft.entity.player.EntityPlayer player) {
        if (player.worldObj.isRemote) {
            ResourceLocation sound = Math.random() < 0.5 ? FEMALE_DAMAGE : FEMALE_DAMAGE2;
            Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, 1.0F, 1.0F, (float) player.posX, (float) player.posY, (float) player.posZ));
        }
    }
}