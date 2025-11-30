package com.wildfire.main.uvs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

public class UVStorage {
    private static final String DIR = "config/wfg_uvs";

    public static UVLayout loadForPlayer(UUID uuid) {
        if (uuid == null) return UVLayout.defaultsForLargeFemale();
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();
        File f = new File(dir, uuid.toString() + ".uv");
        if (!f.exists()) return UVLayout.defaultsForLargeFemale();

        UVLayout layout = new UVLayout();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim().toLowerCase();
                String val = line.substring(eq + 1).trim();
                try {
                    UVQuad q = UVQuad.fromString(val);
                    switch (key) {
                        case "east": layout.put(UVDirection.EAST, q); break;
                        case "west": layout.put(UVDirection.WEST, q); break;
                        case "down": layout.put(UVDirection.DOWN, q); break;
                        case "up": layout.put(UVDirection.UP, q); break;
                        case "north": layout.put(UVDirection.NORTH, q); break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}
        return layout;
    }

    public static void saveForPlayer(UUID uuid, UVLayout layout) {
        if (uuid == null || layout == null) return;
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();
        File f = new File(dir, uuid.toString() + ".uv");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            for (UVDirection d : UVDirection.values()) {
                UVQuad q = layout.get(d);
                if (q != null) {
                    w.write(d.getSaveName() + "=" + q.toString());
                    w.newLine();
                }
            }
        } catch (IOException ignored) {}
    }

    public static void resetToDefaults(UUID uuid) {
        saveForPlayer(uuid, UVLayout.defaultsForLargeFemale());
    }

    /**
     * Minimal dynamic texture accessor.
     * Returns a ResourceLocation for a generated breast texture if present,
     * otherwise returns null so callers fall back to player skin.
     */
    public static ResourceLocation getBreastTexture(UUID uuid) {
        if (uuid == null) return null;

        File genDir = new File(DIR, "generated");
        if (!genDir.exists()) genDir.mkdirs();

        File png = new File(genDir, uuid.toString() + ".png");
        Minecraft mc = Minecraft.getMinecraft();

        try {
            if (png.exists()) {
                BufferedImage img = ImageIO.read(png);
                DynamicTexture dt = new DynamicTexture(img);
                return mc.getTextureManager().getDynamicTextureLocation("wfg_breast_" + uuid.toString(), dt);
            } else {
                // no generated texture yet
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate a breast texture PNG for the given player UUID using the provided UVLayout.
     * This is a basic stub that creates a transparent image; replace the body with actual compositing logic
     * that copies pixels from the player's skin according to UV quads.
     */
    public static void generateBreastTexture(UUID uuid, UVLayout layout) {
        if (uuid == null || layout == null) return;

        File genDir = new File(DIR, "generated");
        if (!genDir.exists()) genDir.mkdirs();

        File png = new File(genDir, uuid.toString() + ".png");

        try {
            // Create a transparent 64x64 image (replace with your desired size/compositing)
            BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

            // TODO: Composite pixels from the player's skin into img using layout.get(...) UV quads.
            // When implemented, write the composed image to disk so getBreastTexture can load it.
            ImageIO.write(img, "PNG", png);
        } catch (Exception ignored) {}
    }
}