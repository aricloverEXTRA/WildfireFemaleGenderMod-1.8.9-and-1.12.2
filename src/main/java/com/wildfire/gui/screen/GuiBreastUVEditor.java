package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.uvs.BreastTypes;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.main.uvs.UVStorage;
import com.wildfire.main.ArmorTextureHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import java.io.IOException;
import java.util.*;

public class GuiBreastUVEditor extends GuiScreen {
    private final GuiScreen parent;
    private final UUID playerUuid;

    private UVDirection selectedDirection = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVLayout selectedUVs;

    private static final int SIDEBAR_WIDTH = 190;
    private static final int TEXTURE_DRAW_SIZE = 196;
    private int uvWindowX, uvWindowY;
    private int previewCenterX, previewCenterY;

    private static final ResourceLocation ADD_ICON = new ResourceLocation("wildfire_gender:textures/gui/widgets/add.png");
    private static final ResourceLocation SUB_ICON = new ResourceLocation("wildfire_gender:textures/gui/widgets/subtract.png");

    // Colors
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GREY = 0xFF888888;
    private static final int COLOR_YELLOW = 0xFFFFDD55;
    private static final int COLOR_CYAN = 0xFF00FFFF;
    private static final int COLOR_SIDEBAR_BG = 0xCC000000;
    private static final int HIGHLIGHT_OVERLAY = 0x55FFDD55;

    public GuiBreastUVEditor(GuiScreen parent, UUID playerUuid) {
        this.parent = parent;
        this.playerUuid = playerUuid;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.selectedUVs = UVStorage.getLayout(playerUuid, selectedBreastIndex);

        uvWindowX = 10;
        uvWindowY = this.height / 2 - TEXTURE_DRAW_SIZE / 2;
        previewCenterX = (this.width - SIDEBAR_WIDTH) / 2 + 20;
        previewCenterY = this.height / 2 + 50;

        int sidebarX = this.width - SIDEBAR_WIDTH;

        // Reset
        this.buttonList.add(new WildfireButton(0, sidebarX + 5, 5, SIDEBAR_WIDTH - 10, 20,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.reset_defaults_all")));

        setupSelectionButtons(sidebarX);
        if (selectedDirection != null) setupAdjustmentButtons(sidebarX);
    }

    private void setupSelectionButtons(int sidebarX) {
        int leftColX = sidebarX + 5;
        int btnW = (SIDEBAR_WIDTH - 15) / 2;
        
        this.buttonList.add(new WildfireButton(1, leftColX, 40, btnW, 15, "Left Base"));
        this.buttonList.add(new WildfireButton(2, leftColX + btnW + 5, 40, btnW, 15, "Right Base"));
        this.buttonList.add(new WildfireButton(3, leftColX, 60, btnW, 15, "Left Overlay"));
        this.buttonList.add(new WildfireButton(4, leftColX + btnW + 5, 60, btnW, 15, "Right Overlay"));
    }

    private void setupAdjustmentButtons(int sidebarX) {
        int xStart = sidebarX + SIDEBAR_WIDTH / 2 + 20;
        int yStart = 90;
        for (int i = 0; i < 4; i++) {
            this.buttonList.add(new WildfireButton(100 + i * 2, xStart, yStart + (i * 18), 14, 14, ""));
            this.buttonList.add(new WildfireButton(101 + i * 2, xStart + 16, yStart + (i * 18), 14, 14, ""));
        }
    }

    private void handleAdjustment(int id, UUID uuid) {
        int row = (id - 100) / 2;
        boolean isAdd = (id % 2 != 0);
        UVQuad quad = selectedUVs.get(selectedDirection);
        if (quad == null) return;

        int delta = getIncrement() * (isAdd ? 1 : -1);
        
        // Boundaries Fix: Ensure we stay within 0-63
        if (row == 0) { // Move X
            int move = clamp(delta, -quad.x1(), 63 - quad.x2());
            quad = quad.addX1(move).addX2(move);
        } else if (row == 1) { // Move Y
            int move = clamp(delta, -quad.y1(), 63 - quad.y2());
            quad = quad.addY1(move).addY2(move);
        } else if (row == 2) { // Width
            quad = quad.addX2(clamp(delta, -(quad.x2() - quad.x1()), 63 - quad.x2()));
        } else if (row == 3) { // Height
            quad = quad.addY2(clamp(delta, -(quad.y2() - quad.y1()), 63 - quad.y2()));
        }

        selectedUVs.put(selectedDirection, quad);
        UVStorage.saveLayout(uuid, selectedBreastIndex, selectedUVs);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawRect(this.width - SIDEBAR_WIDTH, 0, this.width, this.height, COLOR_SIDEBAR_BG);

        // Texture Source
        ResourceLocation tex = mc.thePlayer.getLocationSkin();
        if (selectedBreastIndex.name().contains("OVERLAY")) {
            ResourceLocation armor = ArmorTextureHelper.getArmorTextureForPlayerUUID(playerUuid);
            if (armor != null) tex = armor;
        }
        mc.getTextureManager().bindTexture(tex);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawScaledCustomSizeModalRect(uvWindowX, uvWindowY, 0, 0, 64, 64, TEXTURE_DRAW_SIZE, TEXTURE_DRAW_SIZE, 64, 64);

        // Grid/UV Boxes
        for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
            drawFaceBorderWithTooltip(entry.getKey(), entry.getValue(), mouseX, mouseY, selectedDirection != entry.getKey());
        }

        // --- TRANSPARENCY FIX APPLIED HERE ---
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend(); 
        GuiInventory.drawEntityOnScreen(previewCenterX, previewCenterY, 120, (float)(previewCenterX - mouseX), (float)(previewCenterY - 50 - mouseY), mc.thePlayer);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();

        drawRightEditorPanel(this.width - SIDEBAR_WIDTH + 5, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderButtonIcons();
    }

    private void drawFaceBorderWithTooltip(UVDirection dir, UVQuad quad, int mx, int my, boolean faded) {
        int x1 = uvWindowX + (int)((quad.x1() / 64f) * TEXTURE_DRAW_SIZE);
        int y1 = uvWindowY + (int)((quad.y1() / 64f) * TEXTURE_DRAW_SIZE);
        int x2 = uvWindowX + (int)(((quad.x2() + 1) / 64f) * TEXTURE_DRAW_SIZE);
        int y2 = uvWindowY + (int)(((quad.y2() + 1) / 64f) * TEXTURE_DRAW_SIZE);

        int color = dir.getFaceColor(faded);
        drawRect(x1, y1, x2, y1 + 1, color);
        drawRect(x1, y2 - 1, x2, y2, color);
        drawRect(x1, y1, x1 + 1, y2, color);
        drawRect(x2 - 1, y1, x2, y2, color);

        if (mx >= x1 && mx <= x2 && my >= y1 && my <= y2) {
            this.drawHoveringText(Arrays.asList(COLOR_YELLOW + dir.name(), COLOR_CYAN + "X:" + quad.x1() + " Y:" + quad.y1()), mx, my);
        }
    }

    private void drawRightEditorPanel(int x, int mx, int my) {
        this.fontRendererObj.drawString("Type: " + selectedBreastIndex.name(), x, 25, COLOR_WHITE);
        if (selectedDirection == null) return;
        
        this.fontRendererObj.drawString("Face: " + selectedDirection.name(), x, 80, COLOR_YELLOW);
        String[] labels = { "Move X", "Move Y", "Width", "Height" };
        for (int i = 0; i < 4; i++) {
            this.fontRendererObj.drawString(labels[i], x, 93 + (i * 18), COLOR_WHITE);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            UVStorage.unregister(playerUuid);
            initGui();
        } else if (button.id >= 1 && button.id <= 4) {
            selectedBreastIndex = BreastTypes.values()[button.id - 1];
            initGui();
        } else if (button.id >= 100 && selectedDirection != null) {
            handleAdjustment(button.id, playerUuid);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
            UVQuad q = entry.getValue();
            int x1 = uvWindowX + (int)((q.x1() / 64f) * TEXTURE_DRAW_SIZE);
            int y1 = uvWindowY + (int)((q.y1() / 64f) * TEXTURE_DRAW_SIZE);
            int x2 = uvWindowX + (int)(((q.x2() + 1) / 64f) * TEXTURE_DRAW_SIZE);
            int y2 = uvWindowY + (int)(((q.y2() + 1) / 64f) * TEXTURE_DRAW_SIZE);
            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                selectedDirection = entry.getKey();
                initGui();
                return;
            }
        }
    }

    private void renderButtonIcons() {
        for (GuiButton b : this.buttonList) {
            if (b.id >= 100) {
                mc.getTextureManager().bindTexture((b.id % 2 != 0) ? ADD_ICON : SUB_ICON);
                GlStateManager.enableBlend();
                drawModalRectWithCustomSizedTexture(b.xPosition + 4, b.yPosition + 4, 0, 0, 6, 6, 6, 6);
            }
        }
    }

    private int getIncrement() { return isShiftKeyDown() ? (isCtrlKeyDown() ? 20 : 10) : 1; }
    @Override public boolean doesGuiPauseGame() { return false; }
}