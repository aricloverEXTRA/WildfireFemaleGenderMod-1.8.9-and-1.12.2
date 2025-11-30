package com.wildfire.gui;

import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * SyncedPlayerList - minimal Forge 1.8.9 implementation.
 * Shows players currently in the world (local / server players) as a best-effort substitute
 * for the modern cloud-synced player list.
 */
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
        if (mc.thePlayer == null || mc.theWorld == null) {
            entries = new ArrayList<>();
            return;
        }

        List<SyncedEntry> newList = new ArrayList<>();
        for (Object o : mc.theWorld.playerEntities) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) o;
            if (p.getUniqueID().equals(mc.thePlayer.getUniqueID())) continue;
            Contributor.Role role = Contributors.getContributors().getOrDefault(p.getUniqueID(), null) != null ? Contributors.getContributors().get(p.getUniqueID()).getRole() : null;
            int color = 0xFFFFFF;
            if (role != null) {
                // simple mapping for role colors: creators -> light purple, translators -> light blue, developers -> gold
                switch (role) {
                    case MOD_CREATOR: color = 0xFF99FF; break;
                    case TRANSLATOR: color = 0x66CCFF; break;
                    case DEVELOPER: color = 0xFFD700; break;
                    default: color = 0xFFFFFF; break;
                }
            }
            String gender = "Male";
            // attempt to fetch local stored gender for the player (if available)
            try {
                // best-effort: GenderConfig only supports local player in this 1.8.9 port, so we can't read remote players
                gender = "Unknown";
            } catch (Exception ignored) {}
            newList.add(new SyncedEntry(p.getName(), color, gender));
            if (newList.size() >= 40) break;
        }
        entries = newList;
    }

    public static void drawSyncedPlayers(FontRenderer fontRenderer) {
        if (entries.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        int y = 10;
        mc.fontRendererObj.drawString("Players:", 5, y, 0xFFFFFF);
        y += 12;
        for (SyncedEntry e : entries) {
            mc.fontRendererObj.drawString(e.name + " - " + e.gender, 10, y, e.color);
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