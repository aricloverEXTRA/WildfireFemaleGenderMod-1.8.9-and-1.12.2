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

/**
 * WildfireSounds - Forge 1.8.9 friendly event handler.
 *
 * Note: instance methods for @SubscribeEvent are required when registering an instance
 * with MinecraftForge.EVENT_BUS.register(new WildfireSounds()).
 */
public class WildfireSounds {
    private static final String SOUND_KEY1 = "female_damage";
    private static final String SOUND_KEY2 = "female_damage2";

    // domains to try (primary modid first, legacy second)
    private static final String[] TRY_DOMAINS = new String[] {
            WildfireGenderMod.MODID,
            "wildfire_gender"
    };

    // per‑client simple suppression guard
    private boolean hasPlayedHurt = false;

    public static void preInit(FMLPreInitializationEvent event) {
        System.out.println("[WFG] WildfireSounds.preInit: modid=" + WildfireGenderMod.MODID);
    }

    /**
     * Instance event handler for living hurt events.
     * Registered via MinecraftForge.EVENT_BUS.register(new WildfireSounds()) in ClientProxy.
     */
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

    /**
     * Instance event handler for player tick - used to reset the hasPlayedHurt suppression flag.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.hurtSoundsEnabled || settings.voicePitch <= 0) return;

        if (player.hurtResistantTime == 0) {
            // reset per-player suppression — this instance tracks only its own last-played state,
            // we use the instance field hasPlayedHurt for simplicity; it's per-client, not per-player,
            // which is fine for single-client local playback.
            hasPlayedHurt = false;
        }
    }

    /**
     * Helper used to attempt to play a female hurt sound for the given player.
     * Runs on client only.
     */
    public static void playSoundForPlayer(EntityPlayer player) {
        if (!player.worldObj.isRemote) return;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
    }

    private static void playFemaleHurt(EntityPlayer player, GenderConfig.PlayerGenderSettings settings) {
        // compute pitch
        float pitch = settings.voicePitch / 100.0F;

        boolean played = false;
        for (String key : new String[]{SOUND_KEY1, SOUND_KEY2}) {
            for (String domain : TRY_DOMAINS) {
                boolean exists = resourceExists(domain, key);
                if (!exists) continue;

                ResourceLocation candidate = new ResourceLocation(domain, key);
                try {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(candidate, pitch));
                    played = true;
                    break;
                } catch (Throwable t) {
                    System.err.println("[WFG] playFemaleHurt: failed to play " + candidate + " -> " + t.getMessage());
                }
            }
            if (played) break;
        }

        if (!played) {
            try {
                // fallback to vanilla hurt sound with adjusted pitch
                player.playSound("random.hurt", 1.0F, pitch);
            } catch (Throwable t) {
                System.err.println("[WFG] playFemaleHurt: final fallback failed: " + t.getMessage());
                t.printStackTrace();
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
                } catch (IOException ioe) {
                    // ignore close errors
                }
                return true;
            }
        } catch (IOException ioe) {
            // file not found
        } catch (Throwable t) {
            System.err.println("[WFG] resourceExists: unexpected error while checking " + rl + ": " + t.getMessage());
        }
        return false;
    }
}