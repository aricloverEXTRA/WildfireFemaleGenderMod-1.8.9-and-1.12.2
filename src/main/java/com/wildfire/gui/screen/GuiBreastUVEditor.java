package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.ArmorTextureHelper;
import com.wildfire.main.uvs.BreastTypes;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.main.uvs.UVStorage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class GuiBreastUVEditor extends GuiScreen {
    private static final int SIDEBAR_WIDTH = 190;
    private static final int TEXTURE_DRAW_SIZE = 196;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFFDD55;
    private static final int COLOR_CYAN = 0xFF00FFFF;
    private static final int COLOR_SIDEBAR_BG = 0xCC000000;

    private final UUID playerUuid;

    private UVDirection selectedDirection = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVLayout selectedUVs;

    private int uvWindowX;
    private int uvWindowY;
    private int previewCenterX;
    private int previewCenterY;

    private static final ResourceLocation ADD_ICON = new ResourceLocation("wildfire_gender:textures/gui/widgets/add.png");
    private static final ResourceLocation SUB_ICON = new ResourceLocation("wildfire_gender:textures/gui/widgets/subtract.png");

    public GuiBreastUVEditor(GuiScreen parent, UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.selectedUVs = UVStorage.getLayout(this.playerUuid, this.selectedBreastIndex);

        this.uvWindowX = 10;
        this.uvWindowY = this.height / 2 - TEXTURE_DRAW_SIZE / 2;
        this.previewCenterX = (this.width - SIDEBAR_WIDTH) / 2 + 20;
        this.previewCenterY = this.height / 2 + 50;

        int sidebarX = this.width - SIDEBAR_WIDTH;
        this.buttonList.add(new WildfireButton(0, sidebarX + 5, 5, SIDEBAR_WIDTH - 10, 20,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.reset_defaults_all")));

        int leftColX = sidebarX + 5;
        int btnW = (SIDEBAR_WIDTH - 15) / 2;
        this.buttonList.add(new WildfireButton(1, leftColX, 40, btnW, 15, "Left Base"));
        this.buttonList.add(new WildfireButton(2, leftColX + btnW + 5, 40, btnW, 15, "Right Base"));
        this.buttonList.add(new WildfireButton(3, leftColX, 60, btnW, 15, "Left Overlay"));
        this.buttonList.add(new WildfireButton(4, leftColX + btnW + 5, 60, btnW, 15, "Right Overlay"));

        if (this.selectedDirection != null) {
            int xStart = sidebarX + SIDEBAR_WIDTH / 2 + 20;
            int yStart = 90;
            for (int i = 0; i < 4; i++) {
                this.buttonList.add(new WildfireButton(100 + i * 2, xStart, yStart + (i * 18), 14, 14, ""));
                this.buttonList.add(new WildfireButton(101 + i * 2, xStart + 16, yStart + (i * 18), 14, 14, ""));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawRect(this.width - SIDEBAR_WIDTH, 0, this.width, this.height, COLOR_SIDEBAR_BG);

        ResourceLocation texture = this.mc.thePlayer != null ? this.mc.thePlayer.getLocationSkin() : null;
        if (this.selectedBreastIndex.name().contains("OVERLAY")) {
            ResourceLocation armor = ArmorTextureHelper.getArmorTextureForPlayerUUID(this.playerUuid);
            if (armor != null) {
                texture = armor;
            }
        }
        if (texture != null) {
            this.mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawScaledCustomSizeModalRect(this.uvWindowX, this.uvWindowY, 0, 0, 64, 64, TEXTURE_DRAW_SIZE, TEXTURE_DRAW_SIZE, 64, 64);
        }

        for (Map.Entry<UVDirection, UVQuad> entry : this.selectedUVs.getAllSides().entrySet()) {
            drawFaceBorderWithTooltip(entry.getKey(), entry.getValue(), mouseX, mouseY, this.selectedDirection != entry.getKey());
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GuiInventory.drawEntityOnScreen(this.previewCenterX, this.previewCenterY, 120,
                (float) (this.previewCenterX - mouseX), (float) (this.previewCenterY - 50 - mouseY), this.mc.thePlayer);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();

        drawRightEditorPanel(this.width - SIDEBAR_WIDTH + 5);
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderButtonIcons();
    }

    private void drawFaceBorderWithTooltip(UVDirection direction, UVQuad quad, int mouseX, int mouseY, boolean faded) {
        if (quad == null) {
            return;
        }
        int x1 = this.uvWindowX + (int) ((quad.x1() / 64.0F) * TEXTURE_DRAW_SIZE);
        int y1 = this.uvWindowY + (int) ((quad.y1() / 64.0F) * TEXTURE_DRAW_SIZE);
        int x2 = this.uvWindowX + (int) (((quad.x2() + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
        int y2 = this.uvWindowY + (int) (((quad.y2() + 1) / 64.0F) * TEXTURE_DRAW_SIZE);

        int color = direction.getFaceColor(faded);
        drawRect(x1, y1, x2, y1 + 1, color);
        drawRect(x1, y2 - 1, x2, y2, color);
        drawRect(x1, y1, x1 + 1, y2, color);
        drawRect(x2 - 1, y1, x2, y2, color);

        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
            this.drawHoveringText(Arrays.asList(COLOR_YELLOW + direction.name(), COLOR_CYAN + "X:" + quad.x1() + " Y:" + quad.y1()), mouseX, mouseY);
        }
    }

    private void drawRightEditorPanel(int x) {
        this.fontRendererObj.drawString("Type: " + this.selectedBreastIndex.name(), x, 25, COLOR_WHITE);
        if (this.selectedDirection == null) {
            return;
        }
        this.fontRendererObj.drawString("Face: " + this.selectedDirection.name(), x, 80, COLOR_YELLOW);
        String[] labels = { "Move X", "Move Y", "Width", "Height" };
        for (int i = 0; i < labels.length; i++) {
            this.fontRendererObj.drawString(labels[i], x, 93 + (i * 18), COLOR_WHITE);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }

        if (button.id == 0) {
            UVStorage.unregister(this.playerUuid);
            this.selectedDirection = null;
            initGui();
        } else if (button.id >= 1 && button.id <= 4) {
            this.selectedBreastIndex = BreastTypes.values()[button.id - 1];
            this.selectedDirection = null;
            initGui();
        } else if (button.id >= 100 && this.selectedDirection != null) {
            handleAdjustment(button.id, this.playerUuid);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (Map.Entry<UVDirection, UVQuad> entry : this.selectedUVs.getAllSides().entrySet()) {
            UVQuad quad = entry.getValue();
            if (quad == null) {
                continue;
            }
            int x1 = this.uvWindowX + (int) ((quad.x1() / 64.0F) * TEXTURE_DRAW_SIZE);
            int y1 = this.uvWindowY + (int) ((quad.y1() / 64.0F) * TEXTURE_DRAW_SIZE);
            int x2 = this.uvWindowX + (int) (((quad.x2() + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
            int y2 = this.uvWindowY + (int) (((quad.y2() + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                this.selectedDirection = entry.getKey();
                initGui();
                return;
            }
        }
    }

    private void handleAdjustment(int id, UUID uuid) {
        if (this.selectedUVs == null || this.selectedDirection == null) {
            return;
        }
        UVQuad quad = this.selectedUVs.get(this.selectedDirection);
        if (quad == null) {
            return;
        }

        int row = (id - 100) / 2;
        boolean isAdd = (id % 2 != 0);
        int delta = getIncrement() * (isAdd ? 1 : -1);

        switch (row) {
            case 0:
                int moveX = clamp(delta, -quad.x1(), 63 - quad.x2());
                quad = quad.addX1(moveX).addX2(moveX);
                break;
            case 1:
                int moveY = clamp(delta, -quad.y1(), 63 - quad.y2());
                quad = quad.addY1(moveY).addY2(moveY);
                break;
            case 2:
                quad = quad.addX2(clamp(delta, -(quad.x2() - quad.x1()), 63 - quad.x2()));
                break;
            case 3:
                quad = quad.addY2(clamp(delta, -(quad.y2() - quad.y1()), 63 - quad.y2()));
                break;
            default:
                break;
        }

        this.selectedUVs.put(this.selectedDirection, quad);
        UVStorage.saveLayout(uuid, this.selectedBreastIndex, this.selectedUVs);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderButtonIcons() {
        for (GuiButton button : this.buttonList) {
            if (button.id >= 100) {
                this.mc.getTextureManager().bindTexture((button.id % 2 != 0) ? ADD_ICON : SUB_ICON);
                GlStateManager.enableBlend();
                drawModalRectWithCustomSizedTexture(button.xPosition + 4, button.yPosition + 4, 0, 0, 6, 6, 6, 6);
            }
        }
    }

    private int getIncrement() {
        return isShiftKeyDown() ? (isCtrlKeyDown() ? 20 : 10) : 1;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}