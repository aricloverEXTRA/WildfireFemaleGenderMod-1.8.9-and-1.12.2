package com.wildfire.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.wildfire.main.config.GenderConfig;

public class WildfireSounds {
    private static final ResourceLocation FEMALE_DAMAGE = new ResourceLocation("wildfire_gender", "female_damage");
    private static final ResourceLocation FEMALE_DAMAGE2 = new ResourceLocation("wildfire_gender", "female_damage2");
    private static boolean hasPlayedHurt = false;

    public static void preInit(FMLPreInitializationEvent event) {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.entityLiving instanceof net.minecraft.entity.player.EntityPlayer && event.entityLiving.worldObj.isRemote) {
            net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) event.entityLiving;
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
            if (settings != null && settings.hurtSoundsEnabled && !hasPlayedHurt && settings.voicePitch > 0) {
                ResourceLocation sound = Math.random() < 0.5 ? FEMALE_DAMAGE : FEMALE_DAMAGE2;
                float pitch = settings.voicePitch / 100.0F;
                Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, pitch, 1.0F, (float) player.posX, (float) player.posY, (float) player.posZ));
                hasPlayedHurt = true;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.worldObj.isRemote) {
            net.minecraft.entity.player.EntityPlayer player = event.player;
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
            if (settings != null && settings.hurtSoundsEnabled && player.hurtResistantTime > 0 && !hasPlayedHurt && settings.voicePitch > 0) {
                ResourceLocation sound = Math.random() < 0.5 ? FEMALE_DAMAGE : FEMALE_DAMAGE2;
                float pitch = settings.voicePitch / 100.0F;
                Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, pitch, 1.0F, (float) player.posX, (float) player.posY, (float) player.posZ));
                hasPlayedHurt = true;
            } else if (player.hurtResistantTime == 0) {
                hasPlayedHurt = false;
            }
        }
    }

    public static void playSound(net.minecraft.entity.player.EntityPlayer player) {
        if (player.worldObj.isRemote) {
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
            if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
                ResourceLocation sound = Math.random() < 0.5 ? FEMALE_DAMAGE : FEMALE_DAMAGE2;
                float pitch = settings.voicePitch / 100.0F;
                Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, pitch, 1.0F, (float) player.posX, (float) player.posY, (float) player.posZ));
            }
        }
    }
}