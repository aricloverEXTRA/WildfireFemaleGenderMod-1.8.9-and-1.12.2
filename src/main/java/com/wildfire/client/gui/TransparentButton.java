package com.wildfire.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class TransparentButton extends GuiButton {

    public TransparentButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int textColor = 0xFFFFFF;
            if (!this.enabled) {
                textColor = 0xA0A0A0;
            } else if (this.hovered) {
                textColor = 0xFFFFA0;
            }
            mc.fontRendererObj.drawString(this.displayString, this.xPosition + this.width / 2 - mc.fontRendererObj.getStringWidth(this.displayString) / 2,
                    this.yPosition + (this.height - 8) / 2, textColor);
        }
    }
}
