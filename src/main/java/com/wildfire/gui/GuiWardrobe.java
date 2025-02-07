package com.wildfire.gui.screen;

import com.wildfire.config.ConfigSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

public class GuiWardrobe extends GuiScreen {
    private static final ResourceLocation MALE_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation FEMALE_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation OTHER_BG = new ResourceLocation("wildfire:textures/gui/wardrobe_bg_other.png");

    private String selectedGender = ConfigSettings.gender; // "Male", "Female", or "Other"

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = (this.width - 263) / 2;
        int centerY = (this.height - 123) / 2;

        // Gender Switch Button
        this.buttonList.add(new GuiButton(0, centerX + 7, centerY + 90, 80, 15, selectedGender));

        // Customize Button (only visible for Female and Other)
        if (!"Male".equals(this.selectedGender)) {
            this.buttonList.add(new GuiButton(1, centerX + 100, centerY + 100, 100, 20, "Customize"));
        }

        // Done Button
        this.buttonList.add(new GuiButton(2, centerX + 100, centerY + 120, 100, 20, "Done"));
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
                this.initGui(); // Refresh the GUI
                break;
            case 1: // Customize
                this.mc.displayGuiScreen(new GuiBreastCustomization());
                break;
            case 2: // Done
                ConfigSettings.saveConfig();
                this.mc.displayGuiScreen(null); // Close the GUI
                break;
            default:
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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
        int centerX = (this.width - 263) / 2;
        int centerY = (this.height - 123) / 2;
        this.drawModalRectWithCustomSizedTexture(centerX, centerY, 0, 0, 263, 123, 512, 512);

        // Adjusted the title position to avoid overlapping
        this.drawCenteredString(this.fontRendererObj, "Female Gender Mod", this.width / 2, centerY - 15, 0xFFFFFF);

        // Draw player entity preview at top left with adjusted position and size
        drawPlayerModel(centerX + 35, centerY + 65, 15, mouseX, mouseY, this.mc.thePlayer);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // Method to draw player entity preview
    private void drawPlayerModel(int posX, int posY, int scale, float mouseX, float mouseY, EntityPlayer player) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        float f = player.renderYawOffset;
        float f1 = player.rotationYaw;
        float f2 = player.rotationPitch;
        float f3 = player.prevRotationYawHead;
        float f4 = player.rotationYawHead;
        player.renderYawOffset = 20; // Tilting the player to face the user's left
        player.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        player.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        player.rotationYawHead = player.rotationYaw;
        player.prevRotationYawHead = player.rotationYaw;
        GlStateManager.rotate(0F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        RenderManager rendermanager = this.mc.getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(player, 0, 0, 0, 0, 1);
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true; // Pause the game when the GUI is open
    }
}
