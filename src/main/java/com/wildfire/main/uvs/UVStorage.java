package com.wildfire.main.uvs;

import com.wildfire.main.ArmorTextureHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UVStorage - inclusive-coordinate aware compositor for Forge 1.8.9.
 *
 * Key points:
 * - UVQuad coordinates are treated as inclusive endpoints (width = x2 - x1 + 1).
 * - Overlay generation prefers the local player's armor texture (if present) and falls back to skin.
 * - Generates two 64x64 PNGs under config/wfg_uvs/generated/<uuid>_base.png and _overlay.png
 *   and registers dynamic textures via Minecraft's texture manager.
 */
public final class UVStorage {
    private static final String DIR = "config/wfg_uvs";
    private static final String GEN_DIR = DIR + File.separator + "generated";

    private static final UVLayout LEFT_BASE_DEST;
    private static final UVLayout RIGHT_BASE_DEST;
    private static final UVLayout LEFT_OVERLAY_DEST;
    private static final UVLayout RIGHT_OVERLAY_DEST;

    private static final ConcurrentHashMap<UUID, ResourceLocation> RUNTIME_BASE = new ConcurrentHashMap<UUID, ResourceLocation>();
    private static final ConcurrentHashMap<UUID, ResourceLocation> RUNTIME_OVERLAY = new ConcurrentHashMap<UUID, ResourceLocation>();

    static {
        LEFT_BASE_DEST = new UVLayout();
        LEFT_BASE_DEST.put(UVDirection.EAST, new UVQuad(24, 21, 27, 26));
        LEFT_BASE_DEST.put(UVDirection.WEST, new UVQuad(16, 21, 19, 26));
        LEFT_BASE_DEST.put(UVDirection.DOWN, new UVQuad(20, 17, 23, 20));
        LEFT_BASE_DEST.put(UVDirection.UP, new UVQuad(20, 25, 23, 26));
        LEFT_BASE_DEST.put(UVDirection.NORTH, new UVQuad(20, 21, 23, 26));

        RIGHT_BASE_DEST = new UVLayout();
        RIGHT_BASE_DEST.put(UVDirection.EAST, new UVQuad(28, 21, 31, 26));
        RIGHT_BASE_DEST.put(UVDirection.WEST, new UVQuad(21, 21, 23, 26));
        RIGHT_BASE_DEST.put(UVDirection.DOWN, new UVQuad(24, 17, 27, 20));
        RIGHT_BASE_DEST.put(UVDirection.UP, new UVQuad(24, 25, 27, 26));
        RIGHT_BASE_DEST.put(UVDirection.NORTH, new UVQuad(24, 21, 27, 26));

        LEFT_OVERLAY_DEST = new UVLayout();
        LEFT_OVERLAY_DEST.put(UVDirection.EAST, new UVQuad(0, 0, 0, 0));
        LEFT_OVERLAY_DEST.put(UVDirection.WEST, new UVQuad(17, 37, 20, 42));
        LEFT_OVERLAY_DEST.put(UVDirection.DOWN, new UVQuad(20, 34, 23, 36));
        LEFT_OVERLAY_DEST.put(UVDirection.UP, new UVQuad(20, 42, 23, 44));
        LEFT_OVERLAY_DEST.put(UVDirection.NORTH, new UVQuad(20, 37, 23, 41));

        RIGHT_OVERLAY_DEST = new UVLayout();
        RIGHT_OVERLAY_DEST.put(UVDirection.EAST, new UVQuad(28, 37, 31, 42));
        RIGHT_OVERLAY_DEST.put(UVDirection.WEST, new UVQuad(0, 0, 0, 0));
        RIGHT_OVERLAY_DEST.put(UVDirection.DOWN, new UVQuad(24, 34, 27, 36));
        RIGHT_OVERLAY_DEST.put(UVDirection.UP, new UVQuad(24, 42, 27, 44));
        RIGHT_OVERLAY_DEST.put(UVDirection.NORTH, new UVQuad(24, 37, 27, 41));
    }

    private UVStorage() {}

    public static UVLayout loadLayout(UUID uuid, BreastTypes type) {
        UVBundle bundle = loadAll(uuid);
        if (bundle == null) return UVLayout.defaultsForLargeFemale();
        if (type == BreastTypes.LEFT) return bundle.leftBase.copy();
        if (type == BreastTypes.RIGHT) return bundle.rightBase.copy();
        if (type == BreastTypes.LEFT_OVERLAY) return bundle.leftOverlay.copy();
        return bundle.rightOverlay.copy();
    }

    public static void saveLayout(UUID uuid, BreastTypes type, UVLayout layout) {
        UVBundle bundle = loadAll(uuid);
        if (bundle == null) bundle = new UVBundle();
        if (type == BreastTypes.LEFT) bundle.leftBase = layout.copy();
        else if (type == BreastTypes.RIGHT) bundle.rightBase = layout.copy();
        else if (type == BreastTypes.LEFT_OVERLAY) bundle.leftOverlay = layout.copy();
        else bundle.rightOverlay = layout.copy();
        saveBundle(uuid, bundle);
        generateBreastTextures(uuid, bundle);
        applyToRuntime(uuid, bundle);
    }

    private static class UVBundle {
        UVLayout leftBase = UVLayout.defaultsForLargeFemale();
        UVLayout rightBase = RIGHT_BASE_DEST.copy();
        UVLayout leftOverlay = LEFT_OVERLAY_DEST.copy();
        UVLayout rightOverlay = RIGHT_OVERLAY_DEST.copy();
    }

    private static UVBundle loadAll(UUID uuid) {
        if (uuid == null) return null;
        try {
            File baseDir = new File(DIR);
            if (!baseDir.exists()) baseDir.mkdirs();
            File f = new File(baseDir, uuid.toString() + ".uv");
            if (!f.exists()) return new UVBundle();

            UVBundle bundle = new UVBundle();
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = line.substring(eq + 1).trim();
                try {
                    UVQuad q = UVQuad.fromString(val);
                    String[] parts = key.split("\\.");
                    if (parts.length == 1) {
                        UVDirection faceDir = dirFromName(parts[0]);
                        if (faceDir != null) {
                            bundle.leftBase.put(faceDir, q);
                            bundle.rightBase.put(faceDir, q);
                            bundle.leftOverlay.put(faceDir, q);
                            bundle.rightOverlay.put(faceDir, q);
                        }
                    } else if (parts.length == 2) {
                        String side = parts[0];
                        String face = parts[1];
                        UVDirection faceDir = dirFromName(face);
                        if (faceDir == null) continue;
                        if ("left".equals(side)) bundle.leftBase.put(faceDir, q);
                        else if ("right".equals(side)) bundle.rightBase.put(faceDir, q);
                        else if ("left_overlay".equals(side) || "leftoverlay".equals(side)) bundle.leftOverlay.put(faceDir, q);
                        else if ("right_overlay".equals(side) || "rightoverlay".equals(side)) bundle.rightOverlay.put(faceDir, q);
                    }
                } catch (NumberFormatException ignored) { }
            }
            reader.close();
            return bundle;
        } catch (IOException e) {
            e.printStackTrace();
            return new UVBundle();
        }
    }

    private static void saveBundle(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        try {
            File baseDir = new File(DIR);
            if (!baseDir.exists()) baseDir.mkdirs();
            File f = new File(baseDir, uuid.toString() + ".uv");
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            writeLayout(w, "left", bundle.leftBase);
            writeLayout(w, "right", bundle.rightBase);
            writeLayout(w, "left_overlay", bundle.leftOverlay);
            writeLayout(w, "right_overlay", bundle.rightOverlay);
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLayout(BufferedWriter w, String prefix, UVLayout layout) throws IOException {
        for (java.util.Map.Entry<UVDirection, UVQuad> e : layout.getAllSides().entrySet()) {
            UVQuad q = e.getValue();
            if (q != null) {
                w.write(prefix + "." + e.getKey().getSaveName() + "=" + q.toString());
                w.newLine();
            }
        }
    }

    private static UVDirection dirFromName(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if ("east".equals(name)) return UVDirection.EAST;
        if ("west".equals(name)) return UVDirection.WEST;
        if ("down".equals(name)) return UVDirection.DOWN;
        if ("up".equals(name)) return UVDirection.UP;
        if ("north".equals(name)) return UVDirection.NORTH;
        return null;
    }

    private static void applyToRuntime(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        try {
            Class<?> wf = Class.forName("com.wildfire.main.WildfireGender");
            java.lang.reflect.Method m = wf.getMethod("getOrAddPlayerById", UUID.class);
            Object playerCfg = m.invoke(null, uuid);
            if (playerCfg != null) {
                try {
                    java.lang.reflect.Method setLeft = playerCfg.getClass().getMethod("updateLeftBreastUVLayout", UVLayout.class);
                    java.lang.reflect.Method setRight = playerCfg.getClass().getMethod("updateRightBreastUVLayout", UVLayout.class);
                    setLeft.invoke(playerCfg, bundle.leftBase);
                    setRight.invoke(playerCfg, bundle.rightBase);
                } catch (Throwable ignored) {}
                try {
                    java.lang.reflect.Method setLeftO = playerCfg.getClass().getMethod("updateLeftBreastOverlayUVLayout", UVLayout.class);
                    java.lang.reflect.Method setRightO = playerCfg.getClass().getMethod("updateRightBreastOverlayUVLayout", UVLayout.class);
                    setLeftO.invoke(playerCfg, bundle.leftOverlay);
                    setRightO.invoke(playerCfg, bundle.rightOverlay);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        try {
            com.wildfire.main.entitydata.EntityConfig cfg = com.wildfire.main.entitydata.EntityConfig.CACHE.getUnchecked(uuid);
            if (cfg != null) {
                cfg.updateLeftBreastUVLayout(bundle.leftBase);
                cfg.updateRightBreastUVLayout(bundle.rightBase);
                cfg.updateLeftBreastOverlayUVLayout(bundle.leftOverlay);
                cfg.updateRightBreastOverlayUVLayout(bundle.rightOverlay);
            }
        } catch (Throwable ignored) {}
    }

    public static ResourceLocation getBreastTexture(UUID uuid, boolean overlay) {
        if (uuid == null) return null;
        return overlay ? RUNTIME_OVERLAY.get(uuid) : RUNTIME_BASE.get(uuid);
    }

    public static void generateBreastTextures(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        try {
            BufferedImage skin = loadPlayerSkin(uuid);
            if (skin == null) return;

            BufferedImage armorImg = null;
            try {
                ResourceLocation armorRL = ArmorTextureHelper.getArmorTextureForPlayerUUID(uuid);
                if (armorRL != null) armorImg = loadImageResource(armorRL);
            } catch (Throwable ignored) {}

            BufferedImage baseImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            BufferedImage overImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

            Graphics2D gBase = baseImg.createGraphics();
            Graphics2D gOver = overImg.createGraphics();
            gBase.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gBase.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            gOver.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gOver.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            compositeLayoutInto(bundle.leftBase, LEFT_BASE_DEST, skin, gBase);
            compositeLayoutInto(bundle.rightBase, RIGHT_BASE_DEST, skin, gBase);

            BufferedImage overlaySource = armorImg != null ? armorImg : skin;
            compositeLayoutInto(bundle.leftOverlay, LEFT_OVERLAY_DEST, overlaySource, gOver);
            compositeLayoutInto(bundle.rightOverlay, RIGHT_OVERLAY_DEST, overlaySource, gOver);

            gBase.dispose();
            gOver.dispose();

            File genFolder = new File(GEN_DIR);
            if (!genFolder.exists()) genFolder.mkdirs();
            File baseFile = new File(genFolder, uuid.toString() + "_base.png");
            File overFile = new File(genFolder, uuid.toString() + "_overlay.png");
            ImageIO.write(baseImg, "PNG", baseFile);
            ImageIO.write(overImg, "PNG", overFile);

            Minecraft mc = Minecraft.getMinecraft();
            ResourceLocation baseRL = mc.getTextureManager().getDynamicTextureLocation("wfg_breast_base_" + uuid.toString(), new DynamicTexture(baseImg));
            ResourceLocation overRL = mc.getTextureManager().getDynamicTextureLocation("wfg_breast_overlay_" + uuid.toString(), new DynamicTexture(overImg));
            RUNTIME_BASE.put(uuid, baseRL);
            RUNTIME_OVERLAY.put(uuid, overRL);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void generateBreastTextures(UUID uuid) {
        UVBundle b = loadAll(uuid);
        if (b == null) return;
        generateBreastTextures(uuid, b);
    }

    // Inclusive-coordinate composite: sizes include endpoints (x2 - x1 + 1)
    private static void compositeLayoutInto(UVLayout srcLayout, UVLayout destLayout, BufferedImage source, Graphics2D g) {
        if (srcLayout == null || destLayout == null || source == null || g == null) return;
        for (UVDirection dir : UVDirection.values()) {
            UVQuad src = srcLayout.get(dir);
            UVQuad dst = destLayout.get(dir);
            if (src == null || dst == null) continue;

            int sx = src.x1();
            int sy = src.y1();
            int sw = src.x2() - src.x1() + 1;
            int sh = src.y2() - src.y1() + 1;
            if (sw <= 0 || sh <= 0) continue;

            int dx = dst.x1();
            int dy = dst.y1();
            int dw = dst.x2() - dst.x1() + 1;
            int dh = dst.y2() - dst.y1() + 1;
            if (dw <= 0 || dh <= 0) continue;

            try {
                int aw = Math.max(0, Math.min(sw, Math.max(0, source.getWidth() - sx)));
                int ah = Math.max(0, Math.min(sh, Math.max(0, source.getHeight() - sy)));
                if (aw <= 0 || ah <= 0) continue;
                BufferedImage sub = source.getSubimage(Math.max(0, sx), Math.max(0, sy), aw, ah);
                Image scaled = sub.getScaledInstance(dw, dh, Image.SCALE_SMOOTH);
                g.drawImage(scaled, dx, dy, null);
            } catch (Throwable ignored) {}
        }
    }

    private static BufferedImage loadPlayerSkin(UUID uuid) {
        try {
            ResourceLocation rl = null;
            try {
                if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getUniqueID().equals(uuid)) {
                    rl = Minecraft.getMinecraft().thePlayer.getLocationSkin();
                }
            } catch (Throwable ignored) {}
            if (rl == null) rl = new ResourceLocation("textures/entity/steve.png");

            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            if (res != null) {
                InputStream is = res.getInputStream();
                try {
                    BufferedImage skin = ImageIO.read(is);
                    return skin;
                } finally {
                    try { is.close(); } catch (IOException ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static BufferedImage loadImageResource(ResourceLocation rl) {
        try {
            if (rl == null) return null;
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            if (res != null) {
                InputStream is = res.getInputStream();
                try {
                    BufferedImage img = ImageIO.read(is);
                    return img;
                } finally {
                    try { is.close(); } catch (IOException ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void resetToDefaults(UUID uuid) {
        UVBundle b = new UVBundle();
        saveBundle(uuid, b);
        generateBreastTextures(uuid, b);
        applyToRuntime(uuid, b);
    }
}