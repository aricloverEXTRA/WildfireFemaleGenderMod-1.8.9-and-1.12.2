package com.wildfire.main.uvs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UVStorage {
    private static final Map<UUID, Map<BreastTypes, UVLayout>> userLayouts = new HashMap<UUID, Map<BreastTypes, UVLayout>>();

    private static final LoadingCache<String, ResourceLocation> textureCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(200)
            .build(new CacheLoader<String, ResourceLocation>() {
                @Override
                public ResourceLocation load(String key) {
                    return null;
                }
            });

    public static UVLayout getLayout(UUID uuid, BreastTypes type) {
        if (uuid == null || type == null) {
            return new UVLayout(type);
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
        Map<BreastTypes, UVLayout> bundle = new HashMap<BreastTypes, UVLayout>();
        for (BreastTypes type : BreastTypes.values()) {
            bundle.put(type, new UVLayout(type));
        }
        return bundle;
    }

    public static void saveLayout(UUID uuid, BreastTypes type, UVLayout layout) {
        if (uuid == null || type == null || layout == null) {
            return;
        }

        Map<BreastTypes, UVLayout> bundle = userLayouts.get(uuid);
        if (bundle == null) {
            bundle = createDefaultBundle();
            userLayouts.put(uuid, bundle);
        }
        bundle.put(type, layout.copy());
        generateBreastTextures(uuid);
    }

    public static void unregister(UUID uuid) {
        if (uuid != null) {
            userLayouts.remove(uuid);
            textureCache.invalidate(uuid.toString() + "_base");
            textureCache.invalidate(uuid.toString() + "_overlay");
        }
    }

    public static ResourceLocation getBreastTexture(UUID uuid, boolean overlay) {
        if (uuid == null) {
            return null;
        }
        String key = uuid.toString() + (overlay ? "_overlay" : "_base");
        return textureCache.getIfPresent(key);
    }

    public static void generateBreastTextures(UUID uuid) {
        if (uuid == null) {
            return;
        }
        String key = uuid.toString() + "_base";
        textureCache.invalidate(key);
        textureCache.invalidate(uuid.toString() + "_overlay");
    }
}