package com.wildfire.gui;

import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.contributors.Contributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

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
        for (EntityPlayer p : mc.world.playerEntities) {
            if (p.getUniqueID().equals(mc.player.getUniqueID())) continue;
            Contributor.Role role = null;
            Contributor c = Contributors.getContributors().get(p.getUniqueID());
            if (c != null) role = c.getRole();

            int color = 0xFFFFFF;
            if (role != null) {
                switch (role) {
                    case MOD_CREATOR: color = 0xFF99FF; break;
                    case TRANSLATOR:  color = 0x66CCFF; break;
                    case DEVELOPER:   color = 0xFFD700; break;
                    default:          color = 0xFFFFFF; break;
                }
            }

            String gender = "Unknown"; // remote player gender not tracked locally
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
        SyncedEntry(String name, int color, String gender) { this.name = name; this.color = color; this.gender = gender; }
    }
}