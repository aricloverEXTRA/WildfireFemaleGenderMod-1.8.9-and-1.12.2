package com.wildfire.gui.screen;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.WildfireButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.opengl.GL11;

public class GuiBreastCustomization extends GuiScreen implements GuiSlider.ISlider {
    private static final ResourceLocation FEMALE_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization.png");
    private static final ResourceLocation OTHER_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization_other.png");
    private static final ResourceLocation CUSTOMIZATION_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_customization_tab.png");
    private static final ResourceLocation BREAST_PHYSICS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_physics_tab.png");
    private static final ResourceLocation MISCELLANEOUS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/miscellaneous_tab.png");

    private GuiSlider breastSlider, separationSlider, depthSlider, heightSlider, rotationSlider, intensitySlider, momentumSlider;
    private WildfireButton physicsButton, dualPhysicsButton, soundButton;
    private int selectedTab = 0;
    private GenderConfig.PlayerGenderSettings settings;

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.settings = GenderConfig.getPlayerSettings(mc.thePlayer);

        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        int sliderX = guiLeft + 24;
        int sliderY = guiTop + 50;
        int sliderWidth = 200;
        int sliderHeight = 20;
        int spacing = 25;

        breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, settings.breastSize, false, true, this);
        separationSlider = new GuiSlider(1, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, settings.separation, false, true, this);
        depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, settings.depth, false, true, this);
        heightSlider = new GuiSlider(3, sliderX, sliderY + spacing * 3, sliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, settings.height, false, true, this);
        rotationSlider = new GuiSlider(4, sliderX, sliderY + spacing * 4, sliderWidth, sliderHeight, "Rotation: ", "", 0.0D, 10.0D, settings.rotation, false, true, this);

        physicsButton = new WildfireButton(5, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Physics: " + (settings.physicsEnabled ? "§aEnabled" : "§cDisabled"));
        dualPhysicsButton = new WildfireButton(6, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Dual-Physics: " + (settings.breastsUniboob ? "No" : "Yes"));
        intensitySlider = new GuiSlider(7, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, "Intensity: ", "%", 0.0D, 150.0D, settings.intensity, false, true, this);
        momentumSlider = new GuiSlider(8, sliderX, sliderY + spacing * 3, sliderWidth, sliderHeight, "Momentum: ", "%", 25.0D, 100.0D, settings.momentum, false, true, this);

        soundButton = new WildfireButton(9, sliderX, sliderY, sliderWidth, sliderHeight, "Hurt Sounds: " + (settings.hurtSoundsEnabled ? "ON" : "OFF"));

        this.buttonList.add(new WildfireButton(11, guiLeft + 6, guiTop + 6, 84, 12, "Customization"));
        this.buttonList.add(new WildfireButton(12, guiLeft + 94, guiTop + 6, 84, 12, "Breast Physics"));
        this.buttonList.add(new WildfireButton(13, guiLeft + 182, guiTop + 6, 84, 12, "Miscellaneous"));
        this.buttonList.add(new WildfireButton(14, guiLeft + (guiWidth - 75) / 2 + 15, guiTop + guiHeight - 30, 75, 10, "Back"));

        updateSlidersState();
    }

    private void updateSlidersState() {
        boolean slidersEnabled = !"Male".equals(settings.gender);
        breastSlider.enabled = slidersEnabled && selectedTab == 0;
        separationSlider.enabled = slidersEnabled && selectedTab == 0;
        depthSlider.enabled = slidersEnabled && selectedTab == 0;
        heightSlider.enabled = slidersEnabled && selectedTab == 0;
        rotationSlider.enabled = slidersEnabled && selectedTab == 0;
        physicsButton.enabled = slidersEnabled && selectedTab == 1;
        dualPhysicsButton.enabled = slidersEnabled && selectedTab == 1 && settings.physicsEnabled;
        intensitySlider.enabled = slidersEnabled && selectedTab == 1 && settings.physicsEnabled;
        momentumSlider.enabled = slidersEnabled && selectedTab == 1 && settings.physicsEnabled;
        soundButton.enabled = slidersEnabled && selectedTab == 2;

        this.buttonList.removeIf(button -> button.id >= 0 && button.id <= 9);
        if (selectedTab == 0) {
            this.buttonList.add(breastSlider);
            this.buttonList.add(separationSlider);
            this.buttonList.add(depthSlider);
            this.buttonList.add(heightSlider);
            this.buttonList.add(rotationSlider);
        } else if (selectedTab == 1) {
            this.buttonList.add(physicsButton);
            this.buttonList.add(dualPhysicsButton);
            this.buttonList.add(intensitySlider);
            this.buttonList.add(momentumSlider);
        } else if (selectedTab == 2) {
            this.buttonList.add(soundButton);
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        switch (slider.id) {
            case 0: settings.breastSize = (float) slider.getValue(); break;
            case 1: settings.separation = (float) slider.getValue(); break;
            case 2: settings.depth = (float) slider.getValue(); break;
            case 3: settings.height = (float) slider.getValue(); break;
            case 4: settings.rotation = (float) slider.getValue(); break;
            case 7: settings.intensity = (float) slider.getValue(); break;
            case 8: settings.momentum = (float) slider.getValue(); break;
        }
        GenderConfig.saveConfig();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 5:
                settings.physicsEnabled = !settings.physicsEnabled;
                physicsButton.displayString = "Breast Physics: " + (settings.physicsEnabled ? "§aEnabled" : "§cDisabled");
                updateSlidersState();
                GenderConfig.saveConfig();
                break;
            case 6:
                settings.breastsUniboob = !settings.breastsUniboob;
                dualPhysicsButton.displayString = "Dual-Physics: " + (settings.breastsUniboob ? "No" : "Yes");
                GenderConfig.saveConfig();
                break;
            case 9:
                settings.hurtSoundsEnabled = !settings.hurtSoundsEnabled;
                soundButton.displayString = "Hurt Sounds: " + (settings.hurtSoundsEnabled ? "ON" : "OFF");
                GenderConfig.saveConfig();
                break;
            case 11:
                selectedTab = 0;
                updateSlidersState();
                break;
            case 12:
                selectedTab = 1;
                updateSlidersState();
                break;
            case 13:
                selectedTab = 2;
                updateSlidersState();
                break;
            case 14:
                GenderConfig.saveConfig();
                this.mc.displayGuiScreen(new GuiWardrobe());
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = "Other".equals(settings.gender) ? OTHER_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE;
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);

        ResourceLocation tabTexture;
        switch (selectedTab) {
            case 1: tabTexture = BREAST_PHYSICS_TAB_TEXTURE; break;
            case 2: tabTexture = MISCELLANEOUS_TAB_TEXTURE; break;
            default: tabTexture = CUSTOMIZATION_TAB_TEXTURE; break;
        }
        mc.getTextureManager().bindTexture(tabTexture);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop + 24, 0, 0, guiWidth, 242, guiWidth, 242);

        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, guiTop - 15, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}