package com.wildfire.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;

public class GuiBreastCustomization extends GuiScreen implements ISlider {
    private double breastSize = 50.0;
    private double separation = 0.0;
    private double depth = -5.0;
    private double height = 0.0;
    private double rotation = 5.0;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int sliderWidth = 200;
        int sliderHeight = 20;
        int centerX = this.width / 2 - 100;
        int centerY = (int) (this.height / 2 - 70);

        this.buttonList.add(new GuiSlider(0, centerX, centerY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, this.breastSize, false, true, this));
        this.buttonList.add(new GuiSlider(1, centerX, centerY + 25, sliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, this.separation, false, true, this));
        this.buttonList.add(new GuiSlider(2, centerX, centerY + 50, sliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, this.depth, false, true, this));
        this.buttonList.add(new GuiSlider(3, centerX, centerY + 75, sliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, this.height, false, true, this));
        this.buttonList.add(new GuiSlider(4, centerX, centerY + 100, sliderWidth, sliderHeight, "Rotation: ", "", 0.0D, 10.0D, this.rotation, false, true, this));
        this.buttonList.add(new GuiButton(5, centerX, centerY + 135, sliderWidth, sliderHeight, "Done"));
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
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, 15, 0xFFFFFF);
    }
}
