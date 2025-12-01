package com.wildfire.main;

import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import org.lwjgl.input.Keyboard;

public class WildfireEventHandler {

    private static final int GENDER_MENU_KEY = Keyboard.KEY_G;

    public WildfireEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (Keyboard.isKeyDown(GENDER_MENU_KEY) && mc.currentScreen == null) {
            EntityPlayer player = mc.player;
            if (player != null) {
                mc.displayGuiScreen(new GuiWardrobe());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = evt.player;
        if (player != mc.player) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null) return;
        if (!(player instanceof net.minecraft.client.entity.AbstractClientPlayer)) return;

        net.minecraft.client.entity.AbstractClientPlayer acp = (net.minecraft.client.entity.AbstractClientPlayer) player;

        GenderLayer.ensureRegisteredForPlayer(acp);
        BreastPhysics[] phys = GenderLayer.getPhysicsForPlayer(acp);
        if (phys == null) return;

        ItemStack chest = ItemStack.EMPTY;
        try {
            chest = player.inventory.armorInventory.get(2);
        } catch (Throwable ignored) {}

        final com.wildfire.api.IGenderArmor armor;
        if (chest.isEmpty() || !(chest.getItem() instanceof ItemArmor)) {
            armor = EmptyGenderArmor.INSTANCE;
        } else {
            ItemArmor ia = (ItemArmor) chest.getItem();
            if (chest.getItem() == Items.LEATHER_CHESTPLATE) {
                armor = SimpleGenderArmor.LEATHER;
            } else if (chest.getItem() == Items.CHAINMAIL_CHESTPLATE) {
                armor = SimpleGenderArmor.CHAINMAIL;
            } else if (chest.getItem() == Items.GOLDEN_CHESTPLATE) {
                armor = SimpleGenderArmor.GOLD;
            } else if (chest.getItem() == Items.IRON_CHESTPLATE) {
                armor = SimpleGenderArmor.IRON;
            } else if (chest.getItem() == Items.DIAMOND_CHESTPLATE) {
                armor = SimpleGenderArmor.DIAMOND;
            } else {
                armor = SimpleGenderArmor.FALLBACK;
            }
        }

        if (!settings.physicsEnabled) {
            phys[0].resetPhysics();
            if (!settings.breastsUniboob) phys[1].resetPhysics();
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
        Minecraft mc = Minecraft.getMinecraft();
        if (event.player == mc.player) {
            if (event.player instanceof net.minecraft.client.entity.AbstractClientPlayer) {
                net.minecraft.client.entity.AbstractClientPlayer acp =
                        (net.minecraft.client.entity.AbstractClientPlayer) event.player;
                GenderLayer.unregister(acp.getUniqueID());
            }
        }
    }
}