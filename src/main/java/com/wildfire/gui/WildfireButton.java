package com.wildfire.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class WildfireButton extends GuiButton {
    private final boolean transparent;

    public WildfireButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this(buttonId, x, y, widthIn, heightIn, buttonText, false);
    }

    public WildfireButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, boolean transparent) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.transparent = transparent;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int bgColor = this.hovered ? 0xFF2A2A2A : 0xFF1A1A1A;
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, bgColor);

            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }
}