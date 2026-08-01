package com.wildfire.main;

import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class WildfireEventHandler {
    private static final int GENDER_MENU_KEY = Keyboard.KEY_G;

    public WildfireEventHandler() {
    }

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (Keyboard.isKeyDown(GENDER_MENU_KEY) && minecraft != null && minecraft.currentScreen == null) {
            EntityPlayer player = minecraft.thePlayer;
            if (player != null) {
                minecraft.displayGuiScreen(new GuiWardrobe());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = evt.player;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (player == null || minecraft == null || player != minecraft.thePlayer) {
            return;
        }

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null || !(player instanceof AbstractClientPlayer)) {
            return;
        }

        AbstractClientPlayer acp = (AbstractClientPlayer) player;
        GenderLayer.ensureRegisteredForPlayer(acp);
        BreastPhysics[] phys = GenderLayer.getPhysicsForPlayer(acp);
        if (phys == null) {
            return;
        }

        ItemStack chest = null;
        try {
            chest = player.inventory.armorInventory[2];
        } catch (Throwable ignored) {
        }

        com.wildfire.api.IGenderArmor armor;
        if (chest == null || !(chest.getItem() instanceof ItemArmor)) {
            armor = EmptyGenderArmor.INSTANCE;
        } else {
            ItemArmor itemArmor = (ItemArmor) chest.getItem();
            if (itemArmor == null) {
                armor = EmptyGenderArmor.INSTANCE;
            } else if (chest.getItem() == net.minecraft.init.Items.leather_chestplate) {
                armor = SimpleGenderArmor.LEATHER;
            } else if (chest.getItem() == net.minecraft.init.Items.chainmail_chestplate) {
                armor = SimpleGenderArmor.CHAINMAIL;
            } else if (chest.getItem() == net.minecraft.init.Items.golden_chestplate) {
                armor = SimpleGenderArmor.GOLD;
            } else if (chest.getItem() == net.minecraft.init.Items.iron_chestplate) {
                armor = SimpleGenderArmor.IRON;
            } else if (chest.getItem() == net.minecraft.init.Items.diamond_chestplate) {
                armor = SimpleGenderArmor.DIAMOND;
            } else {
                armor = SimpleGenderArmor.FALLBACK;
            }
        }

        if (!settings.physicsEnabled) {
            phys[0].resetPhysics();
            if (!settings.breastsUniboob) {
                phys[1].resetPhysics();
            }
            return;
        }

        boolean dualPhysics = !settings.breastsUniboob;
        if (dualPhysics) {
            phys[0].update((EntityLivingBase) player, armor);
            phys[1].update((EntityLivingBase) player, armor);
        } else {
            phys[0].update((EntityLivingBase) player, armor);
            phys[1].syncFrom(phys[0]);
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        if (event.player instanceof AbstractClientPlayer) {
            GenderLayer.unregister(event.player.getUniqueID());
        }
    }
}