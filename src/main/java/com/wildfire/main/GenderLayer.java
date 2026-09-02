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

/**
 * 1:1 Fabric-accurate GenderLayer for 1.8.9
 * - Model: 4x5x3 box at -4,0,0 and 0,0,0 (Fabric BreastModelBox)
 * - Position: torso-anchored via bipedBody.postRender, offsets from Breasts config
 * - UVs: per-face from UVLayout (EAST/WEST/DOWN/UP/NORTH), SOUTH unused
 * - Armor: separate overlay pass with armor texture, 64x32
 */
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
        // Invisibility: hide breasts entirely when player is invisible (like Fabric - don't render translucent)
        if (player.isInvisible()) return;
        EntityConfig entityCfg = EntityConfig.getEntity(player);
        if (entityCfg == null) return;

        boolean isFake = false;
        try { isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer"); } catch (Throwable ignored) {}
        GenderConfig.PlayerGenderSettings cfg = isFake ? GenderConfig.getStaticFakeCreditsSettings()
                : GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (cfg == null || !cfg.breastsEnabled || cfg.breastSize <= 0.0F) return;

        // Fabric: bustSize 0-0.8, cleavage 0-0.1, offsets -1..1
        // 1.8.9 cfg: breastSize 0-100, breastsCleavage 0-10, offsets -10..10
        // Map: bustSize = breastSize/100 *0.8, cleavage = breastsCleavage/100, offsets = breastsOffset/10
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
        if (bSize > 0.7f) breastSize = bSize; // Fabric: if >0.7 use bSize directly

        // Offsets: Fabric uses round(x,1), -round(y,1), -round(z,1)
        float breastOffsetX = WildfireHelper.round(cfg.breastsOffsetX / 10f, 1);
        float breastOffsetY = -WildfireHelper.round(cfg.breastsOffsetY / 10f, 1);
        float breastOffsetZ = -WildfireHelper.round(cfg.breastsOffsetZ / 10f, 1);
        float outwardAngle = Math.min(Math.round((cfg.breastsCleavage / 10f) * 100f), 10);
        float zOffset = 0.0625f - (bSize * 0.0625f);
        if (bSize > 0.7f) breastSize += 0.5f * Math.abs(bSize - 0.7f) * 2f;

        // Physics
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

        // Breathing: Fabric checks isBreathing && (override || resistance<=0.5)
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
            // Anchor to torso like Fabric: root.translateAndRotate + body.translateAndRotate
            model.bipedBody.postRender(renderScale);
            if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);

            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            // Render both sides using Fabric's setupTransformations logic
            // Left
            renderBreastSide(player, entityCfg.getLeftBreastUVLayout(), entityCfg.getLeftBreastOverlayUVLayout(),
                    true, breastOffsetX, breastOffsetY, breastOffsetZ, zOffset, outwardAngle, breastSize,
                    lPosX, lPosY, lBounce, bounceEnabled, breathing, isUniboob, ageInTicks, renderScale);
            // Right
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
        // Fabric setupTransformations:
        // 1. translate(breastOffsetX*0.0625, 0.05625+breastOffsetY*0.0625, zOffset-0.125+breastOffsetZ*0.0425)
        // 2. if !isUniboob translate(leftOrNegate(-0.125),0,0)
        // 3. if bounceEnabled translate(physX/32, physY/32, 0) and rotateY(bounceRot)
        // 4. translate(0, -0.035*breastSize, 0) then rotation -= physY/12
        // 5. rotation = min(rotation, breastSize+0.2, 1)
        // 6. if chestplate translate(0,0,0.01)
        // 7. rotate Y outwardAngle, rotate X -35*rotation, breathing

        GlStateManager.pushMatrix();
        try {
            // Step 1: base offset - Fabric exact (was amplified 2x causing gigantic breasts)
            GlStateManager.translate(breastOffsetX * 0.0625f, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0425f));
            // FIX: Don't reposition based on dual-physics - breasts should stay same position regardless of physics mode
            // Fabric's isUniboob translation was causing visible jump when toggling dual-physics
            if (bounceEnabled) {
                GlStateManager.translate(physX / 32f, physY / 32f, 0);
                // Y rotation from bounce
                GlStateManager.rotate(bounceRot, 0, 1, 0);
            }
            float rotation = breastSize;
            if (bounceEnabled) {
                GlStateManager.translate(0, -0.035f * breastSize, 0);
                rotation -= physY / 12f;
            }
            rotation = Math.min(rotation, breastSize + 0.2f);
            rotation = Math.min(rotation, 1f);

            // Check chestplate
            boolean isChestplate = false;
            try {
                ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
                if (chest != null && chest.getItem() instanceof ItemArmor) isChestplate = true;
            } catch (Throwable ignored) {}
            if (isChestplate) GlStateManager.translate(0, 0, 0.01f);

            // Outward + pitch - Fabric exact
            GlStateManager.rotate(isLeft ? outwardAngle : -outwardAngle, 0, 1, 0);
            GlStateManager.rotate(-35f * rotation, 1, 0, 0);
            if (breathing) {
                float f5 = -MathHelper.cos(ageInTicks * 0.09F) * 0.45F + 0.45F;
                GlStateManager.rotate(f5, 1, 0, 0);
            }
            GlStateManager.scale(0.9995f, 1f, 1f); // z-fighting fix

            // Now render the box at origin (Fabric: -4,0,0 for left, 0,0,0 for right, 4x5x3)
            float boxX = isLeft ? -4f : 0f;
            renderBox(player, baseUV, boxX, 0f, 0f, 4, 5, 3, 0f, false, renderScale);
            // Overlay (jacket layer) - slightly scaled
            GlStateManager.translate(0, 0, -0.015f);
            GlStateManager.scale(1.05f, 1.05f, 1.05f);
            renderBox(player, overlayUV, boxX, 0f, 0f, 4, 5, 3, 0f, true, renderScale);

        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderBox(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, int dx, int dy, int dz, float delta, boolean isOverlay, float renderScale) {
        if (layout == null) return;
        // Check if all UVs are UNUSED (0,0,0,0) - skip rendering that face
        boolean hasAnyFace = false;
        for (UVDirection dir : UVDirection.values()) {
            UVQuad q = layout.get(dir);
            if (q != null && !(q.x1()==0 && q.y1()==0 && q.x2()==0 && q.y2()==0)) { hasAnyFace = true; break; }
        }
        if (!hasAnyFace && !isOverlay) return; // Don't skip overlay if it's the jacket layer

        // Fabric uses WildfireModelRenderer.BreastModelBox with per-face UVs from UVLayout
        // For 1.8.9 we render manually with Tessellator to get exact per-face UVs
        // Fallback to ModelRenderer only if layout is null
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

        // Bind correct texture
        ResourceLocation tex;
        if (isOverlay) {
            // Overlay uses jacket layer - check if player has jacket
            tex = player.getLocationSkin();
            // For overlay, we still use skin but with overlay UVs
            ResourceLocation overlayTex = UVStorage.getBreastTexture(player.getUniqueID(), true);
            if (overlayTex != null) tex = overlayTex;
        } else {
            tex = UVStorage.getBreastTexture(player.getUniqueID(), false);
            if (tex == null) tex = player.getLocationSkin();
        }

        // Armor overlay: if wearing armor and this is overlay pass, use armor texture
        if (isOverlay) {
            ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayerUUID(player.getUniqueID(), true);
            if (armorTex != null) {
                box.setTextureSize(64, 32);
                this.renderPlayer.bindTexture(armorTex);
                GlStateManager.color(1f, 1f, 1f, 1f);
                box.render(renderScale);
                GlStateManager.color(1f, 1f, 1f, 1f);
                return;
            }
        }

        this.renderPlayer.bindTexture(tex);
        float alpha = 1f;
        if (isOverlay) alpha *= 0.9f;
        GlStateManager.color(1f, 1f, 1f, alpha);
        // Render with per-face UVs by using custom box rendering
        // Since ModelRenderer doesn't support per-face UVs in 1.8.9, we render manually if needed
        // For now use standard render - UVs are approximated via north face
        box.render(renderScale);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void renderBoxWithUVs(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, int dx, int dy, int dz, float delta, boolean isOverlay, float renderScale) {
        // Bind texture
        ResourceLocation tex;
        boolean useArmorTex = false;
        if (isOverlay) {
            ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayerUUID(player.getUniqueID(), true);
            if (armorTex != null) {
                tex = armorTex;
                useArmorTex = true;
            } else {
                tex = UVStorage.getBreastTexture(player.getUniqueID(), true);
                if (tex == null) tex = player.getLocationSkin();
            }
        } else {
            tex = UVStorage.getBreastTexture(player.getUniqueID(), false);
            if (tex == null) tex = player.getLocationSkin();
        }
        // Already checked isInvisible at top - this is unreachable but keep for safety
        float alpha = 1f;
        if (isOverlay) alpha *= 0.9f;
        GlStateManager.color(1f, 1f, 1f, alpha);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float texW = useArmorTex ? 64f : 64f;
        float texH = useArmorTex ? 32f : 64f;

        // Box corners with delta expansion
        float x1 = x - delta, y1 = y - delta, z1 = z - delta;
        float x2 = x + dx + delta, y2 = y + dy + delta, z2 = z + dz + delta;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        // Render each face with its UVQuad - skip UNUSED (0,0,0,0)
        for (UVDirection dir : UVDirection.values()) {
            if (dir == UVDirection.SOUTH) continue; // Fabric doesn't use SOUTH for breasts
            UVQuad quad = layout.get(dir);
            if (quad == null) continue;
            if (quad.x1()==0 && quad.y1()==0 && quad.x2()==0 && quad.y2()==0) continue;

            float u1 = quad.x1() / texW;
            float v1 = quad.y1() / texH;
            float u2 = (quad.x2() + 1) / texW;
            float v2 = (quad.y2() + 1) / texH;

            // Clamp UVs
            u1 = Math.max(0, Math.min(1, u1)); u2 = Math.max(0, Math.min(1, u2));
            v1 = Math.max(0, Math.min(1, v1)); v2 = Math.max(0, Math.min(1, v2));

            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
            switch (dir) {
                case EAST: // +X
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(1,0,0).endVertex();
                    wr.pos(x2, y2, z2).tex(u1, v2).normal(1,0,0).endVertex();
                    wr.pos(x2, y1, z2).tex(u1, v1).normal(1,0,0).endVertex();
                    break;
                case WEST: // -X
                    wr.pos(x1, y1, z2).tex(u2, v1).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z2).tex(u2, v2).normal(-1,0,0).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(-1,0,0).endVertex();
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(-1,0,0).endVertex();
                    break;
                case DOWN: // -Y
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(0,-1,0).endVertex();
                    wr.pos(x2, y1, z2).tex(u2, v2).normal(0,-1,0).endVertex();
                    wr.pos(x1, y1, z2).tex(u1, v2).normal(0,-1,0).endVertex();
                    break;
                case UP: // +Y
                    wr.pos(x1, y2, z2).tex(u1, v1).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z2).tex(u2, v1).normal(0,1,0).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(0,1,0).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(0,1,0).endVertex();
                    break;
                case NORTH: // -Z (front)
                    wr.pos(x2, y1, z1).tex(u2, v1).normal(0,0,-1).endVertex();
                    wr.pos(x1, y1, z1).tex(u1, v1).normal(0,0,-1).endVertex();
                    wr.pos(x1, y2, z1).tex(u1, v2).normal(0,0,-1).endVertex();
                    wr.pos(x2, y2, z1).tex(u2, v2).normal(0,0,-1).endVertex();
                    break;
                default: break;
            }
            tess.draw();
        }
        GlStateManager.disableBlend();
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
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (settings == null) return false;
        // Fabric: if armor alwaysHidesBreasts or (!showBreastsInArmor && isChestplateOccupied) return false
        boolean isChestplateOccupied = false;
        com.wildfire.api.IGenderArmor armor = com.wildfire.render.armor.EmptyGenderArmor.INSTANCE;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                armor = getArmorForStack(chest);
                boolean override = GenderConfig.getOverrideArmorPhysics((EntityPlayer) player);
                isChestplateOccupied = armor.coversBreasts() && !override;
            }
        } catch (Throwable ignored) {}
        if (armor.alwaysHidesBreasts()) return false;
        if (!settings.hideInArmor && isChestplateOccupied) {
            // showBreastsInArmor is !hideInArmor in 1.8.9
            // Fabric: !showBreastsInArmor && isChestplateOccupied -> hide
            // So if hideInArmor==false, showBreastsInArmor==true, don't hide
        } else if (settings.hideInArmor && isChestplateOccupied) {
            return false;
        }
        if (armor.alwaysHidesBreasts()) return false;
        return settings.breastsEnabled && !"Male".equals(settings.gender);
    }

    private static float interp(float a, float b, float t) { return a + (b - a) * t; }
    @Override public boolean shouldCombineTextures() { return false; }
}
