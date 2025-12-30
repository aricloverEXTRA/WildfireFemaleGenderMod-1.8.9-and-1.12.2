package com.wildfire.gui;

import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SyncedPlayerList {
    private static List<SyncedEntry> entries = new ArrayList<>();

    static {
        MinecraftForge.EVENT_BUS.register(new SyncedPlayerList());
    }

    private SyncedPlayerList() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) {
            entries = new ArrayList<>();
            return;
        }

        List<SyncedEntry> newList = new ArrayList<>();
        Map<UUID, Contributor> contribs = Contributors.getContributors();

        for (EntityPlayer p : mc.world.playerEntities) {
            if (p.getUniqueID().equals(mc.player.getUniqueID())) continue;

            Contributor c = contribs.get(p.getUniqueID());
            int color = 0xFFFFFF;
            if (c != null) {
                color = c.getColor();
            }

            String gender = "Unknown";
            try {
                GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(p);
                if (settings != null) {
                    gender = settings.gender;
                }
            } catch (Exception ignored) {}

            newList.add(new SyncedEntry(p.getName(), color, gender));
            if (newList.size() >= 40) break;
        }
        entries = newList;
    }

    public static void drawSyncedPlayers(FontRenderer fontRenderer) {
        if (entries.isEmpty()) return;
        int y = 10;
        fontRenderer.drawString("Players:", 5, y, 0xFFFFFF);
        y += 12;
        for (SyncedEntry e : entries) {
            fontRenderer.drawString(e.name + " - " + e.gender, 10, y, e.color);
            y += 10;
        }
    }

    private static class SyncedEntry {
        final String name;
        final int color;
        final String gender;
        SyncedEntry(String name, int color, String gender) {
            this.name = name;
            this.color = color;
            this.gender = gender;
        }
    }
}