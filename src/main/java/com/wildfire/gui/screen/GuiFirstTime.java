package com.wildfire.gui.screen;

import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiFirstTime extends GuiScreen {
    private static final ResourceLocation FIRST_TIME_BG = new ResourceLocation("wildfire_gender:textures/gui/first_time_bg.png");
    private boolean clicked = false;

    @Override
    public void initGui() {
    }

    @Override
    public void updateScreen() {
        if (Mouse.isButtonDown(0) && !clicked) {
            clicked = true;
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(mc.thePlayer);
            if (settings != null) {
                settings.showFirstTimeGui = false;
                GenderConfig.saveConfig();
                mc.displayGuiScreen(new GuiWardrobe());
            }
        } else if (!Mouse.isButtonDown(0)) {
            clicked = false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int bgX = (this.width - 512) / 2;
        int bgY = (this.height - 512) / 2;
        this.mc.getTextureManager().bindTexture(FIRST_TIME_BG);
        this.drawModalRectWithCustomSizedTexture(bgX, bgY, 0, 0, 512, 512, 512, 512);

        String title = "Welcome to Female Gender Mod!";
        String info = "Click anywhere to customize your character's gender and appearance.";
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, bgY + 100, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, info, this.width / 2, bgY + 130, 0xAAAAAA);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}