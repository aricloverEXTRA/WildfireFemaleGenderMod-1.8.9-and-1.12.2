package com.wildfire.main.handlers;

import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ArmorTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.itemStack;
        if (stack == null) return;
        if (!(stack.getItem() instanceof ItemArmor)) return;

        if (Minecraft.getMinecraft().thePlayer != null) {
            if (!com.wildfire.main.config.GenderConfig.getShowArmorTooltip(Minecraft.getMinecraft().thePlayer)) {
                return;
            }
        }

        ItemArmor armor = (ItemArmor) stack.getItem();
        if (armor.armorType != 1) return;

        float support = 0f;
        if (stack.getItem() == Items.leather_chestplate) {
            support = 0.3f;
        } else if (stack.getItem() == Items.chainmail_chestplate) {
            support = 0.5f;
        } else if (stack.getItem() == Items.golden_chestplate) {
            support = 0.85f;
        } else if (stack.getItem() == Items.iron_chestplate) {
            support = 1.0f;
        } else if (stack.getItem() == Items.diamond_chestplate) {
            support = 1.0f;
        } else {
            return;
        }

        String fmt = StatCollector.translateToLocal("wildfire_gender.armor.tooltip");
        String val = String.format("%.2f", support);
        String line = String.format(fmt, val);
        String colored = "\u00A7d" + line;

        List<String> tooltip = event.toolTip;
        tooltip.add(colored);
    }
}
