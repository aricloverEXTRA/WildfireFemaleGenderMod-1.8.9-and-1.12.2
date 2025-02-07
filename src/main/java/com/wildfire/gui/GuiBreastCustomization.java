package com.wildfire.gui.screen;

import com.wildfire.config.ConfigSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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

        int guiWidth = 248;   // Width of your background texture
        int guiHeight = 166;  // Height of your background texture
        int centerX = (this.width - guiWidth) / 2;
        int centerY = (int) ((this.height - guiHeight) / 2); // Explicitly cast to int

        // Place sliders and buttons relative to the background texture
        int sliderX = centerX + 24;   // Adjust as needed to align with the background
        int sliderY = centerY + 30;   // Starting Y position for sliders
        int sliderWidth = 200;
        int sliderHeight = 20;
        int spacing = 25;   // Spacing between sliders

        this.buttonList.add(new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, this.breastSize, false, true, this));
        this.buttonList.add(new GuiSlider(1, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, this.separation, false, true, this));
        this.buttonList.add(new GuiSlider(2, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, this.depth, false, true, this));
        this.buttonList.add(new GuiSlider(3, sliderX, sliderY + spacing * 3, sliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, this.height, false, true, this));
        this.buttonList.add(new GuiSlider(4, sliderX, sliderY + spacing * 4, sliderWidth, sliderHeight, "Rotation: ", "", 0.0D, 10.0D, this.rotation, false, true, this));
        this.buttonList.add(new GuiButton(5, centerX + (guiWidth - 200) / 2, centerY + guiHeight - 30, 200, 20, "Done"));
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
        mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int guiWidth = 248;
        int guiHeight = 166;
        int centerX = (this.width - guiWidth) / 2;
        int centerY = (int) ((this.height - guiHeight) / 2); // Explicitly cast to int
        drawModalRectWithCustomSizedTexture(centerX, centerY, 0, 0, guiWidth, guiHeight, guiWidth, guiHeight);

        // Draw title
        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, centerY + 10, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
