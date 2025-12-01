package com.wildfire.gui.screen;

import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WildfireCreditsScreen extends GuiScreen {
    private static final int BOXES_PER_PAGE = 12;
    private static final int COLUMNS = 6;

    private static final ResourceLocation CREDIT_CONTAINER = new ResourceLocation("wildfire_gender:textures/gui/credits/credit_container.png");
    private static final ResourceLocation CREDIT_OUTLINE = new ResourceLocation("wildfire_gender:textures/gui/credits/credit_outline.png");
    private static final ResourceLocation BUTTON_CONTAINER = new ResourceLocation("wildfire_gender:textures/gui/credits/button_container.png");
    private static final ResourceLocation TAB_CONTAINER = new ResourceLocation("wildfire_gender:textures/gui/credits/tab_container.png");

    private static final ResourceLocation DARK_CREDIT_CONTAINER = new ResourceLocation("wildfire_gender:textures/darkmode/gui/credits/credit_container.png");
    private static final ResourceLocation DARK_CREDIT_OUTLINE = new ResourceLocation("wildfire_gender:textures/darkmode/gui/credits/credit_outline.png");
    private static final ResourceLocation DARK_BUTTON_CONTAINER = new ResourceLocation("wildfire_gender:textures/darkmode/gui/credits/button_container.png");
    private static final ResourceLocation DARK_TAB_CONTAINER = new ResourceLocation("wildfire_gender:textures/darkmode/gui/credits/tab_container.png");

    private enum Category { GENERAL, TRANSLATORS }
    private Category categoryTab = Category.GENERAL;
    private int creditsPage = 0;

    private WildfireButton btnBack, btnPrev, btnNext, btnGeneral, btnTranslators;
    private int navigationY;

    private FakeGUIPlayer[] genPlayers = new FakeGUIPlayer[0];
    private FakeGUIPlayer[] transPlayers = new FakeGUIPlayer[0];
    private static final int PORTRAIT_PLAYER_DOWN_PX = 20;

    public WildfireCreditsScreen() {
        Map<UUID, Contributor> map = Contributors.getContributors();
        List<FakeGUIPlayer> generals = new ArrayList<>();
        List<FakeGUIPlayer> translators = new ArrayList<>();

        for (Map.Entry<UUID, Contributor> e : map.entrySet()) {
            Contributor c = e.getValue();
            if (c == null || !Boolean.TRUE.equals(c.showInCredits())) continue;
            FakeGUIPlayer p = new FakeGUIPlayer(c.name(), e.getKey());
            if (c.getRole() == Contributor.Role.TRANSLATOR) translators.add(p);
            else generals.add(p);
        }

        genPlayers = generals.toArray(new FakeGUIPlayer[0]);
        transPlayers = translators.toArray(new FakeGUIPlayer[0]);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        navigationY = this.height / 2 + 82;

        btnBack = new WildfireButton(0, this.width / 2 - 25, navigationY + 6, 50, 13,
                I18n.format("wildfire_gender.details.go_back"));
        btnPrev = new WildfireButton(1, this.width / 2 - 89, navigationY + 6, 60, 13,
                I18n.format("wildfire_gender.details.prev_page"));
        btnNext = new WildfireButton(2, this.width / 2 + 29, navigationY + 6, 60, 13,
                I18n.format("wildfire_gender.details.next_page"));
        btnGeneral = new WildfireButton(3, this.width / 2 - 89, navigationY + 34, 87, 13,
                I18n.format("wildfire_gender.credits.general"));
        btnTranslators = new WildfireButton(4, this.width / 2 + 2, navigationY + 34, 87, 13,
                I18n.format("wildfire_gender.credits.translators"));

        updateButtonState();

        this.buttonList.add(btnBack);
        this.buttonList.add(btnPrev);
        this.buttonList.add(btnNext);
        this.buttonList.add(btnGeneral);
        this.buttonList.add(btnTranslators);
    }

    private void updateButtonState() {
        btnPrev.enabled = creditsPage > 0;
        btnNext.enabled = creditsPage < getTotalPages() - 1;
        btnGeneral.enabled = categoryTab != Category.GENERAL;
        btnTranslators.enabled = categoryTab != Category.TRANSLATORS;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!(button instanceof WildfireButton)) return;
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(new GuiWardrobe());
                return;
            case 1:
                if (creditsPage > 0) creditsPage--;
                break;
            case 2:
                if (creditsPage < getTotalPages() - 1) creditsPage++;
                break;
            case 3:
                categoryTab = Category.GENERAL;
                creditsPage = 0;
                break;
            case 4:
                categoryTab = Category.TRANSLATORS;
                creditsPage = 0;
                break;
            default:
                break;
        }
        updateButtonState();
    }

    private FakeGUIPlayer[] getActivePlayers() {
        return categoryTab == Category.TRANSLATORS ? transPlayers : genPlayers;
    }

    private int getTotalPages() {
        FakeGUIPlayer[] arr = getActivePlayers();
        if (arr.length == 0) return 1;
        return (int) Math.ceil((double) arr.length / BOXES_PER_PAGE);
    }

    @Override
    public void updateScreen() {
        for (FakeGUIPlayer p : getActivePlayers()) {
            p.tick();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        boolean isDarkMode = mc.player != null && GenderConfig.getDarkMode(mc.player);

        ResourceLocation btnContainer = isDarkMode ? DARK_BUTTON_CONTAINER : BUTTON_CONTAINER;
        ResourceLocation tabContainer = isDarkMode ? DARK_TAB_CONTAINER : TAB_CONTAINER;
        ResourceLocation creditContainer = isDarkMode ? DARK_CREDIT_CONTAINER : CREDIT_CONTAINER;
        ResourceLocation creditOutline = isDarkMode ? DARK_CREDIT_OUTLINE : CREDIT_OUTLINE;

        drawCenteredString(fontRenderer, I18n.format("wildfire_gender.credits.title"),
                width / 2, height / 2 - 100, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("wildfire_gender.credits.description"),
                width / 2, height / 2 - 85, 0x888888);

        mc.getTextureManager().bindTexture(btnContainer);
        drawModalRectWithCustomSizedTexture(width / 2 - 95, navigationY, 0, 0, 190, 25, 190, 25);
        mc.getTextureManager().bindTexture(tabContainer);
        drawModalRectWithCustomSizedTexture(width / 2 - 95, navigationY + 28, 0, 0, 190, 25, 190, 25);

        final int boxW = 52;
        final int boxH = 68;
        FakeGUIPlayer[] active = getActivePlayers();
        int startIndex = creditsPage * BOXES_PER_PAGE;
        int endIndex = Math.min(startIndex + BOXES_PER_PAGE, active.length);

        int rows = (int) Math.ceil((double) Math.max(1, endIndex - startIndex) / COLUMNS);
        int startY = this.height / 2 - (rows * boxH) / 2 + 4;

        for (int i = startIndex; i < endIndex; i++) {
            FakeGUIPlayer fp = active[i];
            int local = i - startIndex;
            int col = local % COLUMNS;
            int row = local / COLUMNS;

            int remainingInRow = Math.min(endIndex - startIndex - row * COLUMNS, COLUMNS);
            int rowWidth = remainingInRow * boxW;
            int startX = (this.width / 2) - (rowWidth / 2) + 4;

            int cx = startX + (col * boxW);
            int cy = startY + (row * boxH);

            this.mc.getTextureManager().bindTexture(creditContainer);
            drawModalRectWithCustomSizedTexture(cx, cy, 0, 0, 52, 68, 52, 68);

            this.mc.getTextureManager().bindTexture(creditOutline);
            drawModalRectWithCustomSizedTexture(cx + 3, cy + 3, 0, 0, 46, 53, 46, 53);

            final int portraitX = cx + 3;
            final int portraitY = cy + 3;
            final int portraitW = 46;
            final int portraitH = 53;

            int sx = portraitX * this.mc.displayWidth / this.width;
            int sy = this.mc.displayHeight - (portraitY + portraitH) * this.mc.displayHeight / this.height;
            int sw = portraitW * this.mc.displayWidth / this.width;
            int sh = portraitH * this.mc.displayHeight / this.height;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(sx, sy, sw, sh);

            int drawCenterX = portraitX + portraitW / 2;
            int drawCenterY = portraitY + (int) (portraitH * 0.80f) + PORTRAIT_PLAYER_DOWN_PX;

            int mouseOffsetX = mouseX - drawCenterX;
            int mouseOffsetY = mouseY - drawCenterY;

            EntityLivingBase entity = fp.getEntity();
            GuiUtils.drawEntityOnScreenNoScissor(this, drawCenterX, drawCenterY, 40, mouseOffsetX, mouseOffsetY, entity);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            int nameDrawX = cx + (boxW / 2);
            int nameDrawY = cy + 55 - 54;
            GL11.glPushMatrix();
            GL11.glTranslatef(nameDrawX, nameDrawY, 0f);
            GL11.glScalef(0.55f, 0.55f, 1.0f);
            GL11.glTranslatef(-nameDrawX, -nameDrawY, 0f);
            drawCenteredString(this.fontRenderer, fp.getName(), nameDrawX, nameDrawY + 7, 0xFFFFFF);
            GL11.glPopMatrix();

            String name = fp.getName();
            int textWidthUnscaled = this.fontRenderer.getStringWidth(name);
            int scaledTextWidth = (int) (textWidthUnscaled * 0.55f);
            int textLeft = nameDrawX - (scaledTextWidth / 2);
            int textRight = nameDrawX + (scaledTextWidth / 2);
            int textTop = nameDrawY + 7;
            int textHeight = (int) (9 * 0.55f);
            int textBottom = textTop + textHeight;

            if (mouseX >= textLeft && mouseX <= textRight && mouseY >= textTop && mouseY <= textBottom) {
                List<String> tooltip = new ArrayList<>();
                String roleText = I18n.format("wildfire_gender.contributor.role.generic.short");
                Contributor found = null;
                for (Map.Entry<UUID, Contributor> e : Contributors.getContributors().entrySet()) {
                    if (e.getValue().name().equals(fp.getName())) {
                        found = e.getValue();
                        break;
                    }
                }
                if (found != null) {
                    try {
                        roleText = I18n.format(found.getRole().shortNameKey());
                    } catch (Throwable ignored) {}
                    if (found.getDescription() != null && !found.getDescription().isEmpty()) {
                        tooltip.add(found.getDescription());
                    }
                }
                tooltip.add(roleText + " - " + fp.getName());
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}