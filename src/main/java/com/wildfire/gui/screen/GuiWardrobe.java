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
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Calendar;

public class GuiWardrobe extends GuiScreen {
    private static final ResourceLocation MALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation FEMALE_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation OTHER_BG = new ResourceLocation("wildfire_gender:textures/gui/wardrobe_bg_other.png");
    private static final ResourceLocation RIBBON_TEXTURE = new ResourceLocation("wildfire_gender:textures/bc_ribbon.png");

    private String selectedGender;
    private boolean isBreastCancerAwarenessMonth;

    @Override
    public void initGui() {
        this.buttonList.clear();

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(mc.thePlayer);
        this.selectedGender = settings != null ? settings.gender : "Male";

        Calendar calendar = Calendar.getInstance();
        isBreastCancerAwarenessMonth = (calendar.get(Calendar.MONTH) == Calendar.OCTOBER);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        this.buttonList.add(new WildfireButton(0, guiLeft + 6, guiTop + 102, 80, 15, getColoredGenderText(selectedGender)));
        WildfireButton customizeButton = new WildfireButton(1, guiLeft + 100, guiTop + 6, 157, 20, "Character Personalization...");
        customizeButton.enabled = !"Male".equals(this.selectedGender);
        this.buttonList.add(customizeButton);
        this.buttonList.add(new WildfireButton(2, guiLeft + 100, guiTop + 80, 157, 20, "Save and Close"));
        this.buttonList.add(new WildfireButton(4, guiLeft + 6, guiTop + 80, 80, 15, "Show Intro Again"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(mc.thePlayer);
        if (settings == null) return;
        switch (button.id) {
            case 0:
                if ("Male".equals(this.selectedGender)) {
                    this.selectedGender = "Female";
                    settings.gender = "Female";
                    settings.breastsEnabled = true;
                } else if ("Female".equals(this.selectedGender)) {
                    this.selectedGender = "Other";
                    settings.gender = "Other";
                    settings.breastsEnabled = true;
                } else {
                    this.selectedGender = "Male";
                    settings.gender = "Male";
                    settings.breastsEnabled = false;
                }
                GenderConfig.saveConfig();
                button.displayString = getColoredGenderText(this.selectedGender);
                this.initGui();
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiBreastCustomization());
                break;
            case 2:
                GenderConfig.saveConfig();
                this.mc.displayGuiScreen(null);
                break;
            case 4:
                settings.showFirstTimeGui = true;
                GenderConfig.saveConfig();
                break;
        }
    }

    private String getColoredGenderText(String gender) {
        switch (gender) {
            case "Male": return "§9" + gender;
            case "Female": return "§d" + gender;
            case "Other": return "§a" + gender;
            default: return gender;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        ResourceLocation background = "Male".equals(selectedGender) ? MALE_BG : "Female".equals(selectedGender) ? FEMALE_BG : OTHER_BG;
        this.mc.getTextureManager().bindTexture(background);

        int guiWidth = 263;
        int guiHeight = 123;
        int guiLeft = (this.width - guiWidth) / 2;
        int guiTop = (this.height - guiHeight) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, guiWidth, guiHeight, 263, 123);

        this.drawCenteredString(this.fontRendererObj, "Female Gender Mod", this.width / 2, guiTop - 15, 0xFFFFFF);

        drawPlayerModel(guiLeft + 50, guiTop + 120, 60, mouseX - guiLeft - 35, mouseY - guiTop - 65, this.mc.thePlayer);

        if (isBreastCancerAwarenessMonth) {
            int rectLeft = guiLeft + (guiWidth - 300) / 2;
            int rectTop = guiTop + guiHeight + 10 - 8;
            drawRect(rectLeft, rectTop, rectLeft + 300, rectTop + 20, 0x55000000);

            this.drawCenteredString(this.fontRendererObj, "§lHey, it's Breast Cancer Awareness Month!", this.width / 2 - 10, guiTop + guiHeight + 10, 0xFFFFFF);
            this.mc.getTextureManager().bindTexture(RIBBON_TEXTURE);

            int ribbonX = rectLeft + 305;
            int ribbonY = rectTop + 2;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.translate(ribbonX, ribbonY, 0);
            this.drawTexturedModalRect(0, 0, 0, 0, 20, 20);
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
}
