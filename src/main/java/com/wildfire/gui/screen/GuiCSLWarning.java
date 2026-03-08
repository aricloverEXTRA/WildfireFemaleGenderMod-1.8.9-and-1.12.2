package com.wildfire.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiCSLWarning extends GuiScreen {

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = width / 2;
        int centerY = height / 2;

        this.buttonList.add(new GuiButton(0, centerX - 60, centerY + 20, 120, 20, "Continue Anyway"));
        this.buttonList.add(new GuiButton(1, centerX - 60, centerY + 45, 120, 20, "Go Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            // Continue to credits
            mc.displayGuiScreen(new WildfireCreditsScreen());
        } else if (button.id == 1) {
            // Go back to wardrobe
            mc.displayGuiScreen(new GuiWardrobe());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        drawCenteredString(fontRendererObj,
                "CustomSkinLoader is not installed.",
                width / 2, height / 2 - 40, 0xFF5555);

        drawCenteredString(fontRendererObj,
                "Skins may appear as Steve/Alex.",
                width / 2, height / 2 - 25, 0xFFFFFF);

        drawCenteredString(fontRendererObj,
                "Install CustomSkinLoader for proper skin rendering.",
                width / 2, height / 2 - 10, 0xAAAAAA);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}