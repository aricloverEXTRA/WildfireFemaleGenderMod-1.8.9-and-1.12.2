package com.wildfire.main.uvs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

public class UVStorage {
    private static final Map<UUID, Map<BreastTypes, UVLayout>> userLayouts = new HashMap<>();
    private static final Map<String, ResourceLocation> generatedTextures = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final File storageFile = new File(Minecraft.getMinecraft().mcDataDir, "config/wildfire_uvs.json");

    public static UVLayout getLayout(UUID uuid, BreastTypes type) {
        if (!userLayouts.containsKey(uuid)) loadAll();
        return userLayouts.computeIfAbsent(uuid, k -> createDefaultBundle()).get(type).copy();
    }

    private static Map<BreastTypes, UVLayout> createDefaultBundle() {
        Map<BreastTypes, UVLayout> bundle = new HashMap<>();
        for (BreastTypes t : BreastTypes.values()) {
            bundle.put(t, new UVLayout(t));
        }
        return bundle;
    }

    public static void saveLayout(UUID uuid, BreastTypes type, UVLayout layout) {
        userLayouts.computeIfAbsent(uuid, k -> createDefaultBundle()).put(type, layout);
        saveAll();
        generateBreastTextures(uuid);
    }

    private static void saveAll() {
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(userLayouts, writer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadAll() {
        if (!storageFile.exists()) return;
        try (FileReader reader = new FileReader(storageFile)) {
            Map<UUID, Map<BreastTypes, UVLayout>> loaded = gson.fromJson(reader, 
                new TypeToken<Map<UUID, Map<BreastTypes, UVLayout>>>(){}.getType());
            if (loaded != null) userLayouts.putAll(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

	public static void unregister(UUID uuid) {
		userLayouts.remove(uuid);
		// This clears the cached layout for that user so it reloads from disk
	}

    public static ResourceLocation getBreastTexture(UUID uuid, boolean overlay) {
        String key = uuid.toString() + (overlay ? "_overlay" : "_base");
        return generatedTextures.get(key);
    }

    public static void generateBreastTextures(UUID uuid) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        try {
            BufferedImage skin = ImageIO.read(mc.getResourceManager().getResource(mc.thePlayer.getLocationSkin()).getInputStream());
            processSet(uuid, skin, false);
            processSet(uuid, skin, true);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void processSet(UUID uuid, BufferedImage source, boolean isOverlay) {
        BufferedImage canvas = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Map<BreastTypes, UVLayout> bundle = userLayouts.getOrDefault(uuid, createDefaultBundle());

        for (Map.Entry<BreastTypes, UVLayout> entry : bundle.entrySet()) {
            if (isOverlay != entry.getKey().name().contains("OVERLAY")) continue;
            UVLayout layout = entry.getValue();
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
        Minecraft.getMinecraft().getTextureManager().loadTexture(loc, new DynamicTexture(canvas));
        generatedTextures.put(key, loc);
    }
}