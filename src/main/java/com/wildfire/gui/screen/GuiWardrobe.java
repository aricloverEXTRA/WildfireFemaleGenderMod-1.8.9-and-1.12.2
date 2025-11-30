package com.wildfire.gui.screen;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class GuiWardrobe extends GuiScreen {
    private static final ResourceLocation MALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation FEMALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation OTHER_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_other.png");
    private static final ResourceLocation RIBBON_TEXTURE = new ResourceLocation("wildfire_gender:textures/bc_ribbon.png");
    private static final ResourceLocation SUN_ICON = new ResourceLocation("wildfire_gender:textures/sun.png");
    private static final ResourceLocation MOON_ICON = new ResourceLocation("wildfire_gender:textures/moon.png");
    private static final ResourceLocation DARK_MALE_BG = new ResourceLocation("wildfire_gender:textures/darkmode/gui/wardrobe_bg_male.png");
    private static final ResourceLocation DARK_FEMALE_BG = new ResourceLocation("wildfire_gender:textures/darkmode/gui/wardrobe_bg_female.png");
    private static final ResourceLocation DARK_OTHER_BG = new ResourceLocation("wildfire_gender:textures/darkmode/gui/wardrobe_bg_other.png");

    private String selectedGender;
    private boolean isBreastCancerAwarenessMonth;
    private boolean isDarkMode = false;

    @Override
    public void initGui() {
        this.buttonList.clear();

        isBreastCancerAwarenessMonth = true; // Debugging purposes

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        this.selectedGender = GenderConfig.getGender(player);
        this.isDarkMode = GenderConfig.getDarkMode(player);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        this.buttonList.add(new WildfireButton(0, guiLeft + 6, guiTop + 102, 80, 15, getColoredGenderText(selectedGender)));

        WildfireButton customizeButton = new WildfireButton(1, guiLeft + 100, guiTop + 6, 157, 20, StatCollector.translateToLocal("wildfire_gender.appearance_settings.title"));
        customizeButton.enabled = !"Male".equals(this.selectedGender);
        this.buttonList.add(customizeButton);

        // Light/Dark theme toggle
        this.buttonList.add(new WildfireButton(3, guiLeft + 100, guiTop + 99, 24, 18, ""));

        // Mod Credits button: 14px to the right of the theme button, lowered further by 1px
        int themeBtnX = guiLeft + 100;
        int themeBtnWidth = 24;
        int creditsX = themeBtnX + themeBtnWidth + 14;
        int creditsY = guiTop + 102; // lowered 1px further
        this.buttonList.add(new WildfireButton(4, creditsX, creditsY, 78, 15, StatCollector.translateToLocal("wildfire_gender.credits.title")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        switch (button.id) {
            case 0:
                if ("Male".equals(this.selectedGender)) {
                    this.selectedGender = "Female";
                    GenderConfig.setGender(player, "Female");
                    GenderConfig.getPlayerSettings(player).breastsEnabled = true;
                } else if ("Female".equals(this.selectedGender)) {
                    this.selectedGender = "Other";
                    GenderConfig.setGender(player, "Other");
                    GenderConfig.getPlayerSettings(player).breastsEnabled = true;
                } else {
                    this.selectedGender = "Male";
                    GenderConfig.setGender(player, "Male");
                    GenderConfig.getPlayerSettings(player).breastsEnabled = false;
                }
                button.displayString = getColoredGenderText(this.selectedGender);
                this.initGui();
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiBreastCustomization());
                break;
            case 3:
                isDarkMode = !isDarkMode;
                GenderConfig.setDarkMode(player, isDarkMode);
                this.initGui();
                break;
            case 4:
                this.mc.displayGuiScreen(new WildfireCreditsScreen());
                break;
            default:
                break;
        }
    }

    private String getColoredGenderText(String gender) {
        switch (gender) {
            case "Male":
                return "§9" + StatCollector.translateToLocal("wildfire_gender.label.male");
            case "Female":
                return "§d" + StatCollector.translateToLocal("wildfire_gender.label.female");
            case "Other":
                return "§a" + StatCollector.translateToLocal("wildfire_gender.label.other");
            default:
                return gender;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ResourceLocation background = "Male".equals(selectedGender) ? (isDarkMode ? DARK_MALE_BG : MALE_BG) :
                                      "Female".equals(selectedGender) ? (isDarkMode ? DARK_FEMALE_BG : FEMALE_BG) :
                                      (isDarkMode ? DARK_OTHER_BG : OTHER_BG);
        mc.getTextureManager().bindTexture(background);
        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRendererObj, StatCollector.translateToLocal("wildfire_gender.wardrobe.title"), this.width / 2, guiTop - 15, 0xFFFFFF);

        // Inventory-style model preview that follows the cursor, clipped to avoid overlap
        EntityLivingBase entity = this.mc.thePlayer;
        int posX = guiLeft + 50;
        int posY = guiTop + 120;
        int scissorX = (posX - 38) * this.mc.displayWidth / this.width;
        int scissorY = this.mc.displayHeight - (posY + 24 - 48) * this.mc.displayHeight / this.height;
        int scissorWidth = 76 * this.mc.displayWidth / this.width;
        int scissorHeight = 88 * this.mc.displayHeight / this.height;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        GuiUtils.drawEntityOnScreenNoScissor(this, posX, posY, 60, mouseX - posX, mouseY - posY, entity);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (isBreastCancerAwarenessMonth) {
            String text = "\u00A7l" + StatCollector.translateToLocal("wildfire_gender.cancer_awareness.title"); // bold
            int textWidth = this.fontRendererObj.getStringWidth(text);
            int textX = this.width / 2 - 10;
            this.drawCenteredString(this.fontRendererObj, text, textX, guiTop + guiHeight + 10, 0xFFFFFF);

            // Draw ribbon to the right of the text
            mc.getTextureManager().bindTexture(RIBBON_TEXTURE);
            int iconX = textX + (textWidth / 2) + 6; // right of centered text
            int iconY = guiTop + guiHeight + 8;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawTexturedModalRect(iconX, iconY, 0, 0, 20, 20);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        // theme icon and credits tooltip handling
        for (GuiButton button : this.buttonList) {
            if (button.id == 3) {
                ResourceLocation icon = isDarkMode ? MOON_ICON : SUN_ICON;
                mc.getTextureManager().bindTexture(icon);
                int x = button.xPosition + (button.width - 8) / 2;
                int y = button.yPosition + (button.height - 8) / 2;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawModalRectWithCustomSizedTexture(x, y, 0, 0, 8, 8, 8, 8);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                if (button.isMouseOver()) {
                    String tooltip = isDarkMode ? StatCollector.translateToLocal("wildfire_gender.label.dark_mode") : StatCollector.translateToLocal("wildfire_gender.label.light_mode");
                    drawHoveringText(Arrays.asList(tooltip), mouseX, mouseY, fontRendererObj);
                }
            }

            if (button.id == 4 && button.isMouseOver()) {
                drawHoveringText(Arrays.asList(StatCollector.translateToLocal("wildfire_gender.credits.title")), mouseX, mouseY, fontRendererObj);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}