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
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WildfireEventHandler {

    public WildfireEventHandler() {}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        try {
            EntityPlayer player = evt.player;
            if (player == null) return;
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft == null || minecraft.theWorld == null) return;
            // Only tick for local player and other players in world (client-side)
            if (!player.worldObj.isRemote) return;

            // Ensure we have settings - for remote players, use defaults
            GenderConfig.PlayerGenderSettings settings = null;
            boolean isLocal = player == minecraft.thePlayer;
            if (isLocal) {
                settings = GenderConfig.getPlayerSettings(player);
                if (settings == null) return;
                if (!(player instanceof AbstractClientPlayer)) return;
            } else {
                // For remote players, check if they should have physics (always tick if possible)
                if (!(player instanceof AbstractClientPlayer)) return;
                // Use local settings as fallback for remote rendering - or skip if no data
                settings = GenderConfig.getPlayerSettings(minecraft.thePlayer);
                if (settings == null) return;
            }

            AbstractClientPlayer acp = (AbstractClientPlayer) player;
            GenderLayer.ensureRegisteredForPlayer(acp);
            BreastPhysics[] phys = GenderLayer.getPhysicsForPlayer(acp);
            if (phys == null) return;

            ItemStack chest = null;
            try { chest = player.inventory.armorInventory[2]; } catch (Throwable ignored) {}

            com.wildfire.api.IGenderArmor armor;
            if (chest == null || !(chest.getItem() instanceof ItemArmor)) {
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

            // For remote players, always tick physics if they have breasts
            boolean physicsEnabled = isLocal ? settings.physicsEnabled : true;
            boolean uniboob = isLocal ? settings.breastsUniboob : false;

            if (!physicsEnabled) {
                phys[0].resetPhysics();
                if (!uniboob) phys[1].resetPhysics();
                return;
            }

            boolean dualPhysics = !uniboob;
            if (dualPhysics) {
                phys[0].update((EntityLivingBase) player, armor);
                phys[1].update((EntityLivingBase) player, armor);
            } else {
                phys[0].update((EntityLivingBase) player, armor);
                phys[1].syncFrom(phys[0]);
            }
        } catch (Throwable t) {
            // Never crash on tick
            System.err.println("[WFG] onPlayerTick error: " + t.getMessage());
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {}

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {}

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        try {
            if (event.player != null) {
                GenderLayer.unregister(event.player.getUniqueID());
            }
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        try {
            // Cleanup old physics entries periodically
            WildfireSounds.cleanupOldEntries();
        } catch (Throwable ignored) {}
    }
}
