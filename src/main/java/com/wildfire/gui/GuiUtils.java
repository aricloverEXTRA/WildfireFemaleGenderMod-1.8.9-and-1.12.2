package com.wildfire.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;

public final class GuiUtils {
    private GuiUtils() {}

    /**
     * Draws an entity on screen, inventory-style, following the cursor.
     *
     * @param screen the GUI screen (unused, kept for API compatibility)
     * @param posX   x position
     * @param posY   y position
     * @param scale  render scale
     * @param mouseX mouse x offset (relative to posX)
     * @param mouseY mouse y offset (relative to posY)
     * @param entity the entity to render
     */
    public static void drawEntityOnScreenNoScissor(GuiScreen screen, int posX, int posY, int scale,
                                                   float mouseX, float mouseY, EntityLivingBase entity) {
        GuiInventory.drawEntityOnScreen(posX, posY, scale, mouseX, mouseY, entity);
    }
}
