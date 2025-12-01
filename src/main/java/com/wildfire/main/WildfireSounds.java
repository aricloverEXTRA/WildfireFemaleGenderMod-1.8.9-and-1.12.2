package com.wildfire.main;

import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;

public class WildfireSounds {
    private static final String SOUND_KEY1 = "female_damage";
    private static final String SOUND_KEY2 = "female_damage2";

    private static final String[] TRY_DOMAINS = new String[] {
            WildfireGenderMod.MODID,
            "wildfire_gender"
    };

    private boolean hasPlayedHurt = false;

    public static void preInit(FMLPreInitializationEvent event) {
        System.out.println("[WFG] WildfireSounds.preInit: modid=" + WildfireGenderMod.MODID);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!player.world.isRemote) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            if (!hasPlayedHurt) {
                playFemaleHurt(player, settings);
                hasPlayedHurt = true;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.player.world.isRemote) return;

        if (event.player.hurtResistantTime == 0) {
            hasPlayedHurt = false;
        }
    }

    public static void playSoundForPlayer(EntityPlayer player) {
        if (!player.world.isRemote) return;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && settings.voicePitch > 0) {
            playFemaleHurt(player, settings);
        }
    }

    private static void playFemaleHurt(EntityPlayer player, GenderConfig.PlayerGenderSettings settings) {
        float pitch = settings.voicePitch / 100.0F;

        SoundEvent found = findRegisteredSoundEvent();
        if (found != null) {
            try {
                Minecraft.getMinecraft().getSoundHandler()
                        .playSound(PositionedSoundRecord.getMasterRecord(found, pitch));
                return;
            } catch (Throwable t) {
                System.err.println("[WFG] playFemaleHurt: failed to play SoundEvent " + found.getRegistryName() + " -> " + t.getMessage());
            }
        }

        try {
            player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0F, pitch);
        } catch (Throwable t) {
            System.err.println("[WFG] playFemaleHurt: fallback failed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static SoundEvent findRegisteredSoundEvent() {
        for (String key : new String[]{SOUND_KEY1, SOUND_KEY2}) {
            for (String domain : TRY_DOMAINS) {
                SoundEvent evt = SoundEvent.REGISTRY.getObject(new ResourceLocation(domain, key));
                if (evt != null) return evt;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean resourceExists(String domain, String soundKey) {
        ResourceLocation rl = new ResourceLocation(domain, "sounds/" + soundKey + ".ogg");
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            if (res != null) {
                try { if (res.getInputStream() != null) res.getInputStream().close(); } catch (IOException ignored) {}
                return true;
            }
        } catch (IOException ignored) {
        } catch (Throwable t) {
            System.err.println("[WFG] resourceExists: error checking " + rl + ": " + t.getMessage());
        }
        return false;
    }
}