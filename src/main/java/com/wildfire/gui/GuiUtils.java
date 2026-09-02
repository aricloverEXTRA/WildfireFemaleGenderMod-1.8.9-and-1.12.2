package com.wildfire.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;

public final class GuiUtils {
    private GuiUtils() {}

    public static void drawEntityOnScreenNoScissor(GuiScreen screen, int posX, int posY, int scale,
                                                   float mouseX, float mouseY, EntityLivingBase entity) {

        GuiInventory.drawEntityOnScreen(posX, posY, scale, -mouseX, -mouseY, entity);
    }
}
