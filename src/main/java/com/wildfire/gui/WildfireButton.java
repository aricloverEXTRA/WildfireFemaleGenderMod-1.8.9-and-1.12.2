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
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int color = this.hovered ? 0xFFFFFFFF : 0x66666666;
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, color);
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }
}
