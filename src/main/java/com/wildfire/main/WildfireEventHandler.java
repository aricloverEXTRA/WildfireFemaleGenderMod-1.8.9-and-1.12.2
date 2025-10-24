package com.wildfire.main;

import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class WildfireEventHandler {
    private static final int GENDER_MENU_KEY = Keyboard.KEY_G;

    public WildfireEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(WildfireSounds.class);
    }

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;

        if (Keyboard.isKeyDown(GENDER_MENU_KEY) && Minecraft.getMinecraft().currentScreen == null) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;

        EntityPlayer player = evt.player;
        if (player != Minecraft.getMinecraft().thePlayer) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.physicsEnabled) return;

        BreastPhysics[] phys = GenderLayer.getPhysics((net.minecraft.client.entity.AbstractClientPlayer) player);
        if (phys != null) {
            phys[0].update(player, settings.breastSize, settings.intensity, settings.momentum);
            phys[1].update(player, settings.breastSize, settings.intensity, settings.momentum);
        }
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (!event.entity.worldObj.isRemote) return;
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        if (player != Minecraft.getMinecraft().thePlayer) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings != null && settings.hurtSoundsEnabled && "Female".equals(settings.gender)) {
            WildfireSounds.playSound(player);
        }
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player != Minecraft.getMinecraft().thePlayer) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !settings.physicsEnabled) return;

        BreastPhysics[] phys = GenderLayer.getPhysics((net.minecraft.client.entity.AbstractClientPlayer) player);
        if (phys != null) {
            phys[0].applyAttackImpulse(player, settings.breastSize, settings.intensity, settings.momentum);
            phys[1].applyAttackImpulse(player, settings.breastSize, settings.intensity, settings.momentum);
        }
    }
}
