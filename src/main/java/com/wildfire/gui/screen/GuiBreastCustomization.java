package com.wildfire.gui.screen;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.screen.GuiBreastUVEditor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import java.util.Objects;

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
    private WildfireButton physicsButton, dualPhysicsButton, armorPhysicsButton, soundButton;
    private WildfireButton hideInArmorButton, showArmorTooltipButton, holidayThemesButton;
    private WildfireButton breastTextureEditorButton;
    private int selectedTab = 0;
    private GenderConfig.PlayerGenderSettings settings;
    private boolean isDarkMode = false;

    private static String prefixFromKey(String key) {
        String localized = I18n.format(key);
        localized = localized.replace("%s", "");
        localized = localized.replace("%%", "%");
        if (!localized.endsWith(" ")) localized = localized + " ";
        return localized;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.settings = GenderConfig.getPlayerSettings(mc.player);
        this.isDarkMode = GenderConfig.getDarkMode(mc.player);

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

        breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.wardrobe.slider.breast_size", ""), "%", 0.0D, 100.0D, settings.breastSize, false, true, this);

        separationSlider = new GuiSlider(1, sliderX, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.wardrobe.slider.separation", ""), "", -10.0D, 10.0D, settings.separation, false, true, this);

        heightSlider = new GuiSlider(3, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.wardrobe.slider.height", ""), "", -10.0D, 10.0D, settings.height, false, true, this);

        depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.wardrobe.slider.depth", ""), "", -10.0D, 0.0D, settings.depth, false, true, this);

        rotationSlider = new GuiSlider(4, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.wardrobe.slider.rotation", ""), "°", 0.0D, 10.0D, settings.rotation, false, true, this);

        intensitySlider = new GuiSlider(7, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.slider.bounce", ""), "%", 0.0D, 150.0D, settings.intensity, false, true, this);

        momentumSlider = new GuiSlider(8, sliderX + smallSliderWidth + horizontalSpacing, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.slider.floppy", ""), "%", 25.0D, 100.0D, settings.momentum, false, true, this);

        voicePitchSlider = new GuiSlider(10, sliderX, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                I18n.format("wildfire_gender.slider.voice_pitch", ""), "%", 80.0D, 120.0D, settings.voicePitch, false, true, this);

        String enabledText = "\u00A7a" + I18n.format("wildfire_gender.label.enabled");
        String disabledText = "\u00A7c" + I18n.format("wildfire_gender.label.disabled");

        physicsButton = new WildfireButton(5, sliderX, sliderY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.char_settings.physics", settings.physicsEnabled ? enabledText : disabledText));

        dualPhysicsButton = new WildfireButton(6, sliderX, sliderY + spacing - 1, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.breast_customization.dual_physics", settings.breastsUniboob ? I18n.format("wildfire_gender.label.no") : I18n.format("wildfire_gender.label.yes")));

        int armorY = sliderY + spacing - 1 + 28 + 14 - 5 + 11;
        armorPhysicsButton = new WildfireButton(14, sliderX, armorY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.char_settings.override_armor_physics", GenderConfig.getOverrideArmorPhysics(mc.player) ? enabledText : disabledText));

        soundButton = new WildfireButton(9, sliderX, sliderY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.char_settings.hurt_sounds", settings.hurtSoundsEnabled ? enabledText : disabledText));

        int miscBase = sliderY + spacing - 1 + 28;
        int hideInArmorY = miscBase - 4;
        int showArmorTooltipY = hideInArmorY + 28 + 4 - 2 - 6;
        int holidayThemesY = showArmorTooltipY + 4 + 20;

        hideInArmorButton = new WildfireButton(15, sliderX, hideInArmorY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.char_settings.hide_in_armor", GenderConfig.getShowArmorTooltip(mc.player) ? enabledText : disabledText));

        showArmorTooltipButton = new WildfireButton(16, sliderX, showArmorTooltipY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.char_settings.show_armor_stat", GenderConfig.getShowArmorTooltip(mc.player) ? enabledText : disabledText));

        holidayThemesButton = new WildfireButton(17, sliderX, holidayThemesY, sliderWidth, sliderHeight,
                I18n.format("wildfire_gender.misc.holiday_themes", GenderConfig.getHolidayThemes(mc.player) ? enabledText : disabledText));

        int editorY = sliderY + spacing * 2 + 20 + 8;
        breastTextureEditorButton = new WildfireButton(18, sliderX, editorY, 130, 15, I18n.format("wildfire_gender.uv_editor"));

        WildfireButton customizationTab = new WildfireButton(11, guiLeft + 6, guiTop + 6, 84, 12, I18n.format("wildfire_gender.breast_customization.tab_customization"));
        WildfireButton physicsTab = new WildfireButton(12, guiLeft + 94, guiTop + 6, 84, 12, I18n.format("wildfire_gender.breast_customization.tab_physics"));
        WildfireButton miscTab = new WildfireButton(13, guiLeft + 182, guiTop + 6, 84, 12, I18n.format("wildfire_gender.breast_customization.tab_miscellaneous"));

        this.buttonList.add(customizationTab);
        this.buttonList.add(physicsTab);
        this.buttonList.add(miscTab);

        this.buttonList.add(breastSlider);
        this.buttonList.add(separationSlider);
        this.buttonList.add(depthSlider);
        this.buttonList.add(heightSlider);
        this.buttonList.add(rotationSlider);

        this.buttonList.add(physicsButton);
        this.buttonList.add(dualPhysicsButton);
        this.buttonList.add(armorPhysicsButton);
        this.buttonList.add(intensitySlider);
        this.buttonList.add(momentumSlider);

        this.buttonList.add(soundButton);
        this.buttonList.add(hideInArmorButton);
        this.buttonList.add(showArmorTooltipButton);
        this.buttonList.add(holidayThemesButton);

        this.buttonList.add(breastTextureEditorButton);

        this.buttonList.add(voicePitchSlider);

        updateSlidersState();
    }

    private void updateSlidersState() {
        boolean slidersEnabled = !"Male".equals(settings.gender);

        breastSlider.visible = slidersEnabled && selectedTab == 0;
        breastSlider.enabled = breastSlider.visible;

        separationSlider.visible = slidersEnabled && selectedTab == 0;
        separationSlider.enabled = separationSlider.visible;

        depthSlider.visible = slidersEnabled && selectedTab == 0;
        depthSlider.enabled = depthSlider.visible;

        heightSlider.visible = slidersEnabled && selectedTab == 0;
        heightSlider.enabled = heightSlider.visible;

        rotationSlider.visible = slidersEnabled && selectedTab == 0;
        rotationSlider.enabled = rotationSlider.visible;

        breastTextureEditorButton.visible = slidersEnabled && selectedTab == 0;
        breastTextureEditorButton.enabled = breastTextureEditorButton.visible;

        physicsButton.visible = slidersEnabled && selectedTab == 1;
        physicsButton.enabled = physicsButton.visible;

        dualPhysicsButton.visible = slidersEnabled && selectedTab == 1;
        dualPhysicsButton.enabled = dualPhysicsButton.visible && settings.physicsEnabled;

        armorPhysicsButton.visible = slidersEnabled && selectedTab == 1;
        armorPhysicsButton.enabled = armorPhysicsButton.visible;

        intensitySlider.visible = slidersEnabled && selectedTab == 1;
        intensitySlider.enabled = intensitySlider.visible && settings.physicsEnabled;

        momentumSlider.visible = slidersEnabled && selectedTab == 1;
        momentumSlider.enabled = momentumSlider.visible && settings.physicsEnabled;

        soundButton.visible = slidersEnabled && selectedTab == 2;
        soundButton.enabled = soundButton.visible;

        hideInArmorButton.visible = slidersEnabled && selectedTab == 2;
        hideInArmorButton.enabled = hideInArmorButton.visible;

        showArmorTooltipButton.visible = slidersEnabled && selectedTab == 2;
        showArmorTooltipButton.enabled = showArmorTooltipButton.visible;

        holidayThemesButton.visible = slidersEnabled && selectedTab == 2;
        holidayThemesButton.enabled = holidayThemesButton.visible;

        voicePitchSlider.visible = slidersEnabled && selectedTab == 2;
        voicePitchSlider.enabled = voicePitchSlider.visible && settings.hurtSoundsEnabled;

        for (GuiButton button : this.buttonList) {
            if (button.id == 11) button.enabled = selectedTab != 0;
            if (button.id == 12) button.enabled = selectedTab != 1;
            if (button.id == 13) button.enabled = selectedTab != 2;
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
                if (settings.hurtSoundsEnabled) {
                    settings.voicePitch = (float) slider.getValue();
                }
                break;
        }
        GenderConfig.saveConfig();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 5:
                settings.physicsEnabled = !settings.physicsEnabled;
                physicsButton.displayString = I18n.format("wildfire_gender.char_settings.physics",
                        settings.physicsEnabled ? I18n.format("wildfire_gender.label.enabled") : I18n.format("wildfire_gender.label.disabled"));
                GenderConfig.saveConfig();
                updateSlidersState();
                break;
            case 6:
                settings.breastsUniboob = !settings.breastsUniboob;
                dualPhysicsButton.displayString = I18n.format("wildfire_gender.breast_customization.dual_physics",
                        settings.breastsUniboob ? I18n.format("wildfire_gender.label.no") : I18n.format("wildfire_gender.label.yes"));
                GenderConfig.saveConfig();
                break;
            case 9:
                settings.hurtSoundsEnabled = !settings.hurtSoundsEnabled;
                String enabled = "\u00A7a" + I18n.format("wildfire_gender.label.enabled");
                String disabled = "\u00A7c" + I18n.format("wildfire_gender.label.disabled");
                soundButton.displayString = I18n.format("wildfire_gender.char_settings.hurt_sounds",
                        settings.hurtSoundsEnabled ? enabled : disabled);
                GenderConfig.saveConfig();
                updateSlidersState();
                break;
            case 14:
                boolean newArmorPhysics = !GenderConfig.getOverrideArmorPhysics(mc.player);
                GenderConfig.setOverrideArmorPhysics(mc.player, newArmorPhysics);
                armorPhysicsButton.displayString = I18n.format("wildfire_gender.char_settings.override_armor_physics",
                        newArmorPhysics ? ("\u00A7a" + I18n.format("wildfire_gender.label.enabled")) : ("\u00A7c" + I18n.format("wildfire_gender.label.disabled")));
                break;
            case 15:
                boolean newHide = !GenderConfig.getHideInArmor(mc.player);
                GenderConfig.setHideInArmor(mc.player, newHide);
                hideInArmorButton.displayString = I18n.format("wildfire_gender.char_settings.hide_in_armor",
                        newHide ? ("\u00A7a" + I18n.format("wildfire_gender.label.enabled")) : ("\u00A7c" + I18n.format("wildfire_gender.label.disabled")));
                break;
            case 16:
                boolean newTooltip = !GenderConfig.getShowArmorTooltip(mc.player);
                GenderConfig.setShowArmorTooltip(mc.player, newTooltip);
                showArmorTooltipButton.displayString = I18n.format("wildfire_gender.char_settings.show_armor_stat",
                        newTooltip ? ("\u00A7a" + I18n.format("wildfire_gender.label.enabled")) : ("\u00A7c" + I18n.format("wildfire_gender.label.disabled")));
                break;
            case 17:
                boolean newHoliday = !GenderConfig.getHolidayThemes(mc.player);
                GenderConfig.setHolidayThemes(mc.player, newHoliday);
                holidayThemesButton.displayString = I18n.format("wildfire_gender.misc.holiday_themes",
                        newHoliday ? ("\u00A7a" + I18n.format("wildfire_gender.label.enabled")) : ("\u00A7c" + I18n.format("wildfire_gender.label.disabled")));
                break;
            case 18:
                if (mc.player != null) {
                    mc.displayGuiScreen(new GuiBreastUVEditor(this, Objects.requireNonNull(mc.player).getUniqueID()));
                } else {
                    mc.displayGuiScreen(new GuiBreastUVEditor(this, null));
                }
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
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            GenderConfig.saveConfig();
            this.mc.displayGuiScreen(new GuiWardrobe()); // ESC -> Wardrobe
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = "Other".equals(settings.gender) ? (isDarkMode ? DARK_OTHER_BACKGROUND_TEXTURE : OTHER_BACKGROUND_TEXTURE) : (isDarkMode ? DARK_FEMALE_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE);
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        drawModalRectWithCustomSizedTexture(guiLeft + 0, guiTop + 0, 0, 0, guiWidth, guiHeight, 512, 512);

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
                    wb.drawButton(mc, mouseX, mouseY, partialTicks);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }

        int previewCenterX = guiLeft + 44;
        int previewCenterY = guiTop + 6 + 44;
        GuiUtils.drawEntityOnScreenNoScissor(this, previewCenterX, previewCenterY, 48, mouseX - previewCenterX, mouseY - previewCenterY, this.mc.player);

        this.drawCenteredString(this.fontRenderer, I18n.format("wildfire_gender.appearance_settings.title"), this.width / 2, guiTop - 15, 0xFFFFFF);

        if (armorPhysicsButton != null && armorPhysicsButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(I18n.format("wildfire_gender.tooltip.override_armor_physics.line1")), mouseX, mouseY, fontRenderer);
        }
        if (soundButton != null && soundButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(I18n.format("wildfire_gender.tooltip.hurt_sounds")), mouseX, mouseY, fontRenderer);
        }
        if (holidayThemesButton != null && holidayThemesButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(I18n.format("wildfire_gender.tooltip.holiday_themes.line1")), mouseX, mouseY, fontRenderer);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}