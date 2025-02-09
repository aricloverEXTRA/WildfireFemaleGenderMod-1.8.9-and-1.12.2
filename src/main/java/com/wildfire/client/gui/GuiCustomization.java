package com.wildfire.client.gui.screen;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import com.wildfire.client.gui.screen.GuiBreastCustomization;

public class GuiCustomization extends GuiScreen {
    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 - 25, "Customize Breasts"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiBreastCustomization());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Customization GUI", this.width / 2, 40, 0xFFFFFF);
    }
}
