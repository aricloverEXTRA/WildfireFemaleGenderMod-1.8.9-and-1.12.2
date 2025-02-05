package com.wildfire.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;

public class GuiCustomization extends GuiScreen {
    private static final int BUTTON_GENDER_MALE = 0;
    private static final int BUTTON_GENDER_FEMALE = 1;
    private static final int BUTTON_GENDER_OTHER = 2;
    private static final int BUTTON_CUSTOMIZE_BREASTS = 3;

    private String gender = "Male";

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_GENDER_MALE, this.width / 2 - 100, this.height / 2 - 50, "Male"));
        this.buttonList.add(new GuiButton(BUTTON_GENDER_FEMALE, this.width / 2 - 100, this.height / 2 - 25, "Female"));
        this.buttonList.add(new GuiButton(BUTTON_GENDER_OTHER, this.width / 2 - 100, this.height / 2, "Other"));
        this.buttonList.add(new GuiButton(BUTTON_CUSTOMIZE_BREASTS, this.width / 2 - 100, this.height / 2 + 25, "Customize Breasts"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BUTTON_GENDER_MALE:
                gender = "Male";
                break;
            case BUTTON_GENDER_FEMALE:
                gender = "Female";
                break;
            case BUTTON_GENDER_OTHER:
                gender = "Other";
                break;
            case BUTTON_CUSTOMIZE_BREASTS:
                if (!gender.equals("Male")) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiBreastCustomization(gender));
                }
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Customization GUI", this.width / 2, 40, 0xFFFFFF);
    }
}
