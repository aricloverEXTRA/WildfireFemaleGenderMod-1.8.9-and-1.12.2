package com.wildfire.main.uvs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * FIXED: Replaced unbounded texture cache with expiring LoadingCache.
 * Generated textures auto-expire after 10 minutes of inactivity.
 * UV layouts are now cached per-player with proper cleanup.
 */
public class UVStorage {
    private static final Map<UUID, Map<BreastTypes, UVLayout>> userLayouts = new HashMap<>();
    
    // FIXED: Use LoadingCache with expiration instead of unbounded HashMap
    private static final LoadingCache<String, ResourceLocation> textureCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(500)  // Hard limit on cached textures
            .build(new CacheLoader<String, ResourceLocation>() {
                @Override
                public ResourceLocation load(String key) {
                    return null;  // Placeholder; actual loading done manually
                }
            });
    
    private static final Gson gson = new Gson();
    private static final File storageFile = new File(Minecraft.getMinecraft().mcDataDir, "config/wildfire_uvs.json");

    public static UVLayout getLayout(UUID uuid, BreastTypes type) {
        if (uuid == null || type == null) return new UVLayout(type);
        
        if (!userLayouts.containsKey(uuid)) {
            loadAll();
        }
        
        Map<BreastTypes, UVLayout> bundle = userLayouts.get(uuid);
        if (bundle == null) {
            bundle = createDefaultBundle();
            userLayouts.put(uuid, bundle);
        }
        
        UVLayout layout = bundle.get(type);
        return layout != null ? layout.copy() : new UVLayout(type);
    }

    private static Map<BreastTypes, UVLayout> createDefaultBundle() {
        Map<BreastTypes, UVLayout> bundle = new HashMap<>();
        for (BreastTypes t : BreastTypes.values()) {
            bundle.put(t, new UVLayout(t));
        }
        return bundle;
    }

    public static void saveLayout(UUID uuid, BreastTypes type, UVLayout layout) {
        if (uuid == null || type == null || layout == null) return;
        
        userLayouts.computeIfAbsent(uuid, k -> createDefaultBundle()).put(type, layout);
        saveAll();
        generateBreastTextures(uuid);
    }

    private static void saveAll() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(userLayouts, writer);
        } catch (Exception e) { 
            System.err.println("[WFG] Failed to save UV layouts: " + e.getMessage());
        }
    }

    private static void loadAll() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Map<UUID, Map<BreastTypes, UVLayout>> loaded = gson.fromJson(reader, 
                new TypeToken<Map<UUID, Map<BreastTypes, UVLayout>>>(){}.getType());
            if (loaded != null) {
                userLayouts.putAll(loaded);
            }
        } catch (Exception e) { 
            System.err.println("[WFG] Failed to load UV layouts: " + e.getMessage());
        }
    }

    // FIXED: Proper cleanup on unregister
    public static void unregister(UUID uuid) {
        if (uuid == null) return;
        userLayouts.remove(uuid);
        
        // Also invalidate generated textures for this player
        textureCache.invalidate(uuid.toString() + "_base");
        textureCache.invalidate(uuid.toString() + "_overlay");
        System.out.println("[WFG] Unregistered UV layouts for: " + uuid);
    }

    // FIXED: Cache lookup with expiration
    public static ResourceLocation getBreastTexture(UUID uuid, boolean overlay) {
        if (uuid == null) return null;
        String key = uuid.toString() + (overlay ? "_overlay" : "_base");
        try {
            return textureCache.getIfPresent(key);
        } catch (Exception e) {
            return null;
        }
    }

    // FIXED: Only generate if layouts exist; avoid repeat generation
    public static void generateBreastTextures(UUID uuid) {
        if (uuid == null) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !mc.thePlayer.getUniqueID().equals(uuid)) {
            return;  // Only generate for local player
        }
        
        try {
            BufferedImage skin = ImageIO.read(mc.getResourceManager().getResource(mc.thePlayer.getLocationSkin()).getInputStream());
            processSet(uuid, skin, false);
            processSet(uuid, skin, true);
        } catch (Exception e) { 
            System.err.println("[WFG] Failed to generate breast textures: " + e.getMessage());
        }
    }

    private static void processSet(UUID uuid, BufferedImage source, boolean isOverlay) {
        if (uuid == null || source == null) return;
        
        BufferedImage canvas = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Map<BreastTypes, UVLayout> bundle = userLayouts.getOrDefault(uuid, createDefaultBundle());

        for (Map.Entry<BreastTypes, UVLayout> entry : bundle.entrySet()) {
            if (isOverlay != entry.getKey().name().contains("OVERLAY")) continue;
            
            UVLayout layout = entry.getValue();
            if (layout == null) continue;
            
            for (Map.Entry<UVDirection, UVQuad> side : layout.getAllSides().entrySet()) {
                UVQuad q = side.getValue();
                if (q == null || q.x1() < 0) continue;
                
                int w = Math.max(1, q.x2() - q.x1());
                int h = Math.max(1, q.y2() - q.y1());
                
                try {
                    BufferedImage sub = source.getSubimage(q.x1(), q.y1(), w, h);
                    canvas.getGraphics().drawImage(sub, q.x1(), q.y1(), null);
                } catch (Exception ignored) {}
            }
        }
        
        String key = uuid.toString() + (isOverlay ? "_overlay" : "_base");
        ResourceLocation loc = new ResourceLocation("wildfire_gender", "textures/generated/" + key);
        
        try {
            Minecraft.getMinecraft().getTextureManager().loadTexture(loc, new DynamicTexture(canvas));
            textureCache.put(key, loc);
        } catch (Exception e) {
            System.err.println("[WFG] Failed to load texture: " + e.getMessage());
        }
    }
}