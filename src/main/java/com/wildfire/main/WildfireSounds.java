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

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
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

        float pitch = settings.voicePitch / 100.0F;

        boolean played = false;
        for (String key : new String[]{SOUND_KEY1, SOUND_KEY2}) {
            for (String domain : TRY_DOMAINS) {
                if (!resourceExists(domain, key)) continue;

                ResourceLocation candidate = new ResourceLocation(domain, key);
                try {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(candidate, pitch));
                    played = true;
                    lastPlayedPerPlayer.put(playerName, currentTime);
                    break;
                } catch (Throwable t) {
                    System.err.println("[WFG] playFemaleHurt: failed to play " + candidate + " -> " + t.getMessage());
                }
            }
            if (played) break;
        }

        if (!played) {
            try {
                player.playSound("random.hurt", 1.0F, pitch);
                lastPlayedPerPlayer.put(playerName, currentTime);
            } catch (Throwable t) {
                System.err.println("[WFG] playFemaleHurt: final fallback failed: " + t.getMessage());
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