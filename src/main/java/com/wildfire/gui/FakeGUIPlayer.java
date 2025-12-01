package com.wildfire.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FakeGUIPlayer {
    private final String name;
    private final UUID uuid;
    private EntityOtherPlayerMP entity;

    private static final Map<UUID, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();

    private static final Map<UUID, Float> PREVIEW_BREAST_SIZE = new ConcurrentHashMap<>();

    public FakeGUIPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid != null ? uuid : UUID.nameUUIDFromBytes(("fake:" + name).getBytes());
        PREVIEW_BREAST_SIZE.putIfAbsent(this.uuid, 100.0f);
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public float getPreviewBreastSize() {
        Float v = PREVIEW_BREAST_SIZE.get(this.uuid);
        return v == null ? 100.0f : v;
    }

    public EntityOtherPlayerMP getEntity() {
        if (entity == null) {
            Minecraft mc = Minecraft.getMinecraft();

            // create the preview entity
            GameProfile profile = new GameProfile(this.uuid, this.name);
            entity = new EntityOtherPlayerMP(mc.world, profile);

            // schedule skin resolution & registration in background
            if (!SKIN_CACHE.containsKey(this.uuid)) {
                new Thread(() -> resolveAndRegisterSkin(mc)).start();
            } else {
                // apply cached skin resource location to entity (best-effort)
                try {
                    setEntitySkinResource(entity, SKIN_CACHE.get(this.uuid));
                } catch (Throwable ignored) {}
            }
        }
        return entity;
    }

    private void resolveAndRegisterSkin(Minecraft mc) {
        try {
            String uuidNoHyphen = this.uuid.toString().replace("-", "");
            String[] candidateUrls = new String[] {
                    "https://crafatar.com/skins/" + uuidNoHyphen,                // Crafatar by UUID
                    "https://crafty.gg/skins/" + uuidNoHyphen + ".png",         // Crafty by UUID
                    "https://minotar.net/skin/" + this.name                      // Minotar by name
            };

            String found = null;
            for (String url : candidateUrls) {
                if (testUrlExists(url)) {
                    found = url;
                    break;
                }
            }

            ResourceLocation defaultSkin = new ResourceLocation("textures/entity/steve.png");
            ResourceLocation skinLoc = new ResourceLocation("wildfire_gender", "skins/" + this.uuid.toString());

            if (found != null) {
                ThreadDownloadImageData td = new ThreadDownloadImageData(null, found, defaultSkin, null);
                mc.getTextureManager().loadTexture(skinLoc, td);
                SKIN_CACHE.put(this.uuid, skinLoc);
                try {
                    setEntitySkinResource(entity, skinLoc);
                } catch (Throwable ignored) {}
            } else {
                // No remote skin found; register fallback (default) so texture manager has an entry and avoid repeated probes
                ThreadDownloadImageData td = new ThreadDownloadImageData(null, "", defaultSkin, null);
                mc.getTextureManager().loadTexture(skinLoc, td);
                SKIN_CACHE.put(this.uuid, skinLoc);
                try {
                    setEntitySkinResource(entity, skinLoc);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean testUrlExists(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.connect();
            int code = conn.getResponseCode();
            conn.disconnect();
            return code >= 200 && code < 400;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Try to set the preview entity's skin resource via reflection. We search for common field names and methods.
     * Best-effort only.
     */
    private static void setEntitySkinResource(EntityOtherPlayerMP ent, ResourceLocation skin) {
        if (ent == null || skin == null) return;
        Class<?> cls = ent.getClass();
        while (cls != null) {
            try {
                java.lang.reflect.Field f = null;
                try {
                    f = cls.getDeclaredField("locationSkin");
                } catch (NoSuchFieldException ex) {
                    try { f = cls.getDeclaredField("field_110313_e"); } catch (NoSuchFieldException ex2) { f = null; }
                }
                if (f != null) {
                    f.setAccessible(true);
                    f.set(ent, skin);
                    return;
                }
            } catch (Throwable ignored) {}
            cls = cls.getSuperclass();
        }

        try {
            java.lang.reflect.Method m = AbstractClientPlayer.class.getDeclaredMethod("func_110313_c", ResourceLocation.class);
            if (m != null) {
                m.setAccessible(true);
                m.invoke(ent, skin);
            }
        } catch (Throwable ignored) {}
    }

    public void tick() {
        if (entity != null) {
            entity.prevRotationYawHead = entity.rotationYawHead;
            // subtle tick to keep animation breathing active
            entity.rotationYawHead += 0.0F;
        }
    }
}