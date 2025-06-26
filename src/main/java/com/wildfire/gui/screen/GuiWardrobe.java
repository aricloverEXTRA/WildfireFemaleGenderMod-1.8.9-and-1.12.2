package com.wildfire.gui.screen;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.WildfireButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

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

        // Check for Breast Cancer Awareness Month (October)
        // isBreastCancerAwarenessMonth = (Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER);
        isBreastCancerAwarenessMonth = true; // Debugging purposes

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        this.selectedGender = GenderConfig.getGender(player);
        this.isDarkMode = GenderConfig.getDarkMode(player);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        this.buttonList.add(new WildfireButton(0, guiLeft + 6, guiTop + 102, 80, 15, getColoredGenderText(selectedGender)));

        WildfireButton customizeButton = new WildfireButton(1, guiLeft + 100, guiTop + 6, 157, 20, "Character Personalization...");
        customizeButton.enabled = !"Male".equals(this.selectedGender);
        this.buttonList.add(customizeButton);
        this.buttonList.add(new WildfireButton(3, guiLeft + 100, guiTop + 99, 24, 18, ""));
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
            default:
                break;
        }
    }

    private String getColoredGenderText(String gender) {
        switch (gender) {
            case "Male":
                return "§9" + gender;
            case "Female":
                return "§d" + gender;
            case "Other":
                return "§a" + gender;
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

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons first

        this.drawCenteredString(this.fontRendererObj, "Female Gender Mod", this.width / 2, guiTop - 15, 0xFFFFFF);

        drawPlayerModel(guiLeft + 50, guiTop + 120, 60, mouseX - guiLeft - 35, mouseY - guiTop - 65, this.mc.thePlayer);

        if (isBreastCancerAwarenessMonth) {
            int rectLeft = guiLeft + (guiWidth - 300) / 2;
            int rectTop = guiTop + guiHeight + 10 - 8;
            drawRect(rectLeft, rectTop, rectLeft + 300, rectTop + 20, 0x55000000);
            this.drawCenteredString(this.fontRendererObj, "§lHey, it's Breast Cancer Awareness Month!", this.width / 2 - 10, guiTop + guiHeight + 10, 0xFFFFFF);
            mc.getTextureManager().bindTexture(RIBBON_TEXTURE);
            int ribbonX = rectLeft + 5; // Draw on the rectangle
            int ribbonY = rectTop + 1;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.translate(ribbonX, ribbonY, 0);
            this.drawTexturedModalRect(0, 0, 0, 0, 20, 20);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

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
                    String tooltip = isDarkMode ? "Theme: Dark" : "Theme: Light (default)";
                    drawHoveringText(Arrays.asList(tooltip), mouseX, mouseY, fontRendererObj);
                }
                break;
            }
        }
    }

    private void drawPlayerModel(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        int scissorX = (posX - 38) * this.mc.displayWidth / this.width;
        int scissorY = this.mc.displayHeight - (posY + 24 - 48) * this.mc.displayHeight / this.height;
        int scissorWidth = 76 * this.mc.displayWidth / this.width;
        int scissorHeight = 88 * this.mc.displayHeight / this.height;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(135F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.renderEntitySimple(entity, 1.0F);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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
        return true;
    }
}