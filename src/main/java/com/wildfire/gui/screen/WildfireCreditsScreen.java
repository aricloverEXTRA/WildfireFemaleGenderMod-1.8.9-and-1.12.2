package com.wildfire.gui.screen;

import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
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

    private static final int BOX_WIDTH = 52;
    private static final int BOX_HEIGHT = 68;
    private static final int H_SPACING = 8;
    private static final int V_SPACING = 8;

    public WildfireCreditsScreen() {
        Map<UUID, Contributor> map = Contributors.getContributors();
        List<FakeGUIPlayer> generals = new ArrayList<>();
        List<FakeGUIPlayer> translators = new ArrayList<>();

        // Hard-ordered GENERAL list
        List<UUID> orderedGeneral = Arrays.asList(
                UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946"), // WildfireFGM
                UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"), // celeste
                UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"), // pupnewfster
                UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"), // DiaDemiEmi
                UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"), // IzzyBizzy45
                UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"), // Kichura
                UUID.fromString("ad3cb52d-524b-41b4-b9d6-b91ec440811d"), // RacoonDog
                UUID.fromString("9a60e979-c890-4b43-a4c0-32d8a9f6b6b9"), // SavLeftUs
                UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b")  // Powerless001
        );

        Set<UUID> alreadyAdded = new HashSet<>();
        for (UUID id : orderedGeneral) {
            Contributor c = map.get(id);
            if (c == null) continue;
            if (!Boolean.TRUE.equals(c.showInCredits())) continue;
            if (c.getRole() == Contributor.Role.TRANSLATOR) continue;
            generals.add(new FakeGUIPlayer(c.name(), id));
            alreadyAdded.add(id);
        }

        // Any other GENERAL contributors not in ordered list
        for (Entry<UUID, Contributor> e : map.entrySet()) {
            UUID id = e.getKey();
            if (alreadyAdded.contains(id)) continue;
            Contributor c = e.getValue();
            if (c == null || !Boolean.TRUE.equals(c.showInCredits())) continue;
            if (c.getRole() == Contributor.Role.TRANSLATOR) continue;
            generals.add(new FakeGUIPlayer(c.name(), id));
        }

        // Hard-ordered TRANSLATOR list
        List<UUID> orderedTranslators = Arrays.asList(
                UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"), // ArcticWah
                UUID.fromString("4c3e3225-aec0-499c-b563-2b17cdb017f8"), // Betawolfy
                UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"), // Bluelight
                UUID.fromString("e31edb15-d8bd-44ac-8ec3-b54114e9d595"), // PinguinLars
                UUID.fromString("242c1a3a-83ee-4aa6-a3de-568cdac082a4")  // le0n_lol
        );

        Set<UUID> transAlready = new HashSet<>();
        for (UUID id : orderedTranslators) {
            Contributor c = map.get(id);
            if (c == null) continue;
            if (!Boolean.TRUE.equals(c.showInCredits())) continue;
            if (c.getRole() != Contributor.Role.TRANSLATOR) continue;
            translators.add(new FakeGUIPlayer(c.name(), id));
            transAlready.add(id);
        }

        // Any other translators not in ordered list
        for (Entry<UUID, Contributor> e : map.entrySet()) {
            UUID id = e.getKey();
            if (transAlready.contains(id)) continue;
            Contributor c = e.getValue();
            if (c == null || !Boolean.TRUE.equals(c.showInCredits())) continue;
            if (c.getRole() == Contributor.Role.TRANSLATOR) {
                translators.add(new FakeGUIPlayer(c.name(), id));
            }
        }

        genPlayers = generals.toArray(new FakeGUIPlayer[0]);
        transPlayers = translators.toArray(new FakeGUIPlayer[0]);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        navigationY = this.height / 2 + 82;

        btnBack = new WildfireButton(0, this.width / 2 - 25, navigationY + 6, 50, 13, StatCollector.translateToLocal("wildfire_gender.details.go_back"));
        btnPrev = new WildfireButton(1, this.width / 2 - 89, navigationY + 6, 60, 13, StatCollector.translateToLocal("wildfire_gender.details.prev_page"));
        btnNext = new WildfireButton(2, this.width / 2 + 29, navigationY + 6, 60, 13, StatCollector.translateToLocal("wildfire_gender.details.next_page"));
        btnGeneral = new WildfireButton(3, this.width / 2 - 89, navigationY + 34, 87, 13, StatCollector.translateToLocal("wildfire_gender.credits.general"));
        btnTranslators = new WildfireButton(4, this.width / 2 + 2, navigationY + 34, 87, 13, StatCollector.translateToLocal("wildfire_gender.credits.translators"));

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

        boolean isDarkMode = mc.thePlayer != null && GenderConfig.getDarkMode(mc.thePlayer);

        ResourceLocation btnContainer = isDarkMode ? DARK_BUTTON_CONTAINER : BUTTON_CONTAINER;
        ResourceLocation tabContainer = isDarkMode ? DARK_TAB_CONTAINER : TAB_CONTAINER;
        ResourceLocation creditContainer = isDarkMode ? DARK_CREDIT_CONTAINER : CREDIT_CONTAINER;
        ResourceLocation creditOutline = isDarkMode ? DARK_CREDIT_OUTLINE : CREDIT_OUTLINE;

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("wildfire_gender.credits.title"), width / 2, height / 2 - 100, 0xFFFFFF);
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("wildfire_gender.credits.description"), width / 2, height / 2 - 85, 0x888888);

        mc.getTextureManager().bindTexture(btnContainer);
        drawModalRectWithCustomSizedTexture(width / 2 - 95, navigationY, 0, 0, 190, 25, 190, 25);
        mc.getTextureManager().bindTexture(tabContainer);
        drawModalRectWithCustomSizedTexture(width / 2 - 95, navigationY + 28, 0, 0, 190, 25, 190, 25);

        FakeGUIPlayer[] active = getActivePlayers();
        int startIndex = creditsPage * BOXES_PER_PAGE;
        int endIndex = Math.min(startIndex + BOXES_PER_PAGE, active.length);

        int rows = (int) Math.ceil((double) Math.max(1, endIndex - startIndex) / COLUMNS);
        int totalHeight = rows * BOX_HEIGHT + (rows - 1) * V_SPACING;
        int startY = this.height / 2 - (totalHeight / 2) + 4;

        Map<UUID, Contributor> contribMap = Contributors.getContributors();

        for (int i = startIndex; i < endIndex; i++) {
            FakeGUIPlayer fp = active[i];
            int local = i - startIndex;
            int col = local % COLUMNS;
            int row = local / COLUMNS;

            int remainingInRow = Math.min(endIndex - startIndex - row * COLUMNS, COLUMNS);
            int rowWidth = remainingInRow * BOX_WIDTH + (remainingInRow - 1) * H_SPACING;
            int startX = (this.width / 2) - (rowWidth / 2);

            int cx = startX + col * (BOX_WIDTH + H_SPACING);
            int cy = startY + row * (BOX_HEIGHT + V_SPACING);

            this.mc.getTextureManager().bindTexture(creditContainer);
            drawModalRectWithCustomSizedTexture(cx, cy, 0, 0, BOX_WIDTH, BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);

            Contributor found = null;
            UUID uuid = fp.getUUID();
            if (uuid != null) {
                found = contribMap.get(uuid);
            }
            if (found == null) {
                for (Entry<UUID, Contributor> e : contribMap.entrySet()) {
                    if (e.getValue().name().equals(fp.getName())) {
                        found = e.getValue();
                        break;
                    }
                }
            }

            int outlineColor = 0xFFFFFFFF;
            if (found != null) {
                outlineColor = found.getColor();
            }

            float r = ((outlineColor >> 16) & 0xFF) / 255.0F;
            float g = ((outlineColor >> 8) & 0xFF) / 255.0F;
            float b = (outlineColor & 0xFF) / 255.0F;

            GL11.glColor4f(r, g, b, 1.0F);
            this.mc.getTextureManager().bindTexture(creditOutline);
            drawModalRectWithCustomSizedTexture(cx + 3, cy + 3, 0, 0, 46, 53, 46, 53);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

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
            int drawCenterY = portraitY + (int) (portraitH * 0.80f) + PORTRAIT_PLAYER_DOWN_PX + 8;
            int modelScale = 38;

            int mouseOffsetX = mouseX - drawCenterX;
            int mouseOffsetY = mouseY - drawCenterY;

            EntityLivingBase entity = fp.getEntity();
            GuiUtils.drawEntityOnScreenNoScissor(this, drawCenterX, drawCenterY, 38, mouseOffsetX, mouseOffsetY, entity);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            int nameDrawX = cx + (BOX_WIDTH / 2);
            int nameDrawY = cy + 55;

            GL11.glPushMatrix();
            GL11.glTranslatef(nameDrawX, nameDrawY, 0f);
            GL11.glScalef(0.55f, 0.55f, 1.0f);
            GL11.glTranslatef(-nameDrawX, -nameDrawY, 0f);
            drawCenteredString(this.fontRendererObj, fp.getName(), nameDrawX, nameDrawY + 7, 0xFFFFFF);
            GL11.glPopMatrix();

            String name = fp.getName();
            int textWidthUnscaled = this.fontRendererObj.getStringWidth(name);
            int scaledTextWidth = (int) (textWidthUnscaled * 0.55f);
            int textLeft = nameDrawX - (scaledTextWidth / 2);
            int textRight = nameDrawX + (scaledTextWidth / 2);
            int textTop = nameDrawY + 7;
            int textHeight = (int) (9 * 0.55f);
            int textBottom = textTop + textHeight;

            if (mouseX >= textLeft && mouseX <= textRight && mouseY >= textTop && mouseY <= textBottom) {
                List<String> tooltip = new ArrayList<>();
                String roleText = StatCollector.translateToLocal("wildfire_gender.contributor.role.generic.short");
                int roleColor = 0xFFFFFF;

                if (found != null) {
                    // manual text overrides for celeste + pupnewfster
                    UUID id = uuid;
                    if (id != null) {
                        String idStr = id.toString();
                        if (idStr.equals("70336328-0de7-430e-8cba-2779e2a05ab5")) { // celeste
                            roleText = "Maintainer (Fabric)";
                        } else if (idStr.equals("64e57307-72e5-4f43-be9c-181e8e35cc9b")) { // pupnewfster
                            roleText = "Maintainer (NeoForge)";
                        } else {
                            try {
                                roleText = StatCollector.translateToLocal(found.getRole().shortNameKey());
                            } catch (Throwable ignored) {}
                        }
                    }

                    roleColor = found.getColor();

                    if (found.getDescription() != null && !found.getDescription().isEmpty()) {
                        tooltip.add(Contributor.getLegacyColorCode(roleColor) + found.getDescription());
                    }
                }

                tooltip.add(Contributor.getLegacyColorCode(roleColor) + roleText + " - " + fp.getName());
                drawHoveringText(tooltip, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}