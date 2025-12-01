package com.wildfire.main.handlers;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ArmorTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemArmor)) return;

        if (Minecraft.getMinecraft().player != null) {
            if (!com.wildfire.main.config.GenderConfig.getShowArmorTooltip(Minecraft.getMinecraft().player)) {
                return;
            }
        }

        ItemArmor armor = (ItemArmor) stack.getItem();
        if (armor.getEquipmentSlot() != EntityEquipmentSlot.CHEST) return;

        float support;
        if (stack.getItem() == Items.LEATHER_CHESTPLATE) {
            support = 0.3f;
        } else if (stack.getItem() == Items.CHAINMAIL_CHESTPLATE) {
            support = 0.5f;
        } else if (stack.getItem() == Items.GOLDEN_CHESTPLATE) {
            support = 0.85f;
        } else if (stack.getItem() == Items.IRON_CHESTPLATE) {
            support = 1.0f;
        } else if (stack.getItem() == Items.DIAMOND_CHESTPLATE) {
            support = 1.0f;
        } else {
            return;
        }

        String fmt = I18n.format("wildfire_gender.armor.tooltip");
        String val = String.format("%.2f", support);
        String line = String.format(fmt, val);
        String colored = "\u00A7d" + line;

        List<String> tooltip = event.getToolTip();
        tooltip.add(colored);
    }
}