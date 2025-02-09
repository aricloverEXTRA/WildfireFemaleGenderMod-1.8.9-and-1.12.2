package com.wildfire.client.gui.screen;

import com.wildfire.config.ConfigSettings;
import com.wildfire.client.gui.screen.GuiBreastCustomization;
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

public class GuiWardrobe extends GuiScreen {
    private static final ResourceLocation MALE_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation FEMALE_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation OTHER_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_other.png");
    private static final ResourceLocation RIBBON_TEXTURE = new ResourceLocation("wildfire:textures/bc_ribbon.png");
    private static final ResourceLocation CLOUD_ICON = new ResourceLocation("wildfire:textures/cloud.png");

    private static final int BUTTON_BACKGROUND_COLOR = 0x55000000;
    private static final int BUTTON_HIGHLIGHT_COLOR = 0x77000000;

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

        // Gender Switch Button
        this.buttonList.add(new CustomButton(0, guiLeft + 4, guiTop + 102, 80, 15, selectedGender)); // Moved left by 3px and down by 2px

        // Customize Button (only visible for Female and Other)
        CustomButton customizeButton = new CustomButton(1, guiLeft + 100, guiTop + 8, 157, 20, "Character Personalization..."); // Moved up by 5px and renamed
        customizeButton.enabled = !"Male".equals(this.selectedGender);
        this.buttonList.add(customizeButton);

        // Cloud Settings Button
        this.buttonList.add(new CustomButton(3, guiLeft + 120, guiTop + 99, 24, 18, "")); // Moved up by 3px and left by 45px

        // Done Button
        this.buttonList.add(new CustomButton(2, guiLeft + 202, guiTop + 105, 80, 20, "Done")); // Moved up by 5px and to the right by 2px
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: // Switch Gender
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
                button.displayString = this.selectedGender;
                updateGenderButtonColor(button, this.selectedGender);
                this.initGui(); // Refresh the GUI
                break;
            case 1: // Customize
                this.mc.displayGuiScreen(new GuiBreastCustomization());
                break;
            case 2: // Done
                ConfigSettings.saveConfig();
                this.mc.displayGuiScreen(null); // Close the GUI
                break;
            case 3: // Cloud Settings (Placeholder)
                // Future implementation for cloud settings
                break;
            default:
                break;
        }
    }

    private void updateGenderButtonColor(GuiButton button, String gender) {
        switch (gender) {
            case "Male":
                button.packedFGColour = 0x0000FF; // Blue
                break;
            case "Female":
                button.packedFGColour = 0xFF00FF; // Pink
                break;
            case "Other":
                button.packedFGColour = 0x00FF00; // Green
                break;
            default:
                button.packedFGColour = 0xFFFFFF; // Default color (White)
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the darkened background like Inventory and Pause Menu
        this.drawDefaultBackground();
        
        // Draw background based on selected gender
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

        // Draw title
        this.drawCenteredString(this.fontRendererObj, "Female Gender Mod", this.width / 2, guiTop - 15, 0xFFFFFF);

        // Draw player entity preview
        drawPlayerModel(guiLeft + 50, guiTop + 120, 60, mouseX - guiLeft - 35, mouseY - guiTop - 65, this.mc.thePlayer); // Moved to the left

        // Cloud Icon
        this.mc.getTextureManager().bindTexture(CLOUD_ICON);
        this.drawTexturedModalRect(guiLeft + 120, guiTop + 99, 0, 0, 32, 26); // Draw cloud icon

        // Breast Cancer Awareness Month Ribbon
        if (isBreastCancerAwarenessMonth) {
            // Draw black transparent rectangle
            int rectLeft = guiLeft + (guiWidth - 300) / 2;
            int rectTop = guiTop + guiHeight + 10 - 8; // Adjusted for centering
            drawRect(rectLeft, rectTop, rectLeft + 300, rectTop + 20, 0x55000000);

            // Draw awareness text
            this.drawCenteredString(this.fontRendererObj, "Hey, it's Breast Cancer Awareness Month!", this.width / 2 - 10, guiTop + guiHeight + 10, 0xFFFFFF);

            // Draw ribbon texture
            this.mc.getTextureManager().bindTexture(RIBBON_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.translate(rectLeft + 260 + 200, rectTop + 2, 0); // Position the ribbon
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(0, 0, 0, 0, 26, 26);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // Method to draw player entity preview using the inventory screen's player renderer
    private void drawPlayerModel(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale); // Invert character
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(135F, 0F, 1F, 0F); // Rotate the player model to face diagonally left
        float rotationAmount = MathHelper.clamp_float((mouseX + mouseY) * 0.01F, -1.0F, 1.0F); // Minimal movement with cursor
        GlStateManager.rotate(rotationAmount * 5.0F, 0F, 1F, 0F); // Adjust rotation
        RenderHelper.enableStandardItemLighting();
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.renderEntitySimple(entity, 1.0F);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true; // Pause the game when the GUI is open
    }

    // Custom button class to use dark background similar to Breast Cancer Awareness text background
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
