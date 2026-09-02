package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.ArmorTextureHelper;
import com.wildfire.main.config.GenderConfig;
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

    private final GuiScreen parent;
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
        this.parent = parent;
        this.playerUuid = playerUuid;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        try {
            this.selectedUVs = UVStorage.getLayout(this.playerUuid, this.selectedBreastIndex);
        } catch (Throwable t) {
            this.selectedUVs = new UVLayout(this.selectedBreastIndex);
        }
        if (this.selectedUVs == null) this.selectedUVs = new UVLayout(this.selectedBreastIndex);

        this.uvWindowX = 10;
        this.uvWindowY = this.height / 2 - TEXTURE_DRAW_SIZE / 2;
        this.previewCenterX = (this.width - SIDEBAR_WIDTH) / 2 + 20;
        this.previewCenterY = this.height / 2 + 50;

        int sidebarX = this.width - SIDEBAR_WIDTH;
        this.buttonList.add(new WildfireButton(0, sidebarX + 5, 5, SIDEBAR_WIDTH - 10, 20,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.reset_defaults_all")));
        this.buttonList.add(new WildfireButton(5, sidebarX + 5, 28, SIDEBAR_WIDTH - 10, 15,
                StatCollector.translateToLocal("wildfire_gender.gui.back")));

        int leftColX = sidebarX + 5;
        int btnW = (SIDEBAR_WIDTH - 15) / 2;
        this.buttonList.add(new WildfireButton(1, leftColX, 48, btnW, 15, "Left Base"));
        this.buttonList.add(new WildfireButton(2, leftColX + btnW + 5, 48, btnW, 15, "Right Base"));
        this.buttonList.add(new WildfireButton(3, leftColX, 66, btnW, 15, "Left Overlay"));
        this.buttonList.add(new WildfireButton(4, leftColX + btnW + 5, 66, btnW, 15, "Right Overlay"));

        for (GuiButton b : this.buttonList) {
            if (b.id >= 1 && b.id <= 4) {
                BreastTypes t = BreastTypes.values()[b.id - 1];
                if (t == this.selectedBreastIndex) {
                    b.enabled = false;
                }
            }
        }

        if (this.selectedDirection != null) {
            int xStart = sidebarX + 10;
            int yStart = 90;
            String[] labels = { "Move X", "Move Y", "Width", "Height" };
            for (int i = 0; i < 4; i++) {

                this.buttonList.add(new WildfireButton(100 + i * 2, xStart + 80, yStart + (i * 18), 14, 14, ""));
                this.buttonList.add(new WildfireButton(101 + i * 2, xStart + 96, yStart + (i * 18), 14, 14, ""));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawRect(this.width - SIDEBAR_WIDTH, 0, this.width, this.height, COLOR_SIDEBAR_BG);

        ResourceLocation texture = null;
        try {
            if (this.mc.thePlayer != null) texture = this.mc.thePlayer.getLocationSkin();
            if (this.selectedBreastIndex.name().contains("OVERLAY")) {
                ResourceLocation armor = ArmorTextureHelper.getArmorTextureForPlayerUUID(this.playerUuid, true);
                if (armor == null) armor = ArmorTextureHelper.getArmorTextureForPlayerUUID(this.playerUuid, false);
                if (armor != null) texture = armor;
            }
        } catch (Throwable ignored) {}
        if (texture != null) {
            try {
                this.mc.getTextureManager().bindTexture(texture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawScaledCustomSizeModalRect(this.uvWindowX, this.uvWindowY, 0, 0, 64, 64, TEXTURE_DRAW_SIZE, TEXTURE_DRAW_SIZE, 64, 64);
            } catch (Throwable ignored) {}
        } else {
            drawRect(this.uvWindowX, this.uvWindowY, this.uvWindowX + TEXTURE_DRAW_SIZE, this.uvWindowY + TEXTURE_DRAW_SIZE, 0xFF333333);
        }

        if (this.selectedUVs != null) {
            for (Map.Entry<UVDirection, UVQuad> entry : this.selectedUVs.getAllSides().entrySet()) {
                UVQuad q = entry.getValue();
                if (q == null) continue;

                drawFaceBorderWithTooltip(entry.getKey(), q, mouseX, mouseY, this.selectedDirection != entry.getKey());
            }
        }

        try {
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            if (this.mc.thePlayer != null) {
                GuiInventory.drawEntityOnScreen(this.previewCenterX, this.previewCenterY, 60,
                        (float) (this.previewCenterX - mouseX), (float) (this.previewCenterY - 50 - mouseY), this.mc.thePlayer);
            }
            GlStateManager.enableBlend();
            GlStateManager.popMatrix();
        } catch (Throwable t) {
            try { GlStateManager.popMatrix(); } catch (Throwable ignored) {}
        }

        drawRightEditorPanel(this.width - SIDEBAR_WIDTH + 5);
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderButtonIcons();

        if (this.selectedDirection != null && this.selectedUVs != null) {
            UVQuad q = this.selectedUVs.get(this.selectedDirection);
            if (q != null) {
                String info = String.format("%s: [%d,%d -> %d,%d] %s", this.selectedDirection.name(), q.x1(), q.y1(), q.x2(), q.y2(), this.selectedDirection.getDirectionText(this.selectedBreastIndex));

            }
        }
    }

    private void drawFaceBorderWithTooltip(UVDirection direction, UVQuad quad, int mouseX, int mouseY, boolean faded) {
        if (quad == null) return;

        int qx1 = Math.max(0, Math.min(63, quad.x1()));
        int qy1 = Math.max(0, Math.min(63, quad.y1()));
        int qx2 = Math.max(0, Math.min(63, quad.x2()));
        int qy2 = Math.max(0, Math.min(63, quad.y2()));
        if (qx2 < qx1 || qy2 < qy1) return;

        int x1 = this.uvWindowX + (int) ((qx1 / 64.0F) * TEXTURE_DRAW_SIZE);
        int y1 = this.uvWindowY + (int) ((qy1 / 64.0F) * TEXTURE_DRAW_SIZE);
        int x2 = this.uvWindowX + (int) (((qx2 + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
        int y2 = this.uvWindowY + (int) (((qy2 + 1) / 64.0F) * TEXTURE_DRAW_SIZE);

        int color = direction.getFaceColor(faded);
        drawRect(x1, y1, x2, y1 + 1, color);
        drawRect(x1, y2 - 1, x2, y2, color);
        drawRect(x1, y1, x1 + 1, y2, color);
        drawRect(x2 - 1, y1, x2, y2, color);

        int fill = (color & 0x00FFFFFF) | 0x22000000;
        drawRect(x1+1, y1+1, x2-1, y2-1, fill);

        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
            this.drawHoveringText(Arrays.asList(
                "§e" + direction.name() + " §7(" + direction.getDirectionText(this.selectedBreastIndex) + ")",
                "§bX:" + quad.x1() + " Y:" + quad.y1() + " -> " + quad.x2() + "," + quad.y2(),
                "§7Click to select"), mouseX, mouseY);
        }
    }

    private void drawRightEditorPanel(int x) {
        this.fontRendererObj.drawString("Type: " + this.selectedBreastIndex.name(), x, 25, COLOR_WHITE);

        if (this.selectedUVs != null) {
            int y = 85;
            if (this.selectedDirection == null) {
                this.fontRendererObj.drawString("§7Click a face on texture", x, y, COLOR_WHITE);
                this.fontRendererObj.drawString("§7to edit UVs", x, y+10, COLOR_WHITE);
            } else {
                this.fontRendererObj.drawString("Face: " + this.selectedDirection.name(), x, 80, COLOR_YELLOW);
                this.fontRendererObj.drawString("§7" + this.selectedDirection.getDirectionText(this.selectedBreastIndex), x, 90, COLOR_WHITE);
                String[] labels = { "Move X", "Move Y", "Width", "Height" };
                for (int i = 0; i < labels.length; i++) {
                    this.fontRendererObj.drawString(labels[i], x, 103 + (i * 18), COLOR_WHITE);
                }
                UVQuad q = this.selectedUVs.get(this.selectedDirection);
                if (q != null) {
                    this.fontRendererObj.drawString(String.format("§7[%d,%d %dx%d]", q.x1(), q.y1(), q.x2()-q.x1()+1, q.y2()-q.y1()+1), x, 175, COLOR_CYAN);
                }
                this.fontRendererObj.drawString("§7Shift: x10  Ctrl+Shift: x20", x, 185, 0xFFAAAAAA);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == 0) {
            UVStorage.unregister(this.playerUuid);
            this.selectedDirection = null;
            this.selectedUVs = new UVLayout(this.selectedBreastIndex);
            initGui();
        } else if (button.id == 5) {
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id >= 1 && button.id <= 4) {

            if (this.selectedUVs != null && this.playerUuid != null) {
                try { UVStorage.saveLayout(this.playerUuid, this.selectedBreastIndex, this.selectedUVs); } catch (Throwable ignored) {}
            }
            this.selectedBreastIndex = BreastTypes.values()[button.id - 1];
            this.selectedDirection = null;
            try {
                this.selectedUVs = UVStorage.getLayout(this.playerUuid, this.selectedBreastIndex);
            } catch (Throwable t) {
                this.selectedUVs = new UVLayout(this.selectedBreastIndex);
            }
            initGui();
        } else if (button.id >= 100 && this.selectedDirection != null) {
            handleAdjustment(button.id, this.playerUuid);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.selectedUVs == null) return;

        for (Map.Entry<UVDirection, UVQuad> entry : this.selectedUVs.getAllSides().entrySet()) {
            UVQuad quad = entry.getValue();
            if (quad == null) continue;
            int qx1 = Math.max(0, Math.min(63, quad.x1()));
            int qy1 = Math.max(0, Math.min(63, quad.y1()));
            int qx2 = Math.max(0, Math.min(63, quad.x2()));
            int qy2 = Math.max(0, Math.min(63, quad.y2()));
            int x1 = this.uvWindowX + (int) ((qx1 / 64.0F) * TEXTURE_DRAW_SIZE);
            int y1 = this.uvWindowY + (int) ((qy1 / 64.0F) * TEXTURE_DRAW_SIZE);
            int x2 = this.uvWindowX + (int) (((qx2 + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
            int y2 = this.uvWindowY + (int) (((qy2 + 1) / 64.0F) * TEXTURE_DRAW_SIZE);
            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                this.selectedDirection = entry.getKey();
                initGui();
                return;
            }
        }

        if (mouseX >= this.uvWindowX && mouseX <= this.uvWindowX + TEXTURE_DRAW_SIZE &&
            mouseY >= this.uvWindowY && mouseY <= this.uvWindowY + TEXTURE_DRAW_SIZE) {

        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {

            if (this.selectedUVs != null && this.playerUuid != null) {
                try { UVStorage.saveLayout(this.playerUuid, this.selectedBreastIndex, this.selectedUVs); } catch (Throwable ignored) {}
            }
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void handleAdjustment(int id, UUID uuid) {
        if (this.selectedUVs == null || this.selectedDirection == null) return;
        UVQuad quad = this.selectedUVs.get(this.selectedDirection);
        if (quad == null) return;

        int row = (id - 100) / 2;
        boolean isAdd = (id % 2 != 0);
        int delta = getIncrement() * (isAdd ? 1 : -1);

        try {
            switch (row) {
                case 0: {
                    int moveX = clamp(delta, -quad.x1(), 63 - quad.x2());
                    quad = quad.addX1(moveX).addX2(moveX);
                    break;
                }
                case 1: {
                    int moveY = clamp(delta, -quad.y1(), 63 - quad.y2());
                    quad = quad.addY1(moveY).addY2(moveY);
                    break;
                }
                case 2: {
                    int newW = clamp(delta, -(quad.x2() - quad.x1()), 63 - quad.x2());
                    quad = quad.addX2(newW);
                    break;
                }
                case 3: {
                    int newH = clamp(delta, -(quad.y2() - quad.y1()), 63 - quad.y2());
                    quad = quad.addY2(newH);
                    break;
                }
                default: break;
            }
            this.selectedUVs.put(this.selectedDirection, quad);
            UVStorage.saveLayout(uuid, this.selectedBreastIndex, this.selectedUVs);

            try { UVStorage.generateBreastTextures(uuid); } catch (Throwable ignored) {}
        } catch (Throwable t) {
            System.err.println("[WFG] UV adjustment failed: " + t.getMessage());
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderButtonIcons() {
        for (GuiButton button : this.buttonList) {
            if (button.id >= 100) {
                try {
                    this.mc.getTextureManager().bindTexture((button.id % 2 != 0) ? ADD_ICON : SUB_ICON);
                    GlStateManager.enableBlend();
                    drawModalRectWithCustomSizedTexture(button.xPosition + 4, button.yPosition + 4, 0, 0, 6, 6, 6, 6);
                    GlStateManager.disableBlend();
                } catch (Throwable ignored) {}
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

    @Override
    public void onGuiClosed() {

        if (this.selectedUVs != null && this.playerUuid != null) {
            try { UVStorage.saveLayout(this.playerUuid, this.selectedBreastIndex, this.selectedUVs); } catch (Throwable ignored) {}
        }
        super.onGuiClosed();
    }
}
