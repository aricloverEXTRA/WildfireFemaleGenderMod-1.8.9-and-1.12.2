package com.wildfire.gui.screen;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.uvs.BreastTypes;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.main.uvs.UVStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;
import java.util.UUID;

/**
 * Compact UV editor UI (Forge 1.8.9) — icons fixed and centered on 12x12 buttons.
 * - +/- buttons are 12x12; icons are 6x6 and drawn with drawModalRectWithCustomSizedTexture (like the wardrobe).
 * - Save/Done removed; Reset remains.
 * - Layer selectors and face editor panel preserved.
 */
public class GuiBreastUVEditor extends GuiScreen {
    private final GuiScreen parent;
    private final UUID playerUuid;

    private UVDirection selectedDirection = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVLayout selectedUVs;

    private static final int SIDEBAR_WIDTH = 190;

    // Texture preview (left side of screen)
    private static final int TEXTURE_SOURCE_SIZE = 64;
    private static final int TEXTURE_DRAW_SIZE = 196;
    private int uvWindowX, uvWindowY;

    // Player preview
    private static final int MODEL_SCALE = 120;
    private int previewCenterX, previewCenterY;

    // Icons for +/- (small 6x6)
    private static final ResourceLocation ADD_ICON =
            new ResourceLocation("wildfire_gender:textures/gui/widgets/add.png");
    private static final ResourceLocation SUB_ICON =
            new ResourceLocation("wildfire_gender:textures/gui/widgets/subtract.png");

    // Colors
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GREY = 0xFF888888;
    private static final int COLOR_YELLOW = 0xFFFFDD55;
    private static final int COLOR_CYAN = 0xFF00FFFF;
    private static final int COLOR_TIP_BLUE = 0xFF3EA6FF;
    private static final int COLOR_SIDEBAR_BG = 0xCC000000;
    private static final int COLOR_PANEL_BG = 0x66000000;
    private static final int TOOLTIP_BG = 0xCC000000;
    private static final int TOOLTIP_BORDER = 0xFF444444;

    public GuiBreastUVEditor(GuiScreen parent, UUID playerUuid) {
        this.parent = parent;
        this.playerUuid = playerUuid;
    }

    public GuiBreastUVEditor(GuiScreen parent) {
        this(parent, Minecraft.getMinecraft().thePlayer != null
                ? Minecraft.getMinecraft().thePlayer.getUniqueID()
                : null);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        UUID uuid = playerUuid != null ? playerUuid :
                (Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.getUniqueID() : null);
        if (uuid != null) {
            this.selectedUVs = UVStorage.loadForPlayer(uuid);
        } else {
            this.selectedUVs = new UVLayout();
        }

        // Layout anchors
        uvWindowX = 10;
        uvWindowY = this.height / 2 - TEXTURE_DRAW_SIZE / 2;
        previewCenterX = this.width / 2;
        previewCenterY = this.height / 2 + 10;

        int sidebarX = this.width - SIDEBAR_WIDTH;

        // Top-wide Reset
        this.buttonList.add(new WildfireButton(
                0, sidebarX + 5, 5, SIDEBAR_WIDTH - 10, 20,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.reset_defaults_all")));

        // Split sidebar into two compact columns
        int columnsPadding = 6;
        int leftColX = sidebarX + 5;
        int rightColX = sidebarX + SIDEBAR_WIDTH / 2 + 2; // slight gap
        int columnWidth = SIDEBAR_WIDTH / 2 - columnsPadding - 1;

        // Left column: Body Layer + Jacket Layer compact selector
        int leftY = 32;

        // Body Left/Right buttons (compact)
        WildfireButton bodyLeftBtn = new WildfireButton(
                1, leftColX, leftY + 16, (columnWidth - 4) / 2, 15,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.selection.left_breast"));
        WildfireButton bodyRightBtn = new WildfireButton(
                2, leftColX + (columnWidth - 4) / 2 + 4, leftY + 16, (columnWidth - 4) / 2, 15,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.selection.right_breast"));
        leftY += 36;

        // Jacket Layer buttons
        WildfireButton jacketLeftBtn = new WildfireButton(
                3, leftColX, leftY, (columnWidth - 4) / 2, 15,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.selection.left_breast_overlay"));
        WildfireButton jacketRightBtn = new WildfireButton(
                4, leftColX + (columnWidth - 4) / 2 + 4, leftY, (columnWidth - 4) / 2, 15,
                StatCollector.translateToLocal("wildfire_gender.uv_editor.selection.right_breast_overlay"));

        // Toggle feel: disable active one
        if (selectedBreastIndex == BreastTypes.LEFT) bodyLeftBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.RIGHT) bodyRightBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.LEFT_OVERLAY) jacketLeftBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.RIGHT_OVERLAY) jacketRightBtn.enabled = false;

        this.buttonList.add(bodyLeftBtn);
        this.buttonList.add(bodyRightBtn);
        this.buttonList.add(jacketLeftBtn);
        this.buttonList.add(jacketRightBtn);

        // NOTE: Save and Done buttons intentionally removed (ESC closes GUI and changes save instantly)

        // Position increment buttons (compact +/- icon grid in right column when a face is selected)
        if (selectedDirection != null) {
            int editorStartY = 32 + 20; // leave room for title
            int rowHeight = 16;
            int labelW = 46;
            int btnW = 12; // 12x12 clickable area
            int gap = 6;

            // Create Subtract/Add per row (IDs: 100..107)
            for (int row = 0; row < 4; row++) {
                int yRow = editorStartY + row * rowHeight;
                int subId = 100 + row * 2;
                this.buttonList.add(new WildfireButton(subId, rightColX + labelW + gap, yRow, btnW, btnW, ""));
                int addId = 100 + row * 2 + 1;
                this.buttonList.add(new WildfireButton(addId, rightColX + labelW + gap + btnW + 4, yRow, btnW, btnW, ""));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        UUID uuid = playerUuid != null ? playerUuid :
                (Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.getUniqueID() : null);

        switch (button.id) {
            case 0: // Reset
                if (uuid != null) {
                    UVStorage.resetToDefaults(uuid);
                    selectedUVs = UVStorage.loadForPlayer(uuid);
                }
                break;
            case 1: // Body Left
                selectBreastUVMap(BreastTypes.LEFT);
                break;
            case 2: // Body Right
                selectBreastUVMap(BreastTypes.RIGHT);
                break;
            case 3: // Jacket Left (Overlay)
                selectBreastUVMap(BreastTypes.LEFT_OVERLAY);
                break;
            case 4: // Jacket Right (Overlay)
                selectBreastUVMap(BreastTypes.RIGHT_OVERLAY);
                break;
            default:
                // +/- grid
                if (button.id >= 100 && button.id <= 107 && selectedDirection != null) {
                    int row = (button.id - 100) / 2;     // 0..3 => X, Y, W, H
                    boolean isAdd = ((button.id - 100) % 2) == 1;

                    UVQuad quad = selectedUVs.get(selectedDirection);
                    if (quad == null) return;
                    int delta = getIncrement();
                    int toAdd = isAdd ? delta : -delta;

                    if (row == 0) { // X-Pos
                        quad = quad.addX1(toAdd).addX2(toAdd);
                    } else if (row == 1) { // Y-Pos
                        quad = quad.addY1(toAdd).addY2(toAdd);
                    } else if (row == 2) { // Width
                        quad = quad.addX2(toAdd);
                    } else if (row == 3) { // Height
                        quad = quad.addY2(toAdd);
                    }

                    selectedUVs.put(selectedDirection, quad);
                    if (uuid != null) UVStorage.saveForPlayer(uuid, selectedUVs);
                }
                break;
        }
    }

    private void selectBreastUVMap(BreastTypes breast) {
        selectedBreastIndex = breast;
        selectedDirection = null;
        initGui(); // refresh toggle states and grid
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (selectedUVs == null) return;

        for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
            UVDirection direction = entry.getKey();
            UVQuad quad = entry.getValue();
            if (quad == null) continue;

            int rectX1 = uvWindowX + scaleTex(quad.x1());
            int rectY1 = uvWindowY + scaleTex(quad.y1());
            int rectX2 = uvWindowX + scaleTex(quad.x2());
            int rectY2 = uvWindowY + scaleTex(quad.y2());

            if (mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
                if (mouseButton == 0) {
                    selectedDirection = direction;
                } else if (mouseButton == 1) {
                    selectedDirection = null;
                }
                initGui();
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Sidebar background
        drawRect(this.width - SIDEBAR_WIDTH, 0, this.width, this.height, COLOR_SIDEBAR_BG);

        // Column panel backs (visual grouping)
        int sidebarX = this.width - SIDEBAR_WIDTH;
        int leftColX = sidebarX + 5;
        int rightColX = sidebarX + SIDEBAR_WIDTH / 2 + 2;
        int columnWidth = SIDEBAR_WIDTH / 2 - 6 - 1;
        int panelTop = 30;
        int panelBottom = 128;
        drawRect(leftColX, panelTop, leftColX + columnWidth, panelBottom, COLOR_PANEL_BG);
        drawRect(rightColX, panelTop, rightColX + columnWidth, panelBottom + 36, COLOR_PANEL_BG);

        // Skin preview
        if (mc.thePlayer != null) {
            mc.getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1f, 1f, 1f, 1f);
            drawTexturedModalRect(uvWindowX, uvWindowY, 0, 0, TEXTURE_DRAW_SIZE, TEXTURE_DRAW_SIZE);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        // Title
        this.drawCenteredString(this.fontRendererObj,
                StatCollector.translateToLocal("wildfire_gender.uv_editor"),
                this.width / 2, 20, 0xFFFFFF);

        // Draw colored face boxes and tooltips
        if (selectedUVs != null) {
            for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
                drawFaceBorderWithTooltip(entry.getKey(), entry.getValue(), mouseX, mouseY, selectedDirection != entry.getKey());
            }
        }

        // Centered player preview
        GuiInventory.drawEntityOnScreen(previewCenterX,
                previewCenterY,
                MODEL_SCALE,
                -(mouseX - previewCenterX),
                -(mouseY - previewCenterY),
                mc.thePlayer);

        // Right column face editor panel content
        drawRightEditorPanel(rightColX, mouseX, mouseY);

        // Draw buttons (default)
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw add/subtract icons for the small +/- buttons (IDs 100..107)
        // Buttons are 12x12, icons are 6x6; center icons inside buttons.
        final int iconSize = 6;
        for (GuiButton b : this.buttonList) {
            if (b.id >= 100 && b.id <= 107) {
                boolean isAdd = (b.id % 2 == 1);
                ResourceLocation icon = isAdd ? ADD_ICON : SUB_ICON;

                // Bind and draw using the same method as the wardrobe (drawModalRectWithCustomSizedTexture)
                mc.getTextureManager().bindTexture(icon);
                int drawX = b.xPosition + (b.width - iconSize) / 2;
                int drawY = b.yPosition + (b.height - iconSize) / 2;

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                // drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight)
                // icon PNG is 6x6, so textureWidth/textureHeight = 6,6
                drawModalRectWithCustomSizedTexture(drawX, drawY, 0, 0, iconSize, iconSize, iconSize, iconSize);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawRightEditorPanel(int rightColX, int mouseX, int mouseY) {
        int titleY = 32;
        int editorStartY = titleY + 20;
        int rowHeight = 16;
        int labelW = 46;
        int gap = 6;

        if (selectedDirection == null) {
            // No face selected: draw wrapped localized hint in dark grey (4 lines)
            drawWrappedString(StatCollector.translateToLocal("wildfire_gender.uv_editor.no_face_selected"),
                    rightColX + 10, 60, 70, COLOR_GREY);
            return;
        }

        // Selected face title in yellow (use full name + short code)
        String shortName = selectedDirection.getShortName();
        String fullName = fullFaceName(selectedDirection);
        String title = fullName + " (" + shortName + ")";
        this.fontRendererObj.drawString(title, rightColX + 6, titleY, COLOR_YELLOW, false);

        // Labels aligned to the left of +/- buttons
        String[] labels = {
                StatCollector.translateToLocal("wildfire_gender.uv_editor.xpos"),
                StatCollector.translateToLocal("wildfire_gender.uv_editor.ypos"),
                StatCollector.translateToLocal("wildfire_gender.uv_editor.width"),
                StatCollector.translateToLocal("wildfire_gender.uv_editor.height")
        };
        for (int i = 0; i < labels.length; i++) {
            int yRow = editorStartY + i * rowHeight;
            this.fontRendererObj.drawString(labels[i], rightColX + 6, yRow, COLOR_WHITE, false);
        }

        // Tips under the last row — draw smaller so it fits
        int tipY = editorStartY + labels.length * rowHeight + 6;
        GlStateManager.pushMatrix();
        GlStateManager.translate(rightColX + 6, tipY, 0);
        GlStateManager.scale(0.75f, 0.75f, 1.0f); // shrink tips
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("wildfire_gender.uv_editor.increment_tip.line1"),
                0, 0, COLOR_CYAN, false);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("wildfire_gender.uv_editor.increment_tip.line2"),
                0, 12, COLOR_TIP_BLUE, false);
        GlStateManager.popMatrix();
    }

    private void drawFaceBorderWithTooltip(UVDirection direction, UVQuad quad, int mouseX, int mouseY, boolean faded) {
        if (quad == null) return;

        int rectX1 = uvWindowX + scaleTex(quad.x1());
        int rectY1 = uvWindowY + scaleTex(quad.y1());
        int rectX2 = uvWindowX + scaleTex(quad.x2());
        int rectY2 = uvWindowY + scaleTex(quad.y2());

        int borderColor = direction.getFaceColor(faded);
        int borderThickness = 2;

        drawRect(rectX1, rectY1, rectX2, rectY1 + borderThickness, borderColor);
        drawRect(rectX1, rectY2 - borderThickness, rectX2, rectY2, borderColor);
        drawRect(rectX1, rectY1, rectX1 + borderThickness, rectY2, borderColor);
        drawRect(rectX2 - borderThickness, rectY1, rectX2, rectY2, borderColor);

        String faceName = direction.getShortName();
        int centerX = (rectX1 + rectX2) / 2;
        int centerY = (rectY1 + rectY2) / 2;
        int textWidth = this.fontRendererObj.getStringWidth(faceName);
        int textHeight = this.fontRendererObj.FONT_HEIGHT;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, 0);
        GlStateManager.scale(0.7f, 0.7f, 1.0f);
        this.fontRendererObj.drawString(faceName, -textWidth / 2, -textHeight / 2, COLOR_WHITE, true);
        GlStateManager.popMatrix();

        // Custom tooltip: show full face name (yellow) and coordinates (cyan)
        if (mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
            String shortCode = direction.getShortName();
            String full = fullFaceName(direction) + " (" + shortCode + ")";
            String coords = "[" + quad.x1() + ", " + quad.y1() + ", " + quad.x2() + ", " + quad.y2() + "]";

            int pad = 6;
            int titleW = this.fontRendererObj.getStringWidth(full);
            int coordsW = this.fontRendererObj.getStringWidth(coords);
            int w = Math.max(titleW, coordsW) + pad * 2;
            int h = this.fontRendererObj.FONT_HEIGHT + 4 + this.fontRendererObj.FONT_HEIGHT; // two lines

            int tx = mouseX + 12;
            int ty = mouseY - 8;

            // Ensure tooltip stays on screen
            if (tx + w > this.width) tx = mouseX - 12 - w;
            if (ty + h > this.height) ty = this.height - h - 4;
            if (ty < 4) ty = 4;

            // Background
            drawRect(tx, ty, tx + w, ty + h, TOOLTIP_BG);
            // Border
            drawRect(tx, ty, tx + w, ty + 1, TOOLTIP_BORDER);
            drawRect(tx, ty + h - 1, tx + w, ty + h, TOOLTIP_BORDER);
            drawRect(tx, ty, tx + 1, ty + h, TOOLTIP_BORDER);
            drawRect(tx + w - 1, ty, tx + w, ty + h, TOOLTIP_BORDER);

            // Draw title (yellow)
            this.fontRendererObj.drawString(full, tx + pad, ty + 1, COLOR_YELLOW, false);
            // Draw coords (cyan) under title
            this.fontRendererObj.drawString(coords, tx + pad, ty + 1 + this.fontRendererObj.FONT_HEIGHT, COLOR_CYAN, false);
        }
    }

    private String fullFaceName(UVDirection dir) {
        // Map directions to friendly names
        switch (dir) {
            case DOWN: return "Bottom Face";
            case UP: return "Top Face";
            case NORTH: return "Front Face";
            case EAST: return "Outer Face";
            case WEST: return "Inner Face";
            default: return dir.getShortName();
        }
    }

    private void drawWrappedString(String text, int x, int y, int maxWidth, int color) {
        // Draw the specific 4-line hint block as requested
        String[] wrapped = new String[]{
                "Select a",
                "texture face",
                "on the left to",
                "edit it."
        };
        for (int i = 0; i < wrapped.length; i++) {
            this.fontRendererObj.drawString(wrapped[i], x, y + i * 12, color, false);
        }
    }

    private int scaleTex(int v) {
        return (int) ((v / (float) TEXTURE_SOURCE_SIZE) * TEXTURE_DRAW_SIZE);
    }

    private int getIncrement() {
        boolean shift = isShiftKeyDown();
        boolean ctrl = isCtrlKeyDown();
        if (shift && ctrl) return 20;
        if (shift) return 10;
        return 1;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}