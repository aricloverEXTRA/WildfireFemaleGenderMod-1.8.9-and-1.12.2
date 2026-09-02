package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

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

    private GuiSlider breastSlider;
    private GuiSlider separationSlider;
    private GuiSlider depthSlider;
    private GuiSlider heightSlider;
    private GuiSlider rotationSlider;
    private GuiSlider intensitySlider;
    private GuiSlider momentumSlider;
    private GuiSlider voicePitchSlider;
    private WildfireButton physicsButton;
    private WildfireButton dualPhysicsButton;
    private WildfireButton armorPhysicsButton;
    private WildfireButton soundButton;
    private WildfireButton hideInArmorButton;
    private WildfireButton showArmorTooltipButton;
    private WildfireButton holidayThemesButton;
    private WildfireButton breastTextureEditorButton;

    private int selectedTab = 0;
    private GenderConfig.PlayerGenderSettings settings;
    private boolean isDarkMode = false;

    private static String prefixFromKey(String key) {
        String localized = StatCollector.translateToLocal(key);
        localized = localized.replace("%s", "");
        localized = localized.replace("%%", "%");
        if (!localized.endsWith(" ")) {
            localized = localized + " ";
        }
        return localized;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        this.settings = GenderConfig.getPlayerSettings(this.mc.thePlayer);
        this.isDarkMode = GenderConfig.getDarkMode(this.mc.thePlayer);
        if (this.settings == null) {
            this.settings = new GenderConfig.PlayerGenderSettings();
        }

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

        this.breastSlider = new GuiSlider(0, sliderX, sliderY, sliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.wardrobe.slider.breast_size"), "%", 0.0D, 100.0D, this.settings.breastSize, false, true, this);
        this.separationSlider = new GuiSlider(1, sliderX, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.wardrobe.slider.separation"), "", -10.0D, 10.0D, this.settings.breastsOffsetX, false, true, this);
        this.heightSlider = new GuiSlider(3, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.wardrobe.slider.height"), "", -10.0D, 10.0D, -this.settings.breastsOffsetY, false, true, this);
        this.depthSlider = new GuiSlider(2, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.wardrobe.slider.depth"), "", -10.0D, 10.0D, -this.settings.breastsOffsetZ, false, true, this);
        this.rotationSlider = new GuiSlider(4, sliderX + smallSliderWidth + 3 + 1, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.wardrobe.slider.rotation"), "°", 0.0D, 10.0D, this.settings.breastsCleavage * 10f, false, true, this);
        this.intensitySlider = new GuiSlider(7, sliderX, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.slider.bounce"), "%", 0.0D, 150.0D, this.settings.intensity, false, true, this);
        this.momentumSlider = new GuiSlider(8, sliderX + smallSliderWidth + horizontalSpacing, sliderY + spacing * 2 - 2, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.slider.floppy"), "%", 25.0D, 100.0D, this.settings.momentum, false, true, this);
        this.voicePitchSlider = new GuiSlider(10, sliderX, sliderY + spacing - 1, smallSliderWidth, sliderHeight,
                prefixFromKey("wildfire_gender.slider.voice_pitch"), "%", 80.0D, 120.0D, this.settings.voicePitch, false, true, this);

        String enabledText = "§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled");
        String disabledText = "§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled");

        this.physicsButton = new WildfireButton(5, sliderX, sliderY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.char_settings.physics"),
                this.settings.physicsEnabled ? enabledText : disabledText
        ));
        this.dualPhysicsButton = new WildfireButton(6, sliderX, sliderY + spacing - 1, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.breast_customization.dual_physics"),
                this.settings.breastsUniboob ? StatCollector.translateToLocal("wildfire_gender.label.no") : StatCollector.translateToLocal("wildfire_gender.label.yes")
        ));

        int armorY = sliderY + spacing - 1 + 28 + 14 - 5 + 11;
        this.armorPhysicsButton = new WildfireButton(14, sliderX, armorY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.char_settings.override_armor_physics"),
                GenderConfig.getOverrideArmorPhysics(this.mc.thePlayer) ? enabledText : disabledText
        ));

        this.soundButton = new WildfireButton(9, sliderX, sliderY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.char_settings.hurt_sounds"),
                this.settings.hurtSoundsEnabled ? enabledText : disabledText
        ));

        int miscBase = sliderY + spacing - 1 + 28;
        int hideInArmorY = miscBase - 4;
        int showArmorTooltipY = hideInArmorY + 28 + 4 - 2 - 6;
        int holidayThemesY = showArmorTooltipY + 4 + 20;

        this.hideInArmorButton = new WildfireButton(15, sliderX, hideInArmorY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.char_settings.hide_in_armor"),
                GenderConfig.getHideInArmor(this.mc.thePlayer) ? enabledText : disabledText
        ));
        this.showArmorTooltipButton = new WildfireButton(16, sliderX, showArmorTooltipY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.char_settings.show_armor_stat"),
                GenderConfig.getShowArmorTooltip(this.mc.thePlayer) ? enabledText : disabledText
        ));
        this.holidayThemesButton = new WildfireButton(17, sliderX, holidayThemesY, sliderWidth, sliderHeight, String.format(
                StatCollector.translateToLocal("wildfire_gender.misc.holiday_themes"),
                GenderConfig.getHolidayThemes(this.mc.thePlayer) ? enabledText : disabledText
        ));

        int editorY = sliderY + spacing * 2 + 20 + 8;
        this.breastTextureEditorButton = new WildfireButton(18, sliderX, editorY, 130, 15, StatCollector.translateToLocal("wildfire_gender.uv_editor"));

        WildfireButton customizationTab = new WildfireButton(11, guiLeft + 6, guiTop + 6, 84, 12, StatCollector.translateToLocal("wildfire_gender.breast_customization.tab_customization"));
        WildfireButton physicsTab = new WildfireButton(12, guiLeft + 94, guiTop + 6, 84, 12, StatCollector.translateToLocal("wildfire_gender.breast_customization.tab_physics"));
        WildfireButton miscTab = new WildfireButton(13, guiLeft + 182, guiTop + 6, 84, 12, StatCollector.translateToLocal("wildfire_gender.breast_customization.tab_miscellaneous"));

        this.buttonList.add(customizationTab);
        this.buttonList.add(physicsTab);
        this.buttonList.add(miscTab);
        this.buttonList.add(this.breastSlider);
        this.buttonList.add(this.separationSlider);
        this.buttonList.add(this.depthSlider);
        this.buttonList.add(this.heightSlider);
        this.buttonList.add(this.rotationSlider);
        this.buttonList.add(this.physicsButton);
        this.buttonList.add(this.dualPhysicsButton);
        this.buttonList.add(this.armorPhysicsButton);
        this.buttonList.add(this.intensitySlider);
        this.buttonList.add(this.momentumSlider);
        this.buttonList.add(this.soundButton);
        this.buttonList.add(this.hideInArmorButton);
        this.buttonList.add(this.showArmorTooltipButton);
        this.buttonList.add(this.holidayThemesButton);
        this.buttonList.add(this.breastTextureEditorButton);
        this.buttonList.add(this.voicePitchSlider);

        updateSlidersState();
    }

    private void updateSlidersState() {
        boolean slidersEnabled = !"Male".equals(this.settings.gender);

        this.breastSlider.visible = slidersEnabled && this.selectedTab == 0;
        this.breastSlider.enabled = this.breastSlider.visible;
        this.separationSlider.visible = slidersEnabled && this.selectedTab == 0;
        this.separationSlider.enabled = this.separationSlider.visible;
        this.depthSlider.visible = slidersEnabled && this.selectedTab == 0;
        this.depthSlider.enabled = this.depthSlider.visible;
        this.heightSlider.visible = slidersEnabled && this.selectedTab == 0;
        this.heightSlider.enabled = this.heightSlider.visible;
        this.rotationSlider.visible = slidersEnabled && this.selectedTab == 0;
        this.rotationSlider.enabled = this.rotationSlider.visible;
        this.breastTextureEditorButton.visible = slidersEnabled && this.selectedTab == 0;
        this.breastTextureEditorButton.enabled = this.breastTextureEditorButton.visible;

        this.physicsButton.visible = slidersEnabled && this.selectedTab == 1;
        this.physicsButton.enabled = this.physicsButton.visible;
        this.dualPhysicsButton.visible = slidersEnabled && this.selectedTab == 1;
        this.dualPhysicsButton.enabled = this.dualPhysicsButton.visible && this.settings.physicsEnabled;
        this.armorPhysicsButton.visible = slidersEnabled && this.selectedTab == 1;
        this.armorPhysicsButton.enabled = this.armorPhysicsButton.visible;
        this.intensitySlider.visible = slidersEnabled && this.selectedTab == 1;
        this.intensitySlider.enabled = this.intensitySlider.visible && this.settings.physicsEnabled;
        this.momentumSlider.visible = slidersEnabled && this.selectedTab == 1;
        this.momentumSlider.enabled = this.momentumSlider.visible && this.settings.physicsEnabled;

        this.soundButton.visible = slidersEnabled && this.selectedTab == 2;
        this.soundButton.enabled = this.soundButton.visible;
        this.hideInArmorButton.visible = slidersEnabled && this.selectedTab == 2;
        this.hideInArmorButton.enabled = this.hideInArmorButton.visible;
        this.showArmorTooltipButton.visible = slidersEnabled && this.selectedTab == 2;
        this.showArmorTooltipButton.enabled = this.showArmorTooltipButton.visible;
        this.holidayThemesButton.visible = slidersEnabled && this.selectedTab == 2;
        this.holidayThemesButton.enabled = this.holidayThemesButton.visible;
        this.voicePitchSlider.visible = slidersEnabled && this.selectedTab == 2;
        this.voicePitchSlider.enabled = this.voicePitchSlider.visible && this.settings.hurtSoundsEnabled;

        for (GuiButton button : this.buttonList) {
            if (button.id == 11) {
                button.enabled = this.selectedTab != 0;
            }
            if (button.id == 12) {
                button.enabled = this.selectedTab != 1;
            }
            if (button.id == 13) {
                button.enabled = this.selectedTab != 2;
            }
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        switch (slider.id) {
            case 0:
                this.settings.breastSize = (float) slider.getValue();
                break;
            case 1:
                this.settings.breastsOffsetX = (float) slider.getValue();
                break;
            case 2:
                this.settings.breastsOffsetZ = -(float) slider.getValue();
                break;
            case 3:
                this.settings.breastsOffsetY = -(float) slider.getValue();
                break;
            case 4:
                this.settings.breastsCleavage = (float) slider.getValue() / 10f;
                break;
            case 7:
                this.settings.intensity = (float) slider.getValue();
                break;
            case 8:
                this.settings.momentum = (float) slider.getValue();
                break;
            case 10:
                if (this.settings.hurtSoundsEnabled) {
                    this.settings.voicePitch = (float) slider.getValue();
                }
                break;
        }
        GenderConfig.saveConfig();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }

        switch (button.id) {
            case 5:
                this.settings.physicsEnabled = !this.settings.physicsEnabled;
                this.physicsButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.char_settings.physics"),
                        this.settings.physicsEnabled ? StatCollector.translateToLocal("wildfire_gender.label.enabled") : StatCollector.translateToLocal("wildfire_gender.label.disabled")
                );
                GenderConfig.saveConfig();
                updateSlidersState();
                break;
            case 6:
                this.settings.breastsUniboob = !this.settings.breastsUniboob;
                this.dualPhysicsButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.breast_customization.dual_physics"),
                        this.settings.breastsUniboob ? StatCollector.translateToLocal("wildfire_gender.label.no") : StatCollector.translateToLocal("wildfire_gender.label.yes")
                );
                GenderConfig.saveConfig();
                break;
            case 9:
                this.settings.hurtSoundsEnabled = !this.settings.hurtSoundsEnabled;
                String enabled = "§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled");
                String disabled = "§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled");
                this.soundButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.char_settings.hurt_sounds"),
                        this.settings.hurtSoundsEnabled ? enabled : disabled
                );
                GenderConfig.saveConfig();
                updateSlidersState();
                break;
            case 14:
                boolean newArmorPhysics = !GenderConfig.getOverrideArmorPhysics(this.mc.thePlayer);
                GenderConfig.setOverrideArmorPhysics(this.mc.thePlayer, newArmorPhysics);
                this.armorPhysicsButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.char_settings.override_armor_physics"),
                        newArmorPhysics ? ("§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled")) : ("§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled"))
                );
                break;
            case 15:
                boolean newHide = !GenderConfig.getHideInArmor(this.mc.thePlayer);
                GenderConfig.setHideInArmor(this.mc.thePlayer, newHide);
                this.hideInArmorButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.char_settings.hide_in_armor"),
                        newHide ? ("§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled")) : ("§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled"))
                );
                break;
            case 16:
                boolean newTooltip = !GenderConfig.getShowArmorTooltip(this.mc.thePlayer);
                GenderConfig.setShowArmorTooltip(this.mc.thePlayer, newTooltip);
                this.showArmorTooltipButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.char_settings.show_armor_stat"),
                        newTooltip ? ("§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled")) : ("§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled"))
                );
                break;
            case 17:
                boolean newHoliday = !GenderConfig.getHolidayThemes(this.mc.thePlayer);
                GenderConfig.setHolidayThemes(this.mc.thePlayer, newHoliday);
                this.holidayThemesButton.displayString = String.format(
                        StatCollector.translateToLocal("wildfire_gender.misc.holiday_themes"),
                        newHoliday ? ("§a" + StatCollector.translateToLocal("wildfire_gender.label.enabled")) : ("§c" + StatCollector.translateToLocal("wildfire_gender.label.disabled"))
                );
                break;
            case 18:
                this.mc.displayGuiScreen(new GuiBreastUVEditor(this, this.mc.thePlayer != null ? this.mc.thePlayer.getUniqueID() : null));
                break;
            case 11:
                this.selectedTab = 0;
                updateSlidersState();
                break;
            case 12:
                this.selectedTab = 1;
                updateSlidersState();
                break;
            case 13:
                this.selectedTab = 2;
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
            this.mc.displayGuiScreen(new GuiWardrobe());
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = "Other".equals(this.settings.gender) ? (this.isDarkMode ? DARK_OTHER_BACKGROUND_TEXTURE : OTHER_BACKGROUND_TEXTURE) : (this.isDarkMode ? DARK_FEMALE_BACKGROUND_TEXTURE : FEMALE_BACKGROUND_TEXTURE);
        this.mc.getTextureManager().bindTexture(background);
        int guiWidth = 272;
        int guiHeight = 130;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        drawModalRectWithCustomSizedTexture(guiLeft + 0, guiTop + 0, 0, 0, guiWidth, guiHeight, 512, 512);

        ResourceLocation tabTexture;
        switch (this.selectedTab) {
            case 1:
                tabTexture = this.isDarkMode ? DARK_BREAST_PHYSICS_TAB_TEXTURE : BREAST_PHYSICS_TAB_TEXTURE;
                break;
            case 2:
                tabTexture = this.isDarkMode ? DARK_MISCELLANEOUS_TAB_TEXTURE : MISCELLANEOUS_TAB_TEXTURE;
                break;
            default:
                tabTexture = this.isDarkMode ? DARK_CUSTOMIZATION_TAB_TEXTURE : CUSTOMIZATION_TAB_TEXTURE;
                break;
        }
        this.mc.getTextureManager().bindTexture(tabTexture);
        drawModalRectWithCustomSizedTexture(guiLeft + 94, guiTop + 26, 0, 0, guiWidth, guiHeight, 512, 512);

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (GuiButton button : this.buttonList) {
            if (button instanceof WildfireButton) {
                WildfireButton wildfireButton = (WildfireButton) button;
                if (!wildfireButton.enabled && (wildfireButton.id == 11 || wildfireButton.id == 12 || wildfireButton.id == 13)) {
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
                    wildfireButton.drawButton(this.mc, mouseX, mouseY);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }

        int previewCenterX = guiLeft + 44;
        int previewCenterY = guiTop + 78;
        GuiUtils.drawEntityOnScreenNoScissor(this, previewCenterX, previewCenterY, 42, mouseX - previewCenterX, mouseY - previewCenterY, this.mc.thePlayer);

        this.drawCenteredString(this.fontRendererObj, StatCollector.translateToLocal("wildfire_gender.appearance_settings.title"), this.width / 2, guiTop - 15, 0xFFFFFF);

        if (this.armorPhysicsButton != null && this.armorPhysicsButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(StatCollector.translateToLocal("wildfire_gender.tooltip.override_armor_physics.line1")), mouseX, mouseY);
        }
        if (this.soundButton != null && this.soundButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(StatCollector.translateToLocal("wildfire_gender.tooltip.hurt_sounds")), mouseX, mouseY);
        }
        if (this.holidayThemesButton != null && this.holidayThemesButton.isMouseOver()) {
            drawHoveringText(Arrays.asList(StatCollector.translateToLocal("wildfire_gender.tooltip.holiday_themes.line1")), mouseX, mouseY);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
