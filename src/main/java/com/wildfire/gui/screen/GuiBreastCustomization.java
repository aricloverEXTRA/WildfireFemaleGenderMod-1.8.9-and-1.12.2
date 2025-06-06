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

    private GuiSlider breastSlider, separationSlider, depthSlider, heightSlider, rotationSlider, cleavageSlider, stiffnessSlider, dampingSlider;
    private WildfireButton uniboobButton;
    private int selectedTab = 0;
    private GenderConfig.PlayerGenderSettings settings;

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.settings = GenderConfig.getPlayerSettings(mc.thePlayer);

        int guiWidth = 512;
        int guiHeight = 512;
        int contentWidth = 272;
        int contentHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        int contentLeft = guiLeft + (guiWidth - contentWidth) / 2;
        int contentTop = guiTop + (guiHeight - contentHeight) / 2;

        int sliderX = contentLeft + 24;
        int sliderY = contentTop + 50;
        int sliderWidth = 200;
        int sliderHeight = 20;
        int spacing = 25;

        breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight, "Breast Size: ", "%", 0.0D, 100.0D, settings.breastSize, false, true, this);
        separationSlider = new GuiSlider(1, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Separation: ", "", -10.0D, 10.0D, settings.separation, false, true, this);
        depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2, sliderWidth, sliderHeight, "Depth: ", "", -10.0D, 0.0D, settings.depth, false, true, this);
        heightSlider = new GuiSlider(3, sliderX, sliderY + spacing * 3, sliderWidth, sliderHeight, "Height: ", "", -10.0D, 10.0D, settings.height, false, true, this);
        rotationSlider = new GuiSlider(4, sliderX, sliderY + spacing * 4, sliderWidth, sliderHeight, "Rotation: ", "", 0.0D, 10.0D, settings.rotation, false, true, this);
        cleavageSlider = new GuiSlider(5, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Cleavage: ", "", 0.0D, 10.0D, settings.breastsCleavage, false, true, this);

        stiffnessSlider = new GuiSlider(6, sliderX, sliderY, sliderWidth, sliderHeight, "Stiffness: ", "", 0.05D, 0.5D, settings.stiffness, false, true, this);
        dampingSlider = new GuiSlider(7, sliderX, sliderY + spacing, sliderWidth, sliderHeight, "Damping: ", "", 0.5D, 0.95D, settings.damping, false, true, this);

        uniboobButton = new WildfireButton(8, sliderX, sliderY, sliderWidth, sliderHeight, "Uniboob: " + (settings.breastsUniboob ? "ON" : "OFF"));

        this.buttonList.add(new WildfireButton(11, contentLeft + 6, contentTop + 6, 90, 20, "Customization"));
        this.buttonList.add(new WildfireButton(12, contentLeft + 96, contentTop + 6, 90, 20, "Breast Physics"));
        this.buttonList.add(new WildfireButton(13, contentLeft + 186, contentTop + 6, 90, 20, "Miscellaneous"));

        this.buttonList.add(new WildfireButton(14, contentLeft + (contentWidth - 75) / 2, contentTop + contentHeight - 20, 75, 15, "Back"));

        updateSlidersState();
    }

    private void updateSlidersState() {
        boolean slidersEnabled = !"Male".equals(settings.gender);
        breastSlider.enabled = slidersEnabled && selectedTab == 0;
        separationSlider.enabled = slidersEnabled && selectedTab == 0;
        depthSlider.enabled = slidersEnabled && selectedTab == 0;
        heightSlider.enabled = slidersEnabled && selectedTab == 0;
        rotationSlider.enabled = slidersEnabled && selectedTab == 0;
        cleavageSlider.enabled = slidersEnabled && selectedTab == 0;
        stiffnessSlider.enabled = slidersEnabled && selectedTab == 1;
        dampingSlider.enabled = slidersEnabled && selectedTab == 1;
        uniboobButton.enabled = slidersEnabled && selectedTab == 2;

        this.buttonList.removeIf(button -> button.id >= 0 && button.id <= 8);
        if (selectedTab == 0) {
            this.buttonList.add(breastSlider);
            this.buttonList.add(separationSlider);
            this.buttonList.add(depthSlider);
            this.buttonList.add(heightSlider);
            this.buttonList.add(rotationSlider);
            this.buttonList.add(cleavageSlider);
        } else if (selectedTab == 1) {
            this.buttonList.add(stiffnessSlider);
            this.buttonList.add(dampingSlider);
        } else if (selectedTab == 2) {
            this.buttonList.add(uniboobButton);
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
            case 5: settings.breastsCleavage = (float) slider.getValue(); break;
            case 6: settings.stiffness = (float) slider.getValue(); break;
            case 7: settings.damping = (float) slider.getValue(); break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 8:
                settings.breastsUniboob = !settings.breastsUniboob;
                uniboobButton.displayString = "Uniboob: " + (settings.breastsUniboob ? "ON" : "OFF");
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
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ResourceLocation background = "Other".equals(settings.gender) ? OTHER_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE;
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 512;
        int guiHeight = 512;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        this.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);

        int contentWidth = 272;
        int contentHeight = 130;
        int contentLeft = guiLeft + (guiWidth - contentWidth) / 2;
        int contentTop = guiTop + (guiHeight - contentHeight) / 2;

        ResourceLocation tabTexture;
        switch (selectedTab) {
            case 1: tabTexture = BREAST_PHYSICS_TAB_TEXTURE; break;
            case 2: tabTexture = MISCELLANEOUS_TAB_TEXTURE; break;
            default: tabTexture = CUSTOMIZATION_TAB_TEXTURE; break;
        }
        mc.getTextureManager().bindTexture(tabTexture);
        this.drawModalRectWithCustomSizedTexture(contentLeft, contentTop + 18, 0, 0, 272, 32, 272, 32);

        this.drawCenteredString(this.fontRendererObj, "Breast Customization", this.width / 2, guiTop - 15, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}