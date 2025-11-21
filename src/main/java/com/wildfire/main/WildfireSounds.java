package com.wildfire.main;

import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WildfireSounds {
    // Use your mod id constant so assets are looked up under the correct domain
    private static final ResourceLocation FEMALE_DAMAGE = new ResourceLocation(WildfireGenderMod.MODID, "female_damage");
    private static final ResourceLocation FEMALE_DAMAGE2 = new ResourceLocation(WildfireGenderMod.MODID, "female_damage2");

    private static boolean hasPlayedHurt = false;

    public static void preInit(FMLPreInitializationEvent event) {
        // Ensure sounds.json and .ogg files exist in assets/<MODID>/sounds/
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (!player.worldObj.isRemote) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.hurtSoundsEnabled || settings.voicePitch <= 0) return;

        // Reset flag when invulnerability ends so a subsequent hurt can play again
        if (player.hurtResistantTime == 0) {
            hasPlayedHurt = false;
        }
    }

    public static void playSound(EntityPlayer player) {
        if (!player.worldObj.isRemote) return;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
    }

    private static void playFemaleHurt(EntityPlayer player, GenderConfig.PlayerGenderSettings settings) {
        if (hasPlayedHurt) return;
        ResourceLocation sound = Math.random() < 0.5 ? FEMALE_DAMAGE : FEMALE_DAMAGE2;
        float pitch = settings.voicePitch / 100.0F;
        // ensure sound is played on client sound handler
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(sound, pitch));
        hasPlayedHurt = true;
    }
}