package com.wildfire.main.uvs;

import com.wildfire.main.ArmorTextureHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UVStorage for legacy Minecraft (1.8.9 / 1.12.2 backport).
 *
 * Responsibilities:
 * - Load/save per-UUID UV bundles (.uv legacy format or optional JSON)
 * - Generate base and overlay textures by sampling player skin and armor textures
 * - Register generated textures with TextureManager (DynamicTexture)
 * - Provide getLayout and getBreastTexture API for GenderLayer
 * - Apply layouts to runtime EntityConfig / WildfireGender hooks if present
 *
 * Notes:
 * - This implementation attempts to replicate the behavior of the modern Fabric pipeline
 *   while remaining compatible with legacy rendering and resource APIs.
 * - If a player's skin is not yet cached, generation will fall back to default Steve texture
 *   and still produce a valid generated image. When the skin becomes available, call
 *   generateBreastTextures(uuid) again to refresh runtime textures.
 */
public final class UVStorage {
    private static final String DIR = "config" + File.separator + "wfg_uvs";
    private static final String GEN_DIR = DIR + File.separator + "generated";
    private static final String JSON_DIR = DIR + File.separator + "json";

    // Default destination layouts (where we composite into)
    private static final UVLayout LEFT_BASE_DEST;
    private static final UVLayout RIGHT_BASE_DEST;
    private static final UVLayout LEFT_OVERLAY_DEST;
    private static final UVLayout RIGHT_OVERLAY_DEST;

    // Runtime caches for generated textures
    private static final ConcurrentHashMap<UUID, ResourceLocation> RUNTIME_BASE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ResourceLocation> RUNTIME_OVERLAY = new ConcurrentHashMap<>();

    static {
        LEFT_BASE_DEST = new UVLayout();
		LEFT_BASE_DEST.put(UVDirection.EAST,  new UVQuad(20, 20, 23, 23));
		LEFT_BASE_DEST.put(UVDirection.WEST,  new UVQuad(20, 20, 23, 23));
		LEFT_BASE_DEST.put(UVDirection.UP,    new UVQuad(20, 20, 23, 23));
		LEFT_BASE_DEST.put(UVDirection.DOWN,  new UVQuad(20, 20, 23, 23));
		LEFT_BASE_DEST.put(UVDirection.NORTH, new UVQuad(20, 20, 23, 23));
		LEFT_BASE_DEST.put(UVDirection.SOUTH, new UVQuad(20, 20, 23, 23));

        RIGHT_BASE_DEST = new UVLayout();
		RIGHT_BASE_DEST.put(UVDirection.EAST,  new UVQuad(24, 20, 27, 23));
		RIGHT_BASE_DEST.put(UVDirection.WEST,  new UVQuad(24, 20, 27, 23));
		RIGHT_BASE_DEST.put(UVDirection.UP,    new UVQuad(24, 20, 27, 23));
		RIGHT_BASE_DEST.put(UVDirection.DOWN,  new UVQuad(24, 20, 27, 23));
		RIGHT_BASE_DEST.put(UVDirection.NORTH, new UVQuad(24, 20, 27, 23));
		RIGHT_BASE_DEST.put(UVDirection.SOUTH, new UVQuad(24, 20, 27, 23));

        LEFT_OVERLAY_DEST = new UVLayout();
        LEFT_OVERLAY_DEST.put(UVDirection.EAST, new UVQuad(0, 0, 0, 0));
        LEFT_OVERLAY_DEST.put(UVDirection.WEST, new UVQuad(17, 37, 20, 42));
        LEFT_OVERLAY_DEST.put(UVDirection.DOWN, new UVQuad(20, 34, 23, 36));
        LEFT_OVERLAY_DEST.put(UVDirection.UP, new UVQuad(20, 42, 23, 44));
        LEFT_OVERLAY_DEST.put(UVDirection.NORTH, new UVQuad(20, 37, 23, 41));
        LEFT_OVERLAY_DEST.put(UVDirection.SOUTH, new UVQuad(20, 37, 23, 41));

        RIGHT_OVERLAY_DEST = new UVLayout();
        RIGHT_OVERLAY_DEST.put(UVDirection.EAST, new UVQuad(28, 37, 31, 42));
        RIGHT_OVERLAY_DEST.put(UVDirection.WEST, new UVQuad(0, 0, 0, 0));
        RIGHT_OVERLAY_DEST.put(UVDirection.DOWN, new UVQuad(24, 34, 27, 36));
        RIGHT_OVERLAY_DEST.put(UVDirection.UP, new UVQuad(24, 42, 27, 44));
        RIGHT_OVERLAY_DEST.put(UVDirection.NORTH, new UVQuad(24, 37, 27, 41));
        RIGHT_OVERLAY_DEST.put(UVDirection.SOUTH, new UVQuad(24, 37, 27, 41));
    }

    private UVStorage() {}

    // Public API ---------------------------------------------------------------

    /**
     * Return the base or overlay breast texture ResourceLocation for a player UUID, or null if none.
     */
    public static ResourceLocation getBreastTexture(UUID playerId, boolean overlay) {
        if (playerId == null) return null;
        return overlay ? RUNTIME_OVERLAY.get(playerId) : RUNTIME_BASE.get(playerId);
    }

    /**
     * Return the UVLayout for the requested breast type. If none registered, return defaults.
     * This will attempt to load from disk (.uv or JSON) and fall back to defaults.
     */
    public static UVLayout getLayout(UUID playerId, BreastTypes type) {
        UVBundle bundle = loadAll(playerId);
        if (bundle == null) {
            if (type == BreastTypes.LEFT) return LEFT_BASE_DEST.copy();
            if (type == BreastTypes.RIGHT) return RIGHT_BASE_DEST.copy();
            if (type == BreastTypes.LEFT_OVERLAY) return LEFT_OVERLAY_DEST.copy();
            return RIGHT_OVERLAY_DEST.copy();
        }
        switch (type) {
            case LEFT: return bundle.leftBase.copy();
            case RIGHT: return bundle.rightBase.copy();
            case LEFT_OVERLAY: return bundle.leftOverlay.copy();
            default: return bundle.rightOverlay.copy();
        }
    }
	
	// Backwards-compatible wrappers expected by GUI/editor code
	public static UVLayout loadLayout(UUID uuid, BreastTypes type) {
		if (uuid == null) {
			if (type == BreastTypes.LEFT) return LEFT_BASE_DEST.copy();
			if (type == BreastTypes.RIGHT) return RIGHT_BASE_DEST.copy();
			if (type == BreastTypes.LEFT_OVERLAY) return LEFT_OVERLAY_DEST.copy();
			return RIGHT_OVERLAY_DEST.copy();
		}
		UVBundle b = loadAll(uuid);
		if (b == null) {
			if (type == BreastTypes.LEFT) return LEFT_BASE_DEST.copy();
			if (type == BreastTypes.RIGHT) return RIGHT_BASE_DEST.copy();
			if (type == BreastTypes.LEFT_OVERLAY) return LEFT_OVERLAY_DEST.copy();
			return RIGHT_OVERLAY_DEST.copy();
		}
		switch (type) {
			case LEFT: return b.leftBase.copy();
			case RIGHT: return b.rightBase.copy();
			case LEFT_OVERLAY: return b.leftOverlay.copy();
			default: return b.rightOverlay.copy();
		}
	}

	public static void saveLayout(UUID uuid, BreastTypes type, UVLayout layout) {
		if (uuid == null || layout == null) return;
		UVBundle b = loadAll(uuid);
		if (b == null) b = new UVBundle();
		switch (type) {
			case LEFT: b.leftBase = layout.copy(); break;
			case RIGHT: b.rightBase = layout.copy(); break;
			case LEFT_OVERLAY: b.leftOverlay = layout.copy(); break;
			case RIGHT_OVERLAY: b.rightOverlay = layout.copy(); break;
		}
		saveBundle(uuid, b);
		// regenerate runtime textures and apply to runtime caches
		generateBreastTextures(uuid, b);
		applyToRuntime(uuid, b);
	}

    /**
     * Register custom layouts and textures for a player UUID at runtime.
     * Saves the bundle to disk and generates runtime textures.
     */
    public static void register(UUID playerId, UVLayout leftBase, UVLayout rightBase,
                                UVLayout leftOverlay, UVLayout rightOverlay,
                                ResourceLocation explicitBaseTexture, ResourceLocation explicitOverlayTexture) {
        if (playerId == null) return;
        UVBundle bundle = loadAll(playerId);
        if (bundle == null) bundle = new UVBundle();
        if (leftBase != null) bundle.leftBase = leftBase.copy();
        if (rightBase != null) bundle.rightBase = rightBase.copy();
        if (leftOverlay != null) bundle.leftOverlay = leftOverlay.copy();
        if (rightOverlay != null) bundle.rightOverlay = rightOverlay.copy();
        saveBundle(playerId, bundle);
        if (explicitBaseTexture != null) RUNTIME_BASE.put(playerId, explicitBaseTexture);
        if (explicitOverlayTexture != null) RUNTIME_OVERLAY.put(playerId, explicitOverlayTexture);
        generateBreastTextures(playerId, bundle);
        applyToRuntime(playerId, bundle);
    }

    /**
     * Remove any custom data for a player.
     */
    public static void unregister(UUID playerId) {
        if (playerId == null) return;
        RUNTIME_BASE.remove(playerId);
        RUNTIME_OVERLAY.remove(playerId);
        File fBase = new File(GEN_DIR, playerId.toString() + "_base.png");
        if (fBase.exists()) fBase.delete();
        File fOver = new File(GEN_DIR, playerId.toString() + "_overlay.png");
        if (fOver.exists()) fOver.delete();
        File uv = new File(DIR, playerId.toString() + ".uv");
        if (uv.exists()) uv.delete();
        File json = new File(JSON_DIR, playerId.toString() + ".json");
        if (json.exists()) json.delete();
    }

    /**
     * Generate textures for a UUID using the stored bundle on disk.
     */
    public static void generateBreastTextures(UUID uuid) {
        UVBundle b = loadAll(uuid);
        if (b == null) return;
        generateBreastTextures(uuid, b);
    }

    // Internal helpers --------------------------------------------------------

    private static UVBundle loadAll(UUID uuid) {
        if (uuid == null) return null;
        // Try JSON first (modern format), then legacy .uv
        try {
            File jsonDir = new File(JSON_DIR);
            if (jsonDir.exists()) {
                File jf = new File(jsonDir, uuid.toString() + ".json");
                if (jf.exists()) {
                    UVBundle parsed = loadFromJson(jf);
                    if (parsed != null) return parsed;
                }
            }
        } catch (Throwable ignored) {}

        // Legacy .uv loader
        try {
            File baseDir = new File(DIR);
            if (!baseDir.exists()) baseDir.mkdirs();
            File f = new File(baseDir, uuid.toString() + ".uv");
            if (!f.exists()) return new UVBundle();

            UVBundle bundle = new UVBundle();
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
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
            }
            return bundle;
        } catch (IOException e) {
            e.printStackTrace();
            return new UVBundle();
        }
    }

    private static UVBundle loadFromJson(File jf) {
        try (FileReader fr = new FileReader(jf)) {
            JSONParser p = new JSONParser();
            Object o = p.parse(fr);
            if (!(o instanceof JSONObject)) return null;
            JSONObject root = (JSONObject) o;
            UVBundle b = new UVBundle();

            // helper to parse a section into a UVLayout
            java.util.function.BiConsumer<String, java.util.function.Consumer<UVLayout>> parseSection =
                    (key, setter) -> {
                        Object sec = root.get(key);
                        if (!(sec instanceof JSONObject)) return;
                        JSONObject secObj = (JSONObject) sec;
                        UVLayout layout = new UVLayout();
                        for (UVDirection dir : UVDirection.values()) {
                            Object arr = secObj.get(dir.getSaveName());
                            if (arr instanceof java.util.List) {
                                java.util.List<?> list = (java.util.List<?>) arr;
                                if (list.size() >= 4) {
                                    try {
                                        int x1 = ((Number) list.get(0)).intValue();
                                        int y1 = ((Number) list.get(1)).intValue();
                                        int x2 = ((Number) list.get(2)).intValue();
                                        int y2 = ((Number) list.get(3)).intValue();
                                        layout.put(dir, new UVQuad(x1, y1, x2, y2));
                                    } catch (Throwable ignored) {}
                                }
                            }
                        }
                        setter.accept(layout);
                    };

            parseSection.accept("left", l -> b.leftBase = l);
            parseSection.accept("right", l -> b.rightBase = l);
            parseSection.accept("left_overlay", l -> b.leftOverlay = l);
            parseSection.accept("right_overlay", l -> b.rightOverlay = l);

            return b;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static void saveBundle(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        try {
            File baseDir = new File(DIR);
            if (!baseDir.exists()) baseDir.mkdirs();
            File f = new File(baseDir, uuid.toString() + ".uv");
            try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
                writeLayout(w, "left", bundle.leftBase);
                writeLayout(w, "right", bundle.rightBase);
                writeLayout(w, "left_overlay", bundle.leftOverlay);
                writeLayout(w, "right_overlay", bundle.rightOverlay);
                w.flush();
            }
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
        if (name == null) return null;
        name = name.toLowerCase(Locale.ROOT);
        for (UVDirection d : UVDirection.values()) {
            if (d.getSaveName().equals(name) || d.getShortName().equalsIgnoreCase(name)) return d;
        }
        // legacy fallback
        if ("east".equals(name)) return UVDirection.EAST;
        if ("west".equals(name)) return UVDirection.WEST;
        if ("down".equals(name)) return UVDirection.DOWN;
        if ("up".equals(name)) return UVDirection.UP;
        if ("north".equals(name)) return UVDirection.NORTH;
        return null;
    }

    private static void applyToRuntime(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        // Try to call WildfireGender.getOrAddPlayerById(uuid) and update layouts via reflection
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

        // Also update EntityConfig cache if present
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

    /**
     * Generate base and overlay textures for a UUID using the provided bundle.
     * This attempts to sample the player's skin and armor textures and composite the UV quads
     * into 64x64 images. If the player's skin is not available, falls back to default steve skin.
     */
    private static void generateBreastTextures(UUID uuid, UVBundle bundle) {
        if (uuid == null || bundle == null) return;
        try {
            BufferedImage skin = loadPlayerSkin(uuid);
            if (skin == null) {
                // fallback: create transparent images but still register them
                skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = skin.createGraphics();
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, 64, 64);
                g.dispose();
            }

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

            // Composite base (left + right) from skin
            compositeLayoutInto(bundle.leftBase, LEFT_BASE_DEST, skin, gBase);
            compositeLayoutInto(bundle.rightBase, RIGHT_BASE_DEST, skin, gBase);

            // Composite overlay from armor if present, otherwise from skin
            BufferedImage overlaySource = armorImg != null ? armorImg : skin;
            compositeLayoutInto(bundle.leftOverlay, LEFT_OVERLAY_DEST, overlaySource, gOver);
            compositeLayoutInto(bundle.rightOverlay, RIGHT_OVERLAY_DEST, overlaySource, gOver);

            gBase.dispose();
            gOver.dispose();

            // Save generated PNGs for debugging and persistence
            File genFolder = new File(GEN_DIR);
            if (!genFolder.exists()) genFolder.mkdirs();
            File baseFile = new File(genFolder, uuid.toString() + "_base.png");
            File overFile = new File(genFolder, uuid.toString() + "_overlay.png");
            ImageIO.write(baseImg, "PNG", baseFile);
            ImageIO.write(overImg, "PNG", overFile);

            // Register with texture manager
            Minecraft mc = Minecraft.getMinecraft();
            ResourceLocation baseRL = mc.getTextureManager().getDynamicTextureLocation("wfg_breast_base_" + uuid.toString(), new DynamicTexture(baseImg));
            ResourceLocation overRL = mc.getTextureManager().getDynamicTextureLocation("wfg_breast_overlay_" + uuid.toString(), new DynamicTexture(overImg));
            RUNTIME_BASE.put(uuid, baseRL);
            RUNTIME_OVERLAY.put(uuid, overRL);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Inclusive-coordinate composite: sizes include endpoints (x2 - x1 + 1)
    private static void compositeLayoutInto(UVLayout srcLayout, UVLayout destLayout, BufferedImage source, Graphics2D g) {
        if (srcLayout == null || destLayout == null || source == null || g == null) return;
        for (UVDirection dir : UVDirection.values()) {
            UVQuad src = srcLayout.get(dir);
            UVQuad dst = destLayout.get(dir);
            if (src == null || dst == null) continue;

            int sx = Math.max(0, src.x1());
            int sy = Math.max(0, src.y1());
            int sw = Math.max(0, src.x2() - src.x1() + 1);
            int sh = Math.max(0, src.y2() - src.y1() + 1);
            if (sw <= 0 || sh <= 0) continue;

            int dx = Math.max(0, dst.x1());
            int dy = Math.max(0, dst.y1());
            int dw = Math.max(0, dst.x2() - dst.x1() + 1);
            int dh = Math.max(0, dst.y2() - dst.y1() + 1);
            if (dw <= 0 || dh <= 0) continue;

            try {
                int aw = Math.min(sw, Math.max(0, source.getWidth() - sx));
                int ah = Math.min(sh, Math.max(0, source.getHeight() - sy));
                if (aw <= 0 || ah <= 0) continue;

                BufferedImage sub = source.getSubimage(sx, sy, aw, ah);
                g.setComposite(AlphaComposite.SrcOver);
                // Draw scaled into destination rectangle with explicit source/dest coords to preserve alpha
                g.drawImage(sub, dx, dy, dx + dw, dy + dh, 0, 0, sub.getWidth(), sub.getHeight(), null);
                debugLog("Composited " + dir.getSaveName() + " src(" + sx + "," + sy + "," + aw + "," + ah + ") -> dst(" + dx + "," + dy + "," + dw + "," + dh + ")");
            } catch (Throwable t) {
                debugLog("Failed to composite " + dir.getSaveName() + ": " + t.getMessage());
            }
        }
    }

    private static void debugLog(String s) {
        try {
            System.out.println("[UVStorage] " + s);
        } catch (Throwable ignored) {}
    }


    private static BufferedImage loadPlayerSkin(UUID uuid) {
        if (uuid == null) return null;

        // 1) Try generated cached skin (developer convenience)
        try {
            File genSkin = new File(GEN_DIR, uuid.toString() + "_skin.png");
            if (genSkin.exists()) {
                try {
                    BufferedImage img = ImageIO.read(genSkin);
                    if (img != null) {
                        debugLog("Loaded skin from generated cache: " + genSkin.getAbsolutePath());
                        return img;
                    }
                } catch (IOException ignored) {}
            }
        } catch (Throwable ignored) {}

        // 2) Try EntityConfig cache via reflection (if present)
        try {
            try {
                com.wildfire.main.entitydata.EntityConfig cfg = com.wildfire.main.entitydata.EntityConfig.CACHE.getIfPresent(uuid);
                if (cfg != null) {
                    // Try direct method if it exists
                    try {
                        java.lang.reflect.Method m = cfg.getClass().getMethod("getSkinResourceLocation");
                        Object rlObj = m.invoke(cfg);
                        if (rlObj instanceof ResourceLocation) {
                            ResourceLocation rl = (ResourceLocation) rlObj;
                            BufferedImage img = loadImageResource(rl);
                            if (img != null) {
                                debugLog("Loaded skin from EntityConfig.getSkinResourceLocation()");
                                return img;
                            }
                        }
                    } catch (NoSuchMethodException nsme) {
                        // fallback: try common getter names via reflection
                        try {
                            java.lang.reflect.Method m2 = cfg.getClass().getMethod("getSkin");
                            Object rlObj2 = m2.invoke(cfg);
                            if (rlObj2 instanceof ResourceLocation) {
                                BufferedImage img = loadImageResource((ResourceLocation) rlObj2);
                                if (img != null) {
                                    debugLog("Loaded skin from EntityConfig.getSkin()");
                                    return img;
                                }
                            }
                        } catch (Throwable ignored) {}
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}

        // 3) Try CustomSkinLoader local skin folder (best-effort guesses)
        try {
            File mcDir = Minecraft.getMinecraft().mcDataDir;
            String[] candidates = new String[] {
                uuid.toString(),
                "uuid_" + uuid.toString().replace("-", "").substring(0, Math.min(12, uuid.toString().replace("-", "").length())),
                uuid.toString().replace("-", "")
            };
            for (String cand : candidates) {
                File f = new File(mcDir, "CustomSkinLoader" + File.separator + "LocalSkin" + File.separator + "skins" + File.separator + cand + ".png");
                if (f.exists()) {
                    try {
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            debugLog("Loaded skin from CustomSkinLoader local: " + f.getAbsolutePath());
                            return img;
                        }
                    } catch (IOException ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        // 4) Try local player skin ResourceLocation (if this UUID is the local player)
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.thePlayer.getUniqueID().equals(uuid)) {
                try {
                    ResourceLocation rl = mc.thePlayer.getLocationSkin();
                    BufferedImage img = loadImageResource(rl);
                    if (img != null) {
                        debugLog("Loaded skin from local player's ResourceLocation");
                        return img;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        // 5) Fallback to default Steve skin resource
        try {
            ResourceLocation steve = new ResourceLocation("textures/entity/steve.png");
            BufferedImage img = loadImageResource(steve);
            if (img != null) {
                debugLog("Falling back to default Steve skin");
                return img;
            }
        } catch (Throwable ignored) {}

        debugLog("No skin found for UUID " + uuid + " — returning null");
        return null;
    }

    private static BufferedImage loadImageResource(ResourceLocation rl) {
        try {
            if (rl == null) return null;
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(rl);
            if (res != null) {
                try (InputStream is = res.getInputStream()) {
                    return ImageIO.read(is);
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

    // Simple bundle container
    private static class UVBundle {
        UVLayout leftBase = LEFT_BASE_DEST.copy();
        UVLayout rightBase = RIGHT_BASE_DEST.copy();
        UVLayout leftOverlay = LEFT_OVERLAY_DEST.copy();
        UVLayout rightOverlay = RIGHT_OVERLAY_DEST.copy();
    }
}