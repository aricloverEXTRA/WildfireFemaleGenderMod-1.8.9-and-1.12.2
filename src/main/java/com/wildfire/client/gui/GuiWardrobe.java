package com.wildfire.client.gui.screen;

import com.wildfire.config.ConfigSettings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.util.Calendar;
import org.lwjgl.opengl.GL11;

public class GuiWardrobe extends GuiScreen {
    private static final ResourceLocation MALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation FEMALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation OTHER_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_other.png");
    private static final ResourceLocation RIBBON_TEXTURE = new ResourceLocation("wildfire_gender:textures/bc_ribbon.png");
    private static final ResourceLocation CLOUD_ICON = new ResourceLocation("wildfire_gender:textures/cloud.png");

    private static final int BUTTON_BACKGROUND_COLOR = 0x77000000;
    private static final int BUTTON_HIGHLIGHT_COLOR = 0x55000000;

    private String selectedGender = ConfigSettings.gender; // "Male", "Female", or "Other"

    private boolean isBreastCancerAwarenessMonth;

    @Override
    public void initGui() {
        this.buttonList.clear();

        // Check for Breast Cancer Awareness Month (October)
        Calendar calendar = Calendar.getInstance();
        isBreastCancerAwarenessMonth = true; // (calendar.get(Calendar.MONTH) == Calendar.OCTOBER);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        this.buttonList.add(new CustomButton(0, guiLeft + 4, guiTop + 102, 80, 15, getColoredGenderText(selectedGender)));

        CustomButton customizeButton = new CustomButton(1, guiLeft + 100, guiTop + 5, 157, 20, "Character Personalization...");
        customizeButton.enabled = !"Male".equals(this.selectedGender);
        this.buttonList.add(customizeButton);
        this.buttonList.add(new CustomButton(3, guiLeft + 100, guiTop + 99, 24, 18, ""));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if ("Male".equals(this.selectedGender)) {
                    this.selectedGender = "Female";
                    ConfigSettings.gender = "Female";
                    ConfigSettings.breastsEnabled = true;
                } else if ("Female".equals(this.selectedGender)) {
                    this.selectedGender = "Other";
                    ConfigSettings.gender = "Other";
                    ConfigSettings.breastsEnabled = true;
                } else {
                    this.selectedGender = "Male";
                    ConfigSettings.gender = "Male";
                    ConfigSettings.breastsEnabled = false;
                }
                button.displayString = getColoredGenderText(this.selectedGender);
                this.initGui();
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiBreastCustomization());
                break;
            case 2:
                ConfigSettings.saveConfig();
                this.mc.displayGuiScreen(null);
                break;
            case 3: // Cloud Settings (Placeholder)
                // Future implementation for cloud settings
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
        this.drawDefaultBackground();
        
        ResourceLocation background;
        if ("Male".equals(this.selectedGender)) {
            background = MALE_BG;
        } else if ("Female".equals(this.selectedGender)) {
            background = FEMALE_BG;
        } else {
            background = OTHER_BG;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        this.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 512, 512);
        this.drawCenteredString(this.fontRendererObj, "Female Gender Mod", this.width / 2, guiTop - 15, 0xFFFFFF);

        drawPlayerModel(guiLeft + 50, guiTop + 120, 60, mouseX - guiLeft - 35, mouseY - guiTop - 65, this.mc.thePlayer);

        // Cloud Icon		
		this.mc.getTextureManager().bindTexture(CLOUD_ICON);
		int cloudButtonX = guiLeft + 110;
		int cloudButtonY = guiTop + 99;
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(cloudButtonX + 2, cloudButtonY + 2, 0);
		this.drawTexturedModalRect(0, 0, 0, 0, 20, 20);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

        // Breast Cancer Awareness Month Ribbon
        if (isBreastCancerAwarenessMonth) {
            int rectLeft = guiLeft + (guiWidth - 300) / 2;
            int rectTop = guiTop + guiHeight + 10 - 8;
            drawRect(rectLeft, rectTop, rectLeft + 300, rectTop + 20, 0x55000000);

            this.drawCenteredString(this.fontRendererObj, "§lHey, it's Breast Cancer Awareness Month!", this.width / 2 - 10, guiTop + guiHeight + 10, 0xFFFFFF);
            this.mc.getTextureManager().bindTexture(RIBBON_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.translate(rectLeft + 260 + 200, rectTop + 2, 0);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(0, 0, 0, 0, 26, 26);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

private void drawPlayerModel(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entity) {
    GlStateManager.enableColorMaterial();
    GlStateManager.pushMatrix();

    int scissorX = (posX - 38) * this.mc.displayWidth / this.width;
    int scissorY = this.mc.displayHeight - (posY + 24 - 48) * this.mc.displayHeight / this.height;
    int scissorWidth = 76 * this.mc.displayWidth / this.width;
    int scissorHeight = 88 * this.mc.displayHeight / this.height;

    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

    GlStateManager.translate((float) posX, (float) posY, 50.0F);
    GlStateManager.scale((float) -scale, (float) scale, (float) scale);
    GlStateManager.rotate(180F, 0F, 0F, 1F);
    GlStateManager.rotate(135F, 0F, 1F, 0F);
    RenderHelper.enableStandardItemLighting();
    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
    rendermanager.setPlayerViewY(180.0F);
    rendermanager.renderEntitySimple(entity, 1.0F);

    GL11.glDisable(GL11.GL_SCISSOR_TEST);

    GlStateManager.popMatrix();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
}

    @Override
    public boolean doesGuiPauseGame() {
        return true;
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
                int color = this.hovered ? BUTTON_HIGHLIGHT_COLOR : BUTTON_BACKGROUND_COLOR;
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
