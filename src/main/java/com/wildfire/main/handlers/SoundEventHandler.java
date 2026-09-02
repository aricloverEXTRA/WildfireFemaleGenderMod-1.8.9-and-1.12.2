package com.wildfire.main.handlers;

import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Intercepts vanilla hurt sounds and replaces with female hurt sounds for Female/Other players
 * Like Fabric's LivingEntityMixin and Neo's PlayLevelSoundEvent
 */
@SideOnly(Side.CLIENT)
public class SoundEventHandler {

    private static final String[] VANILLA_HURT_SOUNDS = {
        "game.player.hurt", "random.hurt", "game.neutral.hurt"
    };

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        try {
            if (event.sound == null || event.name == null) return;
            String name = event.name;
            boolean isHurt = false;
            for (String hurt : VANILLA_HURT_SOUNDS) {
                if (name.equals(hurt) || name.contains("hurt") || name.contains("damage")) {
                    isHurt = true;
                    break;
                }
            }
            if (!isHurt) return;

            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null) return;
            EntityPlayer player = mc.thePlayer;
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
            if (settings == null || !settings.hurtSoundsEnabled) return;
            if ("Male".equals(settings.gender)) return;

            // Only replace if this is the local player's hurt sound
            // Check if player is actually hurt (hurtTime > 0)
            if (player.hurtTime == 0) return;

            // Replace with female hurt sound
            float basePitch = settings.voicePitch / 100f;
            float pitchVariation = (player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.2F;
            float pitch = basePitch + pitchVariation;

            // Try female sounds
            String[] trySounds = {"female_damage", "female_damage2"};
            String[] domains = {"wildfire_gender", "femalegendermodlegacyforge"};
            for (String key : trySounds) {
                for (String domain : domains) {
                    ResourceLocation rl = new ResourceLocation(domain, key);
                    try {
                        event.sound = PositionedSoundRecord.create(rl, pitch);
                        event.result = event.sound;
                        return;
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            // Don't crash on sound handling
        }
    }
}
