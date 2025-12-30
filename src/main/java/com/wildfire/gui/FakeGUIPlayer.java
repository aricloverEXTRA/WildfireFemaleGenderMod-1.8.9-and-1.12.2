package com.wildfire.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class FakeGUIPlayer {

    private final String name;
    private final UUID uuid;
    private FakeEntityPlayer entity;

    private static final Map<UUID, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> SKIN_LOADING = new ConcurrentHashMap<>();

    // All fake players share the same preview breast size (static)
    public static float STATIC_FAKE_BREAST_SIZE = 100.0F;

    // single-threaded executor for async skin loading (client-only)
    private static final ExecutorService SKIN_LOADER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Wildfire-FakeGUIPlayer-SkinLoader");
        t.setDaemon(true);
        return t;
    });

    private static final ResourceLocation DEFAULT_STEVE = new ResourceLocation("textures/entity/steve.png");

    public FakeGUIPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid != null ? uuid : UUID.nameUUIDFromBytes(("fake:" + name).getBytes());
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public float getPreviewBreastSize() {
        return STATIC_FAKE_BREAST_SIZE;
    }

    public FakeEntityPlayer getEntity() {
        if (entity == null) {
            Minecraft mc = Minecraft.getMinecraft();
            GameProfile profile = new GameProfile(this.uuid, this.name);

            ResourceLocation skinLoc = SKIN_CACHE.get(this.uuid);
            if (skinLoc == null) {
                skinLoc = new ResourceLocation("wildfire_gender", "skins/" + this.uuid.toString());
                SKIN_CACHE.put(this.uuid, skinLoc);
                queueSkinLoad(mc, this.uuid, this.name, skinLoc);
            }

            entity = new FakeEntityPlayer(mc.theWorld, profile, skinLoc);
        }
        return entity;
    }

    private static void queueSkinLoad(Minecraft mc, UUID uuid, String name, ResourceLocation skinLoc) {
        if (Boolean.TRUE.equals(SKIN_LOADING.get(uuid))) {
            return;
        }
        SKIN_LOADING.put(uuid, true);

        SKIN_LOADER.submit(() -> {
            try {
                String skinUrl = fetchMojangSkinUrl(uuid);
                if (skinUrl == null || skinUrl.isEmpty()) {
                    skinUrl = "https://minotar.net/skin/" + name;
                }

                final String finalSkinUrl = skinUrl;
                if (finalSkinUrl == null || finalSkinUrl.isEmpty()) {
                    SKIN_LOADING.put(uuid, false);
                    return;
                }

                mc.addScheduledTask(() -> {
                    ThreadDownloadImageData td = new ThreadDownloadImageData(
                            null,
                            finalSkinUrl,
                            DEFAULT_STEVE,
                            null
                    );
                    mc.getTextureManager().loadTexture(skinLoc, td);
                    SKIN_LOADING.put(uuid, false);
                });
            } catch (Exception e) {
                System.err.println("Failed to queue skin load for " + uuid + ": " + e.getMessage());
                SKIN_LOADING.put(uuid, false);
            }
        });
    }

    private static String fetchMojangSkinUrl(UUID uuid) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" +
                    uuid.toString().replace("-", ""));
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String json = sb.toString();
            int valIndex = json.indexOf("\"value\":\"");
            if (valIndex == -1) return null;
            int start = valIndex + 9;
            int end = json.indexOf("\"", start);
            if (end == -1) return null;
            String base64 = json.substring(start, end);

            String decoded = new String(Base64.getDecoder().decode(base64), "UTF-8");
            int urlIndex = decoded.indexOf("\"url\":\"");
            if (urlIndex == -1) return null;
            int uStart = urlIndex + 7;
            int uEnd = decoded.indexOf("\"", uStart);
            if (uEnd == -1) return null;
            return decoded.substring(uStart, uEnd);
        } catch (Exception e) {
            System.err.println("Failed to fetch Mojang skin URL for " + uuid + ": " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public void tick() {
        if (entity != null) {
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.rotationYawHead += 0.0F;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class FakeEntityPlayer extends AbstractClientPlayer {

        private final ResourceLocation skin;

        public FakeEntityPlayer(World world, GameProfile profile, ResourceLocation skin) {
            super(world, profile);
            this.skin = skin;
        }

        @Override
        public ResourceLocation getLocationSkin() {
            return skin != null ? skin : getLocationSkin();
        }

        @Override
        public ResourceLocation getLocationCape() {
            return null;
        }

        @Override
        public boolean isSpectator() {
            return false;
        }
    }
}