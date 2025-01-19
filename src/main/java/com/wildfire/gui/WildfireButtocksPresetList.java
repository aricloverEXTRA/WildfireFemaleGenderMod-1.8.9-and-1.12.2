/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.gui;

import com.wildfire.gui.screen.WildfireButtocksCustomizationScreen;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.ButtocksPresetConfiguration;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class WildfireButtocksPresetList extends EntryListWidget<WildfireButtocksPresetList.Entry> {

    public boolean active = true;
    public boolean visible = true;

    public class ButtocksPresetListEntry {

        public Identifier ident;
        public String name;
        private ButtocksPresetConfiguration data;

        public ButtocksPresetListEntry(String name, ButtocksPresetConfiguration data) {
            this.name = name;
            this.data = data;
            this.ident = Identifier.of(WildfireGender.MODID, "textures/presets/iknowthisisnull.png");
        }

    }

    private ButtocksPresetListEntry[] BUTTOCKS_PRESETS = new ButtocksPresetListEntry[] {

    };
    private static final Identifier TXTR_SYNC = Identifier.of(WildfireGender.MODID, "textures/sync.png");
    private static final Identifier TXTR_UNKNOWN = Identifier.of(WildfireGender.MODID, "textures/unknown.png");
    private static final Identifier TXTR_CACHED = Identifier.of(WildfireGender.MODID, "textures/cached.png");
    private final int listWidth;
    private final WildfireButtocksCustomizationScreen parent;

    public WildfireButtocksPresetList(WildfireButtocksCustomizationScreen parent, int listWidth, int top) {
        super(MinecraftClient.getInstance(), 156, parent.height, top, 32);
        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
    }

    public ButtocksPresetListEntry[] getPresetList() {
        return BUTTOCKS_PRESETS;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {}

    @Override
    protected void drawMenuListBackground(DrawContext context) {}

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = this.getRowLeft();
        int width = this.getRowWidth();
        int count = this.getEntryCount();

        for(int index = 0; index < count; ++index) {
            int top = this.getRowTop(index);
            int bottom = this.getRowBottom(index);
            if(bottom >= this.getY() && top <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, index, left, top, width, itemHeight);
            }
        }
    }

    @Override
    public int getRowTop(int index) {
        return this.getY() - (int)this.getScrollY() + index * this.itemHeight + this.headerHeight;
    }

    @Override
    protected int getScrollbarX() {
        return parent.width / 2 + 181;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();

        ButtocksPresetConfiguration[] CONFIGS = ButtocksPresetConfiguration.getButtocksPresetConfigurationFiles();
        ArrayList<ButtocksPresetListEntry> tmpPresets = new ArrayList<>();
        for(ButtocksPresetConfiguration presetCfg : CONFIGS) {
            System.out.println("Preset Name: " + presetCfg.get(ButtocksPresetConfiguration.PRESET_NAME));
            tmpPresets.add(new ButtocksPresetListEntry(presetCfg.get(ButtocksPresetConfiguration.PRESET_NAME), presetCfg));
        }
        BUTTOCKS_PRESETS = tmpPresets.toArray(ButtocksPresetListEntry[]::new);

        if(this.client.world == null || this.client.player == null) return;

        for(ButtocksPresetListEntry buttocksPreset : BUTTOCKS_PRESETS) {
            addEntry(new Entry(buttocksPreset));
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Environment(EnvType.CLIENT)
    public class Entry extends EntryListWidget.Entry<WildfireButtocksPresetList.Entry> {
        private final Identifier thumbnail;
        public final ButtocksPresetListEntry nInfo;
        private final WildfireButton btnOpenGUI;

        private Entry(final ButtocksPresetListEntry nInfo) {
            this.nInfo = nInfo;
            this.thumbnail = nInfo.ident;
            btnOpenGUI = new WildfireButton(0, 0, getRowWidth() - 6, itemHeight, Text.empty(), button -> {
                PlayerConfig plr = Objects.requireNonNull(parent.getPlayer(), "getPlayer()");
                plr.updateButtocksSize(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_SIZE));
                plr.getButtocks().updateXOffset(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_OFFSET_X));
                plr.getButtocks().updateYOffset(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_OFFSET_Y));
                plr.getButtocks().updateZOffset(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_OFFSET_Z));
                plr.getButtocks().updateCleavage(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_CLEAVAGE));
                plr.getButtocks().updateUnibutt(nInfo.data.get(ButtocksPresetConfiguration.BUTTOCKS_UNIBUTT));
                PlayerConfig.saveGenderInfo(plr);
            });
        }

        @Override
        public void render(DrawContext ctx, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            if(!visible) return;

            btnOpenGUI.active = WildfireButtocksPresetList.this.active;
            TextRenderer font = MinecraftClient.getInstance().textRenderer;

            ctx.drawTexture(RenderLayer::getGuiTextured, thumbnail, x + 2, y + 2, 0, 0, 28, 28, 28, 28);

            ctx.drawText(font, Text.of(nInfo.name), x + 34, y + 4, 0xFFFFFFFF, false);
            this.btnOpenGUI.setX(x);
            this.btnOpenGUI.setY(y);
            this.btnOpenGUI.render(ctx, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(active && visible) {
                if (this.btnOpenGUI.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
    }
}

