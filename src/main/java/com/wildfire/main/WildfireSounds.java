package com.wildfire.main;

import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXED: Track last-played time per-player instead of global flag.
 * Prevents memory bloat from per-instance fields.
 */
public class WildfireSounds {
    private static final String SOUND_KEY1 = "female_damage";
    private static final String SOUND_KEY2 = "female_damage2";
    private static final String[] TRY_DOMAINS = new String[] {
            WildfireGenderMod.MODID,
            "wildfire_gender"
    };

    // FIXED: Per-player suppression tracking instead of single instance field
    private static final ConcurrentHashMap<String, Long> lastPlayedPerPlayer = new ConcurrentHashMap<>();
    private static final long SUPPRESSION_TIME_MS = 100L;  // Suppress for 100ms

    public static void preInit(FMLPreInitializationEvent event) {
        System.out.println("[WFG] WildfireSounds.preInit: modid=" + WildfireGenderMod.MODID);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (!player.worldObj.isRemote) return;
        // Fabric: only for Female/Other, check gender
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.hurtSoundsEnabled || settings.voicePitch <= 0) return;
        if ("Male".equals(settings.gender)) return;
        // Fabric: check hurtTime == hurtDuration to avoid double-play from server packet
        // In 1.8.9, use hurtTime check
        if (player.hurtTime != player.maxHurtTime && player.hurtTime > 0) {
            // Only play on initial hurt, not every tick
            if (player.hurtTime != player.hurtResistantTime) return;
        }
        playFemaleHurt(player, settings);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.hurtSoundsEnabled || settings.voicePitch <= 0) return;

        // FIXED: Clean up old suppression entries periodically
        if (player.hurtResistantTime == 0) {
            String playerName = player.getName();
            lastPlayedPerPlayer.remove(playerName);
        }
    }

    public static void playSoundForPlayer(EntityPlayer player) {
        if (!player.worldObj.isRemote) return;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
    }

    private static void playFemaleHurt(EntityPlayer player, GenderConfig.PlayerGenderSettings settings) {
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        long lastPlayed = lastPlayedPerPlayer.getOrDefault(playerName, 0L);

        // FIXED: Per-player suppression instead of global
        if (currentTime - lastPlayed < SUPPRESSION_TIME_MS) {
            return;  // Already played recently
        }

        // Fabric: pitch = voicePitch (0.8-1.2) + random variation ±0.2
        float basePitch = settings.voicePitch / 100.0F; // 0.8-1.2
        float pitchVariation = (player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.2F;
        float pitch = basePitch + pitchVariation;

        boolean played = false;
        // Fabric uses single sound event "female_hurt" with 2 variants - we try both
        for (String key : new String[]{SOUND_KEY1, SOUND_KEY2}) {
            for (String domain : TRY_DOMAINS) {
                ResourceLocation candidate = new ResourceLocation(domain, key);
                try {
                    // Use PositionedSoundRecord with correct pitch
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(candidate, pitch));
                    played = true;
                    lastPlayedPerPlayer.put(playerName, currentTime);
                    break;
                } catch (Throwable t) {
                    // Try next domain
                }
            }
            if (played) break;
        }

        if (!played) {
            try {
                // Fallback: play with pitch variation like Fabric
                player.playSound("game.player.hurt", 1.0F, pitch);
                lastPlayedPerPlayer.put(playerName, currentTime);
            } catch (Throwable t) {
                try {
                    player.playSound("random.hurt", 1.0F, pitch);
                    lastPlayedPerPlayer.put(playerName, currentTime);
                } catch (Throwable t2) {
                    System.err.println("[WFG] playFemaleHurt: fallback failed: " + t2.getMessage());
                }
            }
        }
    }

    private static boolean resourceExists(String domain, String soundKey) {
        ResourceLocation rl = new ResourceLocation(domain, "sounds/" + soundKey + ".ogg");
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            if (res != null) {
                try {
                    if (res.getInputStream() != null) res.getInputStream().close();
                } catch (IOException ignored) {}
                return true;
            }
        } catch (IOException ignored) {
            // File not found
        } catch (Throwable t) {
            System.err.println("[WFG] resourceExists: unexpected error checking " + rl + ": " + t.getMessage());
        }
        return false;
    }

    // FIXED: Clean up old entries periodically
    public static void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        lastPlayedPerPlayer.entrySet().removeIf(e -> currentTime - e.getValue() > 60000L);  // Remove >60s old
    }
}