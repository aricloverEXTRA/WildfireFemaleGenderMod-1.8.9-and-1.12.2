package com.wildfire.client.gui.screen;

import com.wildfire.config.ConfigSettings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiBreastCustomization extends GuiScreen implements ISlider {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization.png");

    private float breastSize = ConfigSettings.breastSize;
    private float separation = ConfigSettings.separation;
    private float depth = ConfigSettings.depth;
    private float height = ConfigSettings.height;
    private float rotation = ConfigSettings.rotation;

    private String[] genders = {"Male", "Female", "Other"};
    private int genderIndex;

    private CustomButton genderButton;
    private GuiSlider breastSlider, separationSlider, depthSlider, heightSlider, rotationSlider;

    @Override
    public void initGui() {
        this.buttonList.clear();

        String configGender = ConfigSettings.gender;
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(configGender)) {
                genderIndex = i;
                break;
            }
        }

        int guiWidth = 248;
        int guiHeight = 166;
        int centerX = (this.width - guiWidth) / 2;
        int centerY = (int) ((this.height - guiHeight) / 2);

        int sliderX = centerX + 24;
        int sliderY = centerY + 50;
        int sliderWidth = 200;
        int sliderHeight = 20;
        int spacing = 25;

        genderButton = new CustomButton(10, centerX + (guiWidth - 200) / 2, centerY + 20, 200, 20, getGenderDisplayString());
        this.buttonList.add(genderButton);

        breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, this.breastSize, false, true, this);
        separationSlider = new GuiSlider(1, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, this.separation, false, true, this);
        depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, this.depth, false, true, this);
        heightSlider = new GuiSlider(3, sliderX, sliderY + spacing * 3, sliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, this.height, false, true, this);
        rotationSlider = new GuiSlider(4, sliderX, sliderY + spacing * 4, sliderWidth, sliderHeight, "Rotation: ", "", 0.0D, 10.0D, this.rotation, false, true, this);

        this.buttonList.add(breastSlider);
        this.buttonList.add(separationSlider);
        this.buttonList.add(depthSlider);
        this.buttonList.add(heightSlider);
        this.buttonList.add(rotationSlider);

        this.buttonList.add(new CustomButton(5, centerX + (guiWidth - 200) / 2, centerY + guiHeight - 30, 200, 20, "Done"));

        updateSlidersState();
    }

    private void updateSlidersState() {
        String currentGender = getCurrentGender();
        boolean slidersEnabled = !currentGender.equals("Male");
        breastSlider.enabled = slidersEnabled;
        separationSlider.enabled = slidersEnabled;
        depthSlider.enabled = slidersEnabled;
        heightSlider.enabled = slidersEnabled;
        rotationSlider.enabled = slidersEnabled;
    }

    private String getCurrentGender() {
        return genders[genderIndex];
    }

    private String getGenderDisplayString() {
        return "Gender: " + getCurrentGender();
    }

    private int getGenderTextColor() {
        switch (getCurrentGender()) {
            case "Male":
                return 0x0000FF;
            case "Female":
                return 0xFF00FF;
            case "Other":
                return 0x00FF00;
            default:
                return 0xFFFFFF;
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        switch (slider.id) {
            case 0:
                this.breastSize = (float) slider.getValue();
                break;
            case 1:
                this.separation = (float) slider.getValue();
                break;
            case 2:
                this.depth = (float) slider.getValue();
                break;
            case 3:
                this.height = (float) slider.getValue();
                break;
            case 4:
                this.rotation = (float) slider.getValue();
                break;
            default:
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 5) {
            ConfigSettings.breastSize = this.breastSize;
            ConfigSettings.separation = this.separation;
            ConfigSettings.depth = this.depth;
            ConfigSettings.height = this.height;
            ConfigSettings.rotation = this.rotation;
            ConfigSettings.gender = getCurrentGender();
            ConfigSettings.saveConfig();
            this.mc.displayGuiScreen(null);
        } else if (button.id == 10) {
            genderIndex = (genderIndex + 1) % genders.length;
            String newGender = getCurrentGender();
            genderButton.displayString = getGenderDisplayString();
            updateSlidersState();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int guiWidth = 248;
        int guiHeight = 166;
        int centerX = (this.width - guiWidth) / 2;
        int centerY = (int) ((this.height - guiHeight) / 2);

        drawModalRectWithCustomSizedTexture(centerX, centerY, 0, 0, guiWidth, guiHeight, guiWidth, guiHeight);

        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, centerY + 5, 0xFFFFFF);

        int genderTextColor = getGenderTextColor();
        mc.fontRendererObj.drawString(genderButton.displayString, genderButton.xPosition + genderButton.width / 2 - mc.fontRendererObj.getStringWidth(genderButton.displayString) / 2,
                genderButton.yPosition + (genderButton.height - 8) / 2, genderTextColor);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static class CustomButton extends GuiButton {
        public CustomButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                FontRenderer fontrenderer = mc.fontRendererObj;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int color = this.hovered ? 0x77000000 : 0x55000000;
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
}
