package com.wildfire.main;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.main.uvs.UVStorage;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderPlayer;

    private static final LoadingCache<UUID, BreastPhysics[]> PHYSICS_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<UUID, BreastPhysics[]>() {
                @Override
                public void onRemoval(com.google.common.cache.RemovalNotification<UUID, BreastPhysics[]> notification) {
                    if (notification.getValue() != null) {
                        System.out.println("[WFG] Physics cleaned up for player: " + notification.getKey());
                    }
                }
            })
            .build(new CacheLoader<UUID, BreastPhysics[]>() {
                @Override
                public BreastPhysics[] load(@Nonnull UUID key) {
                    return new BreastPhysics[] { new BreastPhysics(), new BreastPhysics() };
                }
            });

    public GenderLayer(RenderPlayer renderer) {
        this.renderPlayer = renderer;
    }

    public static void ensureRegisteredForPlayer(AbstractClientPlayer player) {
        if (player != null) getPhysicsForPlayer(player);
    }

    public static void unregister(UUID playerId) {
        if (playerId != null) PHYSICS_CACHE.invalidate(playerId);
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {
        if (this.renderPlayer == null || !shouldRenderBreasts(player)) return;
        if (player.isInvisible()) return;
        EntityConfig entityCfg = EntityConfig.getEntity(player);
        if (entityCfg == null) return;

        boolean isFake = false;
        try { isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer"); } catch (Throwable ignored) {}
        GenderConfig.PlayerGenderSettings cfg = isFake ? GenderConfig.getStaticFakeCreditsSettings()
                : GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (cfg == null || !cfg.breastsEnabled || cfg.breastSize <= 0.0F) return;

        float bSizeRaw = MathHelper.clamp_float(cfg.breastSize / 100f * 0.8f, 0f, 0.8f);
        float tightness = 0f;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                tightness = MathHelper.clamp_float(armor.tightness(), 0f, 1f);
                if (cfg.overrideArmorPhysics) tightness = 0f;
            }
        } catch (Throwable ignored) {}
        float bSize = bSizeRaw * (1 - BreastPhysics.TIGHTNESS_REDUCTION_FACTOR * tightness);
        if (bSize < 0.02f) return;

        float breastSize = Math.min(bSize * 1.5f, 0.7f);
        if (bSize > 0.7f) breastSize = bSize;
        breastSize += 0.5f * Math.abs(bSize - 0.7f) * 2f;

        float breastOffsetX = WildfireHelper.round(cfg.breastsOffsetX / 10f, 1);
        float breastOffsetY = -WildfireHelper.round(cfg.breastsOffsetY / 10f, 1);
        float breastOffsetZ = -WildfireHelper.round(cfg.breastsOffsetZ / 10f, 1);
        float outwardAngle = Math.min(Math.round(cfg.breastsCleavage * 100f), 10);
        float zOffset = 0.0625f - (bSize * 0.0625f);

        BreastPhysics[] phys = isFake ? null : getPhysicsForPlayer(player);
        float lPosX = 0f, lPosY = 0f, lBounce = 0f, rPosX = 0f, rPosY = 0f, rBounce = 0f;
        boolean isUniboob = cfg.breastsUniboob;
        boolean hasPhysics = cfg.physicsEnabled;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                float resistance = MathHelper.clamp_float(armor.physicsResistance(), 0f, 1f);
                if (!cfg.overrideArmorPhysics && resistance >= 1f) hasPhysics = false;
            }
        } catch (Throwable ignored) {}
        boolean bounceEnabled = hasPhysics;
        if (phys != null && hasPhysics) {
            lPosX = interp(phys[0].getPrePositionX(), phys[0].getPositionX(), partialTicks);
            lPosY = interp(phys[0].getPrePositionY(), phys[0].getPositionY(), partialTicks);
            lBounce = interp(phys[0].getPreBounceRotation(), phys[0].getBounceRotation(), partialTicks);
            if (isUniboob) { rPosX = lPosX; rPosY = lPosY; rBounce = lBounce; }
            else {
                rPosX = interp(phys[1].getPrePositionX(), phys[1].getPositionX(), partialTicks);
                rPosY = interp(phys[1].getPrePositionY(), phys[1].getPositionY(), partialTicks);
                rBounce = interp(phys[1].getPreBounceRotation(), phys[1].getBounceRotation(), partialTicks);
            }
        }

        boolean breathing = false;
        try {
            float resistance = 0f;
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) resistance = getArmorForStack(chest).physicsResistance();
            breathing = (cfg.overrideArmorPhysics || resistance <= 0.5f) && !player.isInWater();
        } catch (Throwable ignored) { breathing = !player.isInWater(); }

        float renderScale = 0.0625F;
        ModelBiped model = (ModelBiped) this.renderPlayer.getMainModel();

        GlStateManager.pushMatrix();
        try {
            model.bipedBody.postRender(renderScale);
            if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            renderBreastSide(player, entityCfg.getLeftBreastUVLayout(), entityCfg.getLeftBreastOverlayUVLayout(),
                    true, breastOffsetX, breastOffsetY, breastOffsetZ, zOffset, outwardAngle, breastSize,
                    lPosX, lPosY, lBounce, bounceEnabled, breathing, isUniboob, ageInTicks, renderScale);
            renderBreastSide(player, entityCfg.getRightBreastUVLayout(), entityCfg.getRightBreastOverlayUVLayout(),
                    false, breastOffsetX, breastOffsetY, breastOffsetZ, zOffset, outwardAngle, breastSize,
                    rPosX, rPosY, rBounce, bounceEnabled, breathing, isUniboob, ageInTicks, renderScale);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        } catch (Throwable t) {
            System.err.println("[WFG] GenderLayer render error: " + t.getMessage());
            t.printStackTrace();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderBreastSide(AbstractClientPlayer player, UVLayout baseUV, UVLayout overlayUV,
                                  boolean isLeft, float breastOffsetX, float breastOffsetY, float breastOffsetZ,
                                  float zOffset, float outwardAngle, float breastSize,
                                  float physX, float physY, float bounceRot, boolean bounceEnabled, boolean breathing,
                                  boolean isUniboob, float ageInTicks, float renderScale) {
        GlStateManager.pushMatrix();
        try {
            float sep = isLeft ? breastOffsetX * 0.25f : -breastOffsetX * 0.25f;
            GlStateManager.translate(sep, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0425f));
            if (!isUniboob) GlStateManager.translate(isLeft ? -0.125f : 0.125f, 0, 0);
            if (bounceEnabled) {
                GlStateManager.translate(physX / 32f, physY / 32f, 0);
                GlStateManager.rotate(bounceRot, 0, 1, 0);
            }
            if (!isUniboob) GlStateManager.translate(isLeft ? 0.125f : -0.125f, 0, 0);
            float rotation = breastSize;
            if (bounceEnabled) {
                GlStateManager.translate(0, -0.035f * breastSize, 0);
                rotation -= physY / 12f;
            }
            rotation = Math.min(rotation, breastSize + 0.2f);
            rotation = Math.min(rotation, 1f);
            boolean isChestplate = false;
            ItemStack chest = null;
            com.wildfire.api.IGenderArmor armor = null;
            try {
                chest = ((EntityPlayer) player).inventory.armorInventory[2];
                if (chest != null && chest.getItem() instanceof ItemArmor) {
                    isChestplate = true;
                    armor = getArmorForStack(chest);
                }
            } catch (Throwable ignored) {}
            if (isChestplate && armor != null && armor.coversBreasts()) {
                GlStateManager.translate(0, 0, 0.01f);
            }
            GlStateManager.rotate(isLeft ? outwardAngle : -outwardAngle, 0, 1, 0);
            GlStateManager.rotate(-35f * rotation, 1, 0, 0);
            if (breathing) {
                float f5 = -MathHelper.cos(ageInTicks * 0.09F) * 0.45F + 0.45F;
                GlStateManager.rotate(f5, 1, 0, 0);
            }
            GlStateManager.scale(0.9995f, 1f, 1f);
            float boxX = isLeft ? -4f : 0f;
            renderBox(player, baseUV, boxX, 0f, 0f, 4, 5, 3, 0f, false, renderScale);
            if (isChestplate && armor != null && armor.coversBreasts()) {
                renderArmorLayer(player, chest, boxX, 0f, 0f, 4, 5, 3, 0f, renderScale, isLeft);
            } else {
                GlStateManager.translate(0, 0, -0.015f);
                GlStateManager.scale(1.05f, 1.05f, 1.05f);
                renderBox(player, overlayUV, boxX, 0f, 0f, 4, 5, 3, 0f, true, renderScale);
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderArmorLayer(AbstractClientPlayer player, ItemStack chest, float x, float y, float z, int dx, int dy, int dz, float delta, float renderScale, boolean isLeft) {
        ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayer(player, false);
        ResourceLocation armorOverlayTex = ArmorTextureHelper.getArmorTextureForPlayer(player, true);
        if (armorTex == null) return;
        boolean isLeather = chest.getItem() == net.minecraft.init.Items.leather_chestplate;
        int leatherColor = isLeather ? ((net.minecraft.item.ItemArmor) chest.getItem()).getColor(chest) : 0xFFFFFF;
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(0, 0, -0.015f);
            GlStateManager.scale(1.05f, 1.05f, 1.05f);
            float texW = 32f;
            float texH = 32f;
            float x1 = x * 0.0625f, y1 = y * 0.0625f, z1 = z * 0.0625f;
            float x2 = (x + dx) * 0.0625f, y2 = (y + dy) * 0.0625f, z2 = (z + dz) * 0.0625f;
            float d = delta * 0.0625f;
            x1 -= d; y1 -= d; z1 -= d;
            x2 += d; y2 += d; z2 += d;
            this.renderPlayer.bindTexture(armorTex);
            if (isLeather) {
                float r = (leatherColor >> 16 & 255) / 255f;
                float g = (leatherColor >> 8 & 255) / 255f;
                float b = (leatherColor & 255) / 255f;
                GlStateManager.color(r, g, b, 1f);
            } else {
                GlStateManager.color(1f, 1f, 1f, 1f);
            }
            Tessellator tess = Tessellator.getInstance();
            WorldRenderer wr = tess.getWorldRenderer();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x1, y1, z1).tex(0, 0).normal(0,-1,0).endVertex();
            wr.pos(x2, y1, z1).tex(1, 0).normal(0,-1,0).endVertex();
            wr.pos(x2, y1, z2).tex(1, 1).normal(0,-1,0).endVertex();
            wr.pos(x1, y1, z2).tex(0, 1).normal(0,-1,0).endVertex();
            tess.draw();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x2, y1, z1).tex(0, 0).normal(0,0,-1).endVertex();
            wr.pos(x1, y1, z1).tex(1, 0).normal(0,0,-1).endVertex();
            wr.pos(x1, y2, z1).tex(1, 1).normal(0,0,-1).endVertex();
            wr.pos(x2, y2, z1).tex(0, 1).normal(0,0,-1).endVertex();
            tess.draw();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x1, y1, z2).tex(0, 0).normal(0,0,1).endVertex();
            wr.pos(x2, y1, z2).tex(1, 0).normal(0,0,1).endVertex();
            wr.pos(x2, y2, z2).tex(1, 1).normal(0,0,1).endVertex();
            wr.pos(x1, y2, z2).tex(0, 1).normal(0,0,1).endVertex();
            tess.draw();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x2, y1, z1).tex(0, 0).normal(1,0,0).endVertex();
            wr.pos(x2, y2, z1).tex(1, 0).normal(1,0,0).endVertex();
            wr.pos(x2, y2, z2).tex(1, 1).normal(1,0,0).endVertex();
            wr.pos(x2, y1, z2).tex(0, 1).normal(1,0,0).endVertex();
            tess.draw();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x1, y1, z2).tex(0, 0).normal(-1,0,0).endVertex();
            wr.pos(x1, y2, z2).tex(1, 0).normal(-1,0,0).endVertex();
            wr.pos(x1, y2, z1).tex(1, 1).normal(-1,0,0).endVertex();
            wr.pos(x1, y1, z1).tex(0, 1).normal(-1,0,0).endVertex();
            tess.draw();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            wr.pos(x1, y2, z2).tex(0, 0).normal(0,1,0).endVertex();
            wr.pos(x2, y2, z2).tex(1, 0).normal(0,1,0).endVertex();
            wr.pos(x2, y2, z1).tex(1, 1).normal(0,1,0).endVertex();
            wr.pos(x1, y2, z1).tex(0, 1).normal(0,1,0).endVertex();
            tess.draw();
            if (armorOverlayTex != null) {
                this.renderPlayer.bindTexture(armorOverlayTex);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1f, 1f, 1f, 0.5f);
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x1, y1, z1).tex(0, 0).normal(0,-1,0).endVertex();
                wr.pos(x2, y1, z1).tex(1, 0).normal(0,-1,0).endVertex();
                wr.pos(x2, y1, z2).tex(1, 1).normal(0,-1,0).endVertex();
                wr.pos(x1, y1, z2).tex(0, 1).normal(0,-1,0).endVertex();
                tess.draw();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x2, y1, z1).tex(0, 0).normal(0,0,-1).endVertex();
                wr.pos(x1, y1, z1).tex(1, 0).normal(0,0,-1).endVertex();
                wr.pos(x1, y2, z1).tex(1, 1).normal(0,0,-1).endVertex();
                wr.pos(x2, y2, z1).tex(0, 1).normal(0,0,-1).endVertex();
                tess.draw();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x1, y1, z2).tex(0, 0).normal(0,0,1).endVertex();
                wr.pos(x2, y1, z2).tex(1, 0).normal(0,0,1).endVertex();
                wr.pos(x2, y2, z2).tex(1, 1).normal(0,0,1).endVertex();
                wr.pos(x1, y2, z2).tex(0, 1).normal(0,0,1).endVertex();
                tess.draw();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x2, y1, z1).tex(0, 0).normal(1,0,0).endVertex();
                wr.pos(x2, y2, z1).tex(1, 0).normal(1,0,0).endVertex();
                wr.pos(x2, y2, z2).tex(1, 1).normal(1,0,0).endVertex();
                wr.pos(x2, y1, z2).tex(0, 1).normal(1,0,0).endVertex();
                tess.draw();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x1, y1, z2).tex(0, 0).normal(-1,0,0).endVertex();
                wr.pos(x1, y2, z2).tex(1, 0).normal(-1,0,0).endVertex();
                wr.pos(x1, y2, z1).tex(1, 1).normal(-1,0,0).endVertex();
                wr.pos(x1, y1, z1).tex(0, 1).normal(-1,0,0).endVertex();
                tess.draw();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                wr.pos(x1, y2, z2).tex(0, 0).normal(0,1,0).endVertex();
                wr.pos(x2, y2, z2).tex(1, 0).normal(0,1,0).endVertex();
                wr.pos(x2, y2, z1).tex(1, 1).normal(0,1,0).endVertex();
                wr.pos(x1, y2, z1).tex(0, 1).normal(0,1,0).endVertex();
                tess.draw();
                GlStateManager.disableBlend();
            }
            if (chest.isItemEnchanted()) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                GlStateManager.depthFunc(GL11.GL_EQUAL);
                GlStateManager.disableLighting();
                float f = (float) (System.currentTimeMillis() % 3000L) / 3000f * 256f;
                this.renderPlayer.bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
                for (int i = 0; i < 2; i++) {
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float scale = 0.33333334f;
                    GlStateManager.scale(scale, scale, scale);
                    GlStateManager.rotate(30f - i * 60f, 0, 0, 1);
                    GlStateManager.translate(0, f * (i == 0 ? 1 : -1) * 0.01f, 0);
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x1, y1, z1).tex(0, 0).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z1).tex(1, 0).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z2).tex(1, 1).normal(0,-1,0).endVertex();
                    wr.pos(x1, y1, z2).tex(0, 1).normal(0,-1,0).endVertex();
                    tess.draw();
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x2, y1, z1).tex(0, 0).normal(0,0,-1).endVertex();
                    wr.pos(x1, y1, z1).tex(1, 0).normal(0,0,-1).endVertex();
                    wr.pos(x1, y2, z1).tex(1, 1).normal(0,0,-1).endVertex();
                    wr.pos(x2, y2, z1).tex(0, 1).normal(0,0,-1).endVertex();
                    tess.draw();
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x1, y1, z2).tex(0, 0).normal(0,0,1).endVertex();
                    wr.pos(x2, y1, z2).tex(1, 0).normal(0,0,1).endVertex();
                    wr.pos(x2, y2, z2).tex(1, 1).normal(0,0,1).endVertex();
                    wr.pos(x1, y2, z2).tex(0, 1).normal(0,0,1).endVertex();
                    tess.draw();
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x2, y1, z1).tex(0, 0).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z1).tex(1, 0).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z2).tex(1, 1).normal(1,0,0).endVertex();
                    wr.pos(x2, y1, z2).tex(0, 1).normal(1,0,0).endVertex();
                    tess.draw();
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x1, y1, z2).tex(0, 0).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z2).tex(1, 0).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z1).tex(1, 1).normal(-1,0,0).endVertex();
                    wr.pos(x1, y1, z1).tex(0, 1).normal(-1,0,0).endVertex();
                    tess.draw();
                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    wr.pos(x1, y2, z2).tex(0, 0).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z2).tex(1, 0).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z1).tex(1, 1).normal(0,1,0).endVertex();
                    wr.pos(x1, y2, z1).tex(0, 1).normal(0,1,0).endVertex();
                    tess.draw();
                }
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderBox(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, int dx, int dy, int dz, float delta, boolean isOverlay, float renderScale) {
        if (layout == null) return;
        boolean hasAnyFace = false;
        for (UVDirection dir : UVDirection.values()) {
            UVQuad q = layout.get(dir);
            if (q != null && !(q.x1()==0 && q.y1()==0 && q.x2()==0 && q.y2()==0)) { hasAnyFace = true; break; }
        }
        if (!hasAnyFace && !isOverlay) return;
        if (layout != null) {
            renderBoxWithUVs(player, layout, x, y, z, dx, dy, dz, delta, isOverlay, renderScale);
            return;
        }
        UVQuad north = layout != null ? layout.get(UVDirection.NORTH) : null;
        int texX = north != null ? north.x1() : 0;
        int texY = north != null ? north.y1() : 0;
        ModelRenderer box = new ModelRenderer((ModelBiped) this.renderPlayer.getMainModel(), texX, texY);
        box.addBox(x, y, z, dx, dy, dz, delta);
        box.setTextureSize(64, 64);
        ResourceLocation tex;
        boolean hasChestplate = false;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            hasChestplate = chest != null && chest.getItem() instanceof ItemArmor;
        } catch (Throwable ignored) {}
        if (isOverlay) {
            tex = player.getLocationSkin();
            ResourceLocation overlayTex = UVStorage.getBreastTexture(player.getUniqueID(), true);
            if (overlayTex != null) tex = overlayTex;
            ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayerUUID(player.getUniqueID(), true);
            if (armorTex != null) {
                renderBoxWithUVs(player, layout, x, y, z, dx, dy, dz, delta, true, renderScale);
                return;
            }
        } else {
            tex = UVStorage.getBreastTexture(player.getUniqueID(), false);
            if (tex == null) tex = player.getLocationSkin();
            if (hasChestplate) {
                ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayerUUID(player.getUniqueID(), false);
                if (armorTex != null) {
                    renderBoxWithUVs(player, layout, x, y, z, dx, dy, dz, delta, false, renderScale);
                    return;
                }
            }
        }
        this.renderPlayer.bindTexture(tex);
        float alpha = 1f;
        if (isOverlay) alpha *= 0.9f;
        GlStateManager.color(1f, 1f, 1f, alpha);
        box.render(renderScale);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void renderBoxWithUVs(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, int dx, int dy, int dz, float delta, boolean isOverlay, float renderScale) {
        boolean hasChestplate = false;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            hasChestplate = chest != null && chest.getItem() instanceof ItemArmor;
        } catch (Throwable ignored) {}
        boolean hideInArmor = false;
        try { hideInArmor = GenderConfig.getHideInArmor((EntityPlayer) player); } catch (Throwable ignored) {}
        boolean isFake = false;
        try { isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer"); } catch (Throwable ignored) {}
        if (!isFake && hasChestplate && hideInArmor) return;
        ResourceLocation armorTex = null;
        if (hasChestplate) {
            armorTex = ArmorTextureHelper.getArmorTextureForPlayer(player, isOverlay);
            if (armorTex == null && isOverlay) armorTex = ArmorTextureHelper.getArmorTextureForPlayer(player, false);
        }
        ResourceLocation tex;
        boolean useArmorTex = false;
        if (hasChestplate && armorTex != null) {
            tex = armorTex;
            useArmorTex = true;
        } else if (isOverlay) {
            tex = UVStorage.getBreastTexture(player.getUniqueID(), true);
            if (tex == null) tex = player.getLocationSkin();
        } else {
            tex = UVStorage.getBreastTexture(player.getUniqueID(), false);
            if (tex == null) tex = player.getLocationSkin();
        }
        float alpha = 1f;
        if (isOverlay) alpha *= 0.9f;
        GlStateManager.color(1f, 1f, 1f, alpha);
        float texW = 64f;
        float texH = useArmorTex ? 32f : 64f;
        float x1 = x * 0.0625f, y1 = y * 0.0625f, z1 = z * 0.0625f;
        float x2 = (x + dx) * 0.0625f, y2 = (y + dy) * 0.0625f, z2 = (z + dz) * 0.0625f;
        float d = delta * 0.0625f;
        x1 -= d; y1 -= d; z1 -= d;
        x2 += d; y2 += d; z2 += d;
        this.renderPlayer.bindTexture(tex);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        for (UVDirection dir : UVDirection.values()) {
            if (dir == UVDirection.SOUTH) continue;
            UVQuad quad = layout.get(dir);
            if (quad == null) continue;
            if (quad.x1()==0 && quad.y1()==0 && quad.x2()==0 && quad.y2()==0) continue;
            float u1 = quad.x1() / texW;
            float v1 = quad.y1() / texH;
            float u2 = (quad.x2() + 1) / texW;
            float v2 = (quad.y2() + 1) / texH;
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            switch (dir) {
                case EAST:
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z2).tex(u1, v2).normal(1,0,0).endVertex();
                    wr.pos(x2, y1, z2).tex(u1, v1).normal(1,0,0).endVertex();
                    break;
                case WEST:
                    wr.pos(x1, y1, z2).tex(u2, v1).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z2).tex(u2, v2).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(-1,0,0).endVertex();
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(-1,0,0).endVertex();
                    break;
                case DOWN:
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z2).tex(u2, v2).normal(0,-1,0).endVertex();
                    wr.pos(x1, y1, z2).tex(u1, v2).normal(0,-1,0).endVertex();
                    break;
                case UP:
                    wr.pos(x1, y2, z2).tex(u1, v1).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z2).tex(u2, v1).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(0,1,0).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(0,1,0).endVertex();
                    break;
                case NORTH:
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(0,0,-1).endVertex();
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(0,0,-1).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(0,0,-1).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(0,0,-1).endVertex();
                    break;
                default: break;
            }
            tess.draw();
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private com.wildfire.api.IGenderArmor getArmorForStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemArmor)) return com.wildfire.render.armor.EmptyGenderArmor.INSTANCE;
        if (stack.getItem() == net.minecraft.init.Items.leather_chestplate) return com.wildfire.render.armor.SimpleGenderArmor.LEATHER;
        if (stack.getItem() == net.minecraft.init.Items.chainmail_chestplate) return com.wildfire.render.armor.SimpleGenderArmor.CHAINMAIL;
        if (stack.getItem() == net.minecraft.init.Items.golden_chestplate) return com.wildfire.render.armor.SimpleGenderArmor.GOLD;
        if (stack.getItem() == net.minecraft.init.Items.iron_chestplate) return com.wildfire.render.armor.SimpleGenderArmor.IRON;
        if (stack.getItem() == net.minecraft.init.Items.diamond_chestplate) return com.wildfire.render.armor.SimpleGenderArmor.DIAMOND;
        return com.wildfire.render.armor.SimpleGenderArmor.FALLBACK;
    }

    public static BreastPhysics[] getPhysicsForPlayer(AbstractClientPlayer player) {
        if (player == null) return null;
        @Nonnull UUID id = player.getUniqueID();
        return PHYSICS_CACHE.getUnchecked(id);
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer player) {
        if (!ClientConfig.RENDER_BREASTS) return false;
        boolean isFake = false;
        try { isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer"); } catch (Throwable ignored) {}
        if (isFake) {
            GenderConfig.PlayerGenderSettings fakeCfg = GenderConfig.getStaticFakeCreditsSettings();
            if (fakeCfg != null && fakeCfg.breastsEnabled && !"Male".equals(fakeCfg.gender)) return true;
            try {
                com.wildfire.main.entitydata.EntityConfig ec = com.wildfire.main.entitydata.EntityConfig.getEntity(player);
                if (ec != null) return ec.getBreasts() != null && !"Male".equals(ec.getGender());
            } catch (Throwable ignored) {}
            return true;
        }
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (settings == null) {
            try {
                com.wildfire.main.entitydata.EntityConfig ec = com.wildfire.main.entitydata.EntityConfig.getEntity(player);
                if (ec != null) return ec.getBreasts() != null && !"Male".equals(ec.getGender());
            } catch (Throwable ignored) {}
            return false;
        }
        if (!settings.breastsEnabled || "Male".equals(settings.gender)) return false;
        com.wildfire.api.IGenderArmor armor = com.wildfire.render.armor.EmptyGenderArmor.INSTANCE;
        boolean hasChestplate = false;
        boolean coversBreasts = false;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                hasChestplate = true;
                armor = getArmorForStack(chest);
                coversBreasts = armor.coversBreasts();
            }
        } catch (Throwable ignored) {}
        if (armor.alwaysHidesBreasts()) return false;
        if (settings.hideInArmor && hasChestplate && coversBreasts) return false;
        return true;
    }

    private static float interp(float a, float b, float t) { return a + (b - a) * t; }
    @Override public boolean shouldCombineTextures() { return false; }
}
