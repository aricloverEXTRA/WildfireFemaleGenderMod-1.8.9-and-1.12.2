package com.wildfire.client.gui.screen;

import com.wildfire.client.gui.screen.GuiBreastCustomization;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class GuiFirstTime extends GuiScreen {
    private static final ResourceLocation FIRST_TIME_BG = new ResourceLocation("wildfire:textures/gui/first_time_bg.png");
    private boolean clicked = false;

    @Override
    public void initGui() {
        // Initialization if needed
    }

    @Override
    public void updateScreen() {
        if (Mouse.isButtonDown(0)) {
            if (!clicked) {
                clicked = true;
                // Proceed to next screen or close GUI
                this.mc.displayGuiScreen(new GuiBreastCustomization());
            }
        } else {
            clicked = false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(FIRST_TIME_BG);
        int posX = (this.width - 256) / 2;
        int posY = (this.height - 256) / 2;
        this.drawTexturedModalRect(posX, posY, 0, 0, 256, 256);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
