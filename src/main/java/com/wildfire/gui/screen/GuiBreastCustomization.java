package com.wildfire.gui.screen;

import com.wildfire.main.config.ConfigSettings;
import com.wildfire.gui.WildfireButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiBreastCustomization extends GuiScreen implements ISlider {
    private static final ResourceLocation FEMALE_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization.png");
    private static final ResourceLocation OTHER_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization_other.png");
    private static final ResourceLocation CUSTOMIZATION_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_customization_tab.png");
    private static final ResourceLocation BREAST_PHYSICS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_physics_tab.png");
    private static final ResourceLocation MISCELLANEOUS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/miscellaneous_tab.png");

    private float breastSize = ConfigSettings.breastSize;
    private float separation = ConfigSettings.separation;
    private float depth = ConfigSettings.depth;
    private float height = ConfigSettings.height;
    private float rotation = ConfigSettings.rotation;

    private String[] genders = {"Male", "Female", "Other"};
    private GuiSlider breastSlider, separationSlider, depthSlider, heightSlider, rotationSlider;
    private int genderIndex;
    private int selectedTab = 0; // 0: Customization, 1: Breast Physics, 2: Miscellaneous

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

        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (int) ((this.height - guiHeight) / -2);

        int sliderX = guiLeft + 24;
        int sliderY = guiTop + 50;
        int sliderWidth = 200;
        int sliderHeight = 20;
        int spacing = 25;

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

        // Adjusted Back button
        this.buttonList.add(new WildfireButton(5, guiLeft + (guiWidth - 75) / 2 + 15, guiTop + guiHeight - 30, 75, 10, "Back"));

        // Adjusted Tab buttons
        this.buttonList.add(new WildfireButton(11, guiLeft + 6, guiTop + 6, 84, 12, "Customization"));
        this.buttonList.add(new WildfireButton(12, guiLeft + 94, guiTop + 6, 84, 12, "Breast Physics"));
        this.buttonList.add(new WildfireButton(13, guiLeft + 182, guiTop + 6, 84, 12, "Miscellaneous"));

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
        switch (button.id) {
            case 5:
                ConfigSettings.breastSize = this.breastSize;
                ConfigSettings.separation = this.separation;
                ConfigSettings.depth = this.depth;
                ConfigSettings.height = this.height;
                ConfigSettings.rotation = this.rotation;
                ConfigSettings.saveConfig();
                this.mc.displayGuiScreen(new GuiWardrobe());
                break;
            case 11:
                selectedTab = 0;
                break;
            case 12:
                selectedTab = 1;
                break;
            case 13:
                selectedTab = 2;
                break;
            default:
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ResourceLocation background = "Other".equals(getCurrentGender()) ? OTHER_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE;
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (int) ((this.height - guiHeight) / -2);

        drawDefaultBackground();
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);
        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, guiTop - 15, 0xFFFFFF);

        ResourceLocation tabTexture;
        switch (selectedTab) {
            case 1:
                tabTexture = BREAST_PHYSICS_TAB_TEXTURE;
                break;
            case 2:
                tabTexture = MISCELLANEOUS_TAB_TEXTURE;
                break;
            default:
                tabTexture = CUSTOMIZATION_TAB_TEXTURE;
                break;
        }
        mc.getTextureManager().bindTexture(tabTexture);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop + 24, 0, 0, guiWidth, 242, guiWidth, 242);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
