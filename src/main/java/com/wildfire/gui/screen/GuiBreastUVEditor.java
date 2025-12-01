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
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class GuiBreastUVEditor extends GuiScreen {
    private final GuiScreen parent;
    private final UUID playerUuid;

    private UVDirection selectedDirection = null;
    private BreastTypes selectedBreastIndex = BreastTypes.LEFT;
    private UVLayout selectedUVs;

    private static final int SIDEBAR_WIDTH = 190;

    private static final int TEXTURE_SOURCE_SIZE = 64;
    private static final int TEXTURE_DRAW_SIZE = 196;
    private int uvWindowX, uvWindowY;

    private static final int MODEL_SCALE = 120;
    private int previewCenterX, previewCenterY;

    private static final ResourceLocation ADD_ICON =
            new ResourceLocation("wildfire_gender:textures/gui/widgets/add.png");
    private static final ResourceLocation SUB_ICON =
            new ResourceLocation("wildfire_gender:textures/gui/widgets/subtract.png");

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
        this(parent, Minecraft.getMinecraft().player != null
                ? Minecraft.getMinecraft().player.getUniqueID()
                : null);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        UUID uuid = playerUuid != null ? playerUuid :
                (Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.getUniqueID() : null);
        if (uuid != null) {
            this.selectedUVs = UVStorage.loadForPlayer(uuid);
        } else {
            this.selectedUVs = new UVLayout();
        }

        uvWindowX = 10;
        uvWindowY = this.height / 2 - TEXTURE_DRAW_SIZE / 2;
        previewCenterX = this.width / 2;
        previewCenterY = this.height / 2 + 10;

        int sidebarX = this.width - SIDEBAR_WIDTH;

        this.buttonList.add(new WildfireButton(
                0, sidebarX + 5, 5, SIDEBAR_WIDTH - 10, 20,
                I18n.format("wildfire_gender.uv_editor.reset_defaults_all")));

        int columnsPadding = 6;
        int leftColX = sidebarX + 5;
        int rightColX = sidebarX + SIDEBAR_WIDTH / 2 + 2;
        int columnWidth = SIDEBAR_WIDTH / 2 - columnsPadding - 1;

        int leftY = 32;

        WildfireButton bodyLeftBtn = new WildfireButton(
                1, leftColX, leftY + 16, (columnWidth - 4) / 2, 15,
                I18n.format("wildfire_gender.uv_editor.selection.left_breast"));
        WildfireButton bodyRightBtn = new WildfireButton(
                2, leftColX + (columnWidth - 4) / 2 + 4, leftY + 16, (columnWidth - 4) / 2, 15,
                I18n.format("wildfire_gender.uv_editor.selection.right_breast"));
        leftY += 36;

        WildfireButton jacketLeftBtn = new WildfireButton(
                3, leftColX, leftY, (columnWidth - 4) / 2, 15,
                I18n.format("wildfire_gender.uv_editor.selection.left_breast_overlay"));
        WildfireButton jacketRightBtn = new WildfireButton(
                4, leftColX + (columnWidth - 4) / 2 + 4, leftY, (columnWidth - 4) / 2, 15,
                I18n.format("wildfire_gender.uv_editor.selection.right_breast_overlay"));

        if (selectedBreastIndex == BreastTypes.LEFT) bodyLeftBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.RIGHT) bodyRightBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.LEFT_OVERLAY) jacketLeftBtn.enabled = false;
        if (selectedBreastIndex == BreastTypes.RIGHT_OVERLAY) jacketRightBtn.enabled = false;

        this.buttonList.add(bodyLeftBtn);
        this.buttonList.add(bodyRightBtn);
        this.buttonList.add(jacketLeftBtn);
        this.buttonList.add(jacketRightBtn);

        if (selectedDirection != null) {
            int editorStartY = 32 + 20;
            int rowHeight = 16;
            int labelW = 46;
            int btnW = 12;
            int gap = 6;

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
                (Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.getUniqueID() : null);

        switch (button.id) {
            case 0:
                if (uuid != null) {
                    UVStorage.resetToDefaults(uuid);
                    selectedUVs = UVStorage.loadForPlayer(uuid);
                }
                break;
            case 1:
                selectBreastUVMap(BreastTypes.LEFT);
                break;
            case 2:
                selectBreastUVMap(BreastTypes.RIGHT);
                break;
            case 3:
                selectBreastUVMap(BreastTypes.LEFT_OVERLAY);
                break;
            case 4:
                selectBreastUVMap(BreastTypes.RIGHT_OVERLAY);
                break;
            default:
                if (button.id >= 100 && button.id <= 107 && selectedDirection != null) {
                    int row = (button.id - 100) / 2;
                    boolean isAdd = ((button.id - 100) % 2) == 1;

                    UVQuad quad = selectedUVs.get(selectedDirection);
                    if (quad == null) return;
                    int delta = getIncrement();
                    int toAdd = isAdd ? delta : -delta;

                    if (row == 0) {
                        quad = quad.addX1(toAdd).addX2(toAdd);
                    } else if (row == 1) {
                        quad = quad.addY1(toAdd).addY2(toAdd);
                    } else if (row == 2) {
                        quad = quad.addX2(toAdd);
                    } else if (row == 3) {
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

        // Column panel backs
        int sidebarX = this.width - SIDEBAR_WIDTH;
        int leftColX = sidebarX + 5;
        int rightColX = sidebarX + SIDEBAR_WIDTH / 2 + 2;
        int columnWidth = SIDEBAR_WIDTH / 2 - 6 - 1;
        int panelTop = 30;
        int panelBottom = 128;
        drawRect(leftColX, panelTop, leftColX + columnWidth, panelBottom, COLOR_PANEL_BG);
        drawRect(rightColX, panelTop, rightColX + columnWidth, panelBottom + 36, COLOR_PANEL_BG);

        // Skin preview
        if (mc.player != null) {
            mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1f, 1f, 1f, 1f);
            drawTexturedModalRect(uvWindowX, uvWindowY, 0, 0, TEXTURE_DRAW_SIZE, TEXTURE_DRAW_SIZE);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        // Title
        this.drawCenteredString(this.fontRenderer,
                I18n.format("wildfire_gender.uv_editor"),
                this.width / 2, 20, 0xFFFFFF);

        // Draw colored face boxes and tooltips
        if (selectedUVs != null) {
            for (Map.Entry<UVDirection, UVQuad> entry : selectedUVs.getAllSides().entrySet()) {
                drawFaceBorderWithTooltip(entry.getKey(), entry.getValue(), mouseX, mouseY, selectedDirection != entry.getKey());
            }
        }

        GuiInventory.drawEntityOnScreen(previewCenterX,
                previewCenterY,
                MODEL_SCALE,
                -(mouseX - previewCenterX),
                -(mouseY - previewCenterY),
                mc.player);

        drawRightEditorPanel(rightColX, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        final int iconSize = 6;
        for (GuiButton b : this.buttonList) {
            if (b.id >= 100 && b.id <= 107) {
                boolean isAdd = (b.id % 2 == 1);
                ResourceLocation icon = isAdd ? ADD_ICON : SUB_ICON;

                mc.getTextureManager().bindTexture(icon);
                int drawX = b.x + (b.width - iconSize) / 2;
                int drawY = b.y + (b.height - iconSize) / 2;

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
            drawWrappedString(I18n.format("wildfire_gender.uv_editor.no_face_selected"),
                    rightColX + 10, 60, 70, COLOR_GREY);
            return;
        }

        String shortName = selectedDirection.getShortName();
        String fullName = fullFaceName(selectedDirection);
        String title = fullName + " (" + shortName + ")";
        this.fontRenderer.drawString(title, rightColX + 6, titleY, COLOR_YELLOW, false);

        String[] labels = {
                I18n.format("wildfire_gender.uv_editor.xpos"),
                I18n.format("wildfire_gender.uv_editor.ypos"),
                I18n.format("wildfire_gender.uv_editor.width"),
                I18n.format("wildfire_gender.uv_editor.height")
        };
        for (int i = 0; i < labels.length; i++) {
            int yRow = editorStartY + i * rowHeight;
            this.fontRenderer.drawString(labels[i], rightColX + 6, yRow, COLOR_WHITE, false);
        }

        int tipY = editorStartY + labels.length * rowHeight + 6;
        GlStateManager.pushMatrix();
        GlStateManager.translate(rightColX + 6, tipY, 0);
        GlStateManager.scale(0.75f, 0.75f, 1.0f);
        this.fontRenderer.drawString(
                I18n.format("wildfire_gender.uv_editor.increment_tip.line1"),
                0, 0, COLOR_CYAN, false);
        this.fontRenderer.drawString(
                I18n.format("wildfire_gender.uv_editor.increment_tip.line2"),
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
        int textWidth = this.fontRenderer.getStringWidth(faceName);
        int textHeight = this.fontRenderer.FONT_HEIGHT;

        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, 0);
        GlStateManager.scale(0.7f, 0.7f, 1.0f);
        this.fontRenderer.drawString(faceName, -textWidth / 2, -textHeight / 2, COLOR_WHITE, true);
        GlStateManager.popMatrix();

        if (mouseX >= rectX1 && mouseX <= rectX2 && mouseY >= rectY1 && mouseY <= rectY2) {
            String shortCode = direction.getShortName();
            String full = fullFaceName(direction) + " (" + shortCode + ")";
            String coords = "[" + quad.x1() + ", " + quad.y1() + ", " + quad.x2() + ", " + quad.y2() + "]";

            int pad = 6;
            int titleW = this.fontRenderer.getStringWidth(full);
            int coordsW = this.fontRenderer.getStringWidth(coords);
            int w = Math.max(titleW, coordsW) + pad * 2;
            int h = this.fontRenderer.FONT_HEIGHT + 4 + this.fontRenderer.FONT_HEIGHT; // two lines

            int tx = mouseX + 12;
            int ty = mouseY - 8;

            if (tx + w > this.width) tx = mouseX - 12 - w;
            if (ty + h > this.height) ty = this.height - h - 4;
            if (ty < 4) ty = 4;

            drawRect(tx, ty, tx + w, ty + h, TOOLTIP_BG);
            drawRect(tx, ty, tx + w, ty + 1, TOOLTIP_BORDER);
            drawRect(tx, ty + h - 1, tx + w, ty + h, TOOLTIP_BORDER);
            drawRect(tx, ty, tx + 1, ty + h, TOOLTIP_BORDER);
            drawRect(tx + w - 1, ty, tx + w, ty + h, TOOLTIP_BORDER);

            this.fontRenderer.drawString(full, tx + pad, ty + 1, COLOR_YELLOW, false);
            this.fontRenderer.drawString(coords, tx + pad, ty + 1 + this.fontRenderer.FONT_HEIGHT, COLOR_CYAN, false);
        }
    }

    private String fullFaceName(UVDirection dir) {
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
        String[] wrapped = new String[]{
                "Select a",
                "texture face",
                "on the left to",
                "edit it."
        };
        for (int i = 0; i < wrapped.length; i++) {
            this.fontRenderer.drawString(wrapped[i], x, y + i * 12, color, false);
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