package com.wildfire.gui.screen;

import com.wildfire.main.WildfireSounds;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.WildfireButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiBreastCustomization extends GuiScreen implements GuiSlider.ISlider {
    private static final ResourceLocation FEMALE_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization.png");
    private static final ResourceLocation OTHER_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/breast_customization_other.png");
    private static final ResourceLocation CUSTOMIZATION_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_customization_tab.png");
    private static final ResourceLocation BREAST_PHYSICS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/breast_physics_tab.png");
    private static final ResourceLocation MISCELLANEOUS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/gui/tabs/miscellaneous_tab.png");
    private static final ResourceLocation DARK_FEMALE_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/breast_customization.png");
    private static final ResourceLocation DARK_OTHER_BACKGROUND_TEXTURE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/breast_customization_other.png");
    private static final ResourceLocation DARK_CUSTOMIZATION_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/tabs/breast_customization_tab.png");
    private static final ResourceLocation DARK_BREAST_PHYSICS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/tabs/breast_physics_tab.png");
    private static final ResourceLocation DARK_MISCELLANEOUS_TAB_TEXTURE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/tabs/miscellaneous_tab.png");

    private GuiSlider breastSlider, separationSlider, depthSlider, heightSlider, rotationSlider, intensitySlider, momentumSlider, voicePitchSlider;
    private WildfireButton physicsButton, dualPhysicsButton, soundButton;
    private int selectedTab = 0;
    private GenderConfig.PlayerGenderSettings settings;
    private float lastPitchValue = -1.0F;
    private boolean isDarkMode = false;

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.settings = GenderConfig.getPlayerSettings(mc.thePlayer);
        this.isDarkMode = GenderConfig.getDarkMode(mc.thePlayer);

        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        int tabContentLeft = guiLeft + 94;
        int tabContentTop = guiTop + 26;

        int sliderX = tabContentLeft + 6;
        int sliderY = tabContentTop + 6;
        int sliderWidth = 166;
        int sliderHeight = 20;
        int smallSliderWidth = 81;
        int spacing = 25;
        int horizontalSpacing = 4;

        // Breast Size Slider
        breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, settings.breastSize, false, true, this);

        // Separation and Height Sliders
        separationSlider = new GuiSlider(1, sliderX, sliderY + spacing - 1, smallSliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, settings.separation, false, true, this);
        heightSlider = new GuiSlider(3, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing - 1, smallSliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, settings.height, false, true, this);

        // Depth and Rotation Sliders
        depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, settings.depth, false, true, this);
        rotationSlider = new GuiSlider(4, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight, "Rotation: ", "°", 0.0D, 10.0D, settings.rotation, false, true, this);

        // Physics Tab Elements
        physicsButton = new WildfireButton(5, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Physics: " + (settings.physicsEnabled ? "§aEnabled" : "§cDisabled"));
        dualPhysicsButton = new WildfireButton(6, sliderX, sliderY + spacing - 1, sliderWidth, sliderHeight, "Dual-Physics: " + (settings.breastsUniboob ? "No" : "Yes"));
        intensitySlider = new GuiSlider(7, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight, "Intensity: ", "%", 0.0D, 150.0D, settings.intensity, false, true, this);
        momentumSlider = new GuiSlider(8, sliderX + smallSliderWidth + horizontalSpacing, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight, "Momentum: ", "%", 25.0D, 100.0D, settings.momentum, false, true, this);

        // Miscellaneous Tab Elements
        soundButton = new WildfireButton(9, sliderX, sliderY, sliderWidth, sliderHeight, "Female Hurt Sounds: " + (settings.hurtSoundsEnabled ? "§aEnabled" : "§cDisabled"));
        voicePitchSlider = new GuiSlider(10, sliderX, sliderY + spacing + 8, smallSliderWidth, sliderHeight, "Pitch: ", "%", 80.0D, 120.0D, settings.voicePitch, false, true, this);

        // Tab Buttons
        WildfireButton customizationTab = new WildfireButton(11, guiLeft + 6, guiTop + 6, 84, 12, "Customization");
        WildfireButton physicsTab = new WildfireButton(12, guiLeft + 94, guiTop + 6, 84, 12, "Breast Physics");
        WildfireButton miscTab = new WildfireButton(13, guiLeft + 182, guiTop + 6, 84, 12, "Miscellaneous");
        this.buttonList.add(customizationTab);
        this.buttonList.add(physicsTab);
        this.buttonList.add(miscTab);

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
        voicePitchSlider.enabled = slidersEnabled && selectedTab == 2;

        for (GuiButton button : this.buttonList) {
            if (button.id == 11) button.enabled = selectedTab != 0;
            if (button.id == 12) button.enabled = selectedTab != 1;
            if (button.id == 13) button.enabled = selectedTab != 2;
        }

        this.buttonList.removeIf(button -> button.id >= 0 && button.id <= 10);
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
            this.buttonList.add(voicePitchSlider);
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
            case 10: 
                settings.voicePitch = (float) slider.getValue();
                break;
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
                soundButton.displayString = "Female Hurt Sounds: " + (settings.hurtSoundsEnabled ? "§aEnabled" : "§cDisabled");
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
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            GenderConfig.saveConfig();
            this.mc.displayGuiScreen(new GuiWardrobe());
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = "Other".equals(settings.gender) ? (isDarkMode ? DARK_OTHER_BACKGROUND_TEXTURE : OTHER_BACKGROUND_TEXTURE) : 
                                      (isDarkMode ? DARK_FEMALE_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE);
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);

        ResourceLocation tabTexture;
        switch (selectedTab) {
            case 1: tabTexture = isDarkMode ? DARK_BREAST_PHYSICS_TAB_TEXTURE : BREAST_PHYSICS_TAB_TEXTURE; break;
            case 2: tabTexture = isDarkMode ? DARK_MISCELLANEOUS_TAB_TEXTURE : MISCELLANEOUS_TAB_TEXTURE; break;
            default: tabTexture = isDarkMode ? DARK_CUSTOMIZATION_TAB_TEXTURE : CUSTOMIZATION_TAB_TEXTURE; break;
        }
        mc.getTextureManager().bindTexture(tabTexture);
        drawModalRectWithCustomSizedTexture(guiLeft + 94, guiTop + 26, 0, 0, guiWidth, guiHeight, 512, 512);

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (GuiButton button : buttonList) {
            if (button instanceof WildfireButton) {
                WildfireButton wb = (WildfireButton) button;
                if (!wb.enabled && (wb.id == 11 || wb.id == 12 || wb.id == 13)) {
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
                    wb.drawButton(mc, mouseX, mouseY);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }

        this.drawCenteredString(this.fontRendererObj, "Character Personalization", this.width / 2, guiTop - 15, 0xFFFFFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}