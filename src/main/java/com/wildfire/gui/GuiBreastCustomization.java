package com.wildfire.gui.screen;

import com.wildfire.config.ConfigSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraft.util.ResourceLocation;

public class GuiBreastCustomization extends GuiScreen implements ISlider {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("wildfire:textures/gui/breast_customization.png");

    // Config variables
    private double breastSize = ConfigSettings.breastSize;
    private double separation = ConfigSettings.separation;
    private double depth = ConfigSettings.depth;
    private double height = ConfigSettings.height;
    private double rotation = ConfigSettings.rotation;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2 - 100;
        int centerY = (int) (this.height / 2 - 70);

        this.buttonList.add(new GuiSlider(0, centerX, centerY, 200, 20, "Breast Size: ", "%", 0.0D, 100.0D, this.breastSize, false, true, this));
        this.buttonList.add(new GuiSlider(1, centerX, centerY + 25, 200, 20, "Separation: ", "", -10.0D, 10.0D, this.separation, false, true, this));
        this.buttonList.add(new GuiSlider(2, centerX, centerY + 50, 200, 20, "Depth: ", "", -10.0D, 0.0D, this.depth, false, true, this));
        this.buttonList.add(new GuiSlider(3, centerX, centerY + 75, 200, 20, "Height: ", "", -10.0D, 10.0D, this.height, false, true, this));
        this.buttonList.add(new GuiSlider(4, centerX, centerY + 100, 200, 20, "Rotation: ", "", 0.0D, 10.0D, this.rotation, false, true, this));
        this.buttonList.add(new GuiButton(5, centerX, centerY + 135, 200, 20, "Done"));
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        switch (slider.id) {
            case 0:
                this.breastSize = slider.getValue();
                break;
            case 1:
                this.separation = slider.getValue();
                break;
            case 2:
                this.depth = slider.getValue();
                break;
            case 3:
                this.height = slider.getValue();
                break;
            case 4:
                this.rotation = slider.getValue();
                break;
            default:
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 5) {
            // Save settings
            ConfigSettings.breastSize = this.breastSize;
            ConfigSettings.separation = this.separation;
            ConfigSettings.depth = this.depth;
            ConfigSettings.height = this.height;
            ConfigSettings.rotation = this.rotation;
            ConfigSettings.saveConfig();

            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background texture
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int k = (this.width - 248) / 2;
        int l = (int) ((this.height - 166) / 2);
        this.drawModalRectWithCustomSizedTexture(k, l, 0, 0, 248, 166, 248, 166);

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, l + 10, 0xFFFFFF);
    }
}
