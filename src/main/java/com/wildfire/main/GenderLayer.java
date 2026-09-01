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
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
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
        if (player != null) {
            getPhysicsForPlayer(player);
        }
    }

    public static void unregister(UUID playerId) {
        if (playerId != null) {
            PHYSICS_CACHE.invalidate(playerId);
        }
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {
        if (this.renderPlayer == null || !shouldRenderBreasts(player)) {
            return;
        }

        EntityConfig entityCfg = EntityConfig.getEntity(player);
        if (entityCfg == null) {
            return;
        }

        boolean isFake = false;
        try { isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer"); } catch (Throwable ignored) {}
        GenderConfig.PlayerGenderSettings cfg = isFake ? GenderConfig.getStaticFakeCreditsSettings()
                : GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (cfg == null || !cfg.breastsEnabled || cfg.breastSize <= 0.0F) {
            return;
        }

        // 1:1 Fabric logic: use Breasts offsets and cleavage, bustSize physics
        // Fabric: breastOffsetX/Y/Z from breasts.offsets(), cleavage -> outwardAngle, bustSize -> breastSize
        float bSizeRaw = cfg.breastSize / 100f; // 0-1
        // Check tightness
        float tightness = 0f;
        try {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                // Get armor tightness via helper
                com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                tightness = MathHelper.clamp_float(armor.tightness(), 0f, 1f);
                if (cfg.overrideArmorPhysics) tightness = 0f;
            }
        } catch (Throwable ignored) {}
        float bSize = bSizeRaw * (1 - BreastPhysics.TIGHTNESS_REDUCTION_FACTOR * tightness);

        // Fabric: breastSize = min(bSize*1.5, 0.7), with compensation if >0.7
        float breastSize = Math.min(bSize * 1.5f, 0.7f);
        float zOffsetComp = 0f;
        if (bSize > 0.7f) {
            zOffsetComp = 0.5f * Math.abs(bSize - 0.7f) * 2f;
        }

        // Offsets: Fabric uses -round(z,1) for Z, and x/y directly
        float breastOffsetX = cfg.breastsOffsetX;
        float breastOffsetY = cfg.breastsOffsetY;
        float breastOffsetZ = -WildfireHelper.round(cfg.breastsOffsetZ, 1) + zOffsetComp;

        float outwardAngle = Math.min(Math.round(cfg.breastsCleavage * 100f), 10);

        // Physics positions - use GenderLayer cache with lerp
        BreastPhysics[] phys = isFake ? null : getPhysicsForPlayer(player);
        float lPosX = 0f, lPosY = 0f, lBounce = 0f;
        float rPosX = 0f, rPosY = 0f, rBounce = 0f;
        boolean isUniboob = cfg.breastsUniboob;
        if (phys != null) {
            // Check if physics enabled
            boolean hasPhysics = cfg.physicsEnabled;
            // Also check armor resistance
            try {
                ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
                if (chest != null && chest.getItem() instanceof ItemArmor) {
                    com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                    float resistance = MathHelper.clamp_float(armor.physicsResistance(), 0f, 1f);
                    if (!cfg.overrideArmorPhysics && resistance >= 1f) hasPhysics = false;
                }
            } catch (Throwable ignored) {}

            if (hasPhysics) {
                lPosX = interp(phys[0].getPrePositionX(), phys[0].getPositionX(), partialTicks);
                lPosY = interp(phys[0].getPrePositionY(), phys[0].getPositionY(), partialTicks);
                lBounce = interp(phys[0].getPreBounceRotation(), phys[0].getBounceRotation(), partialTicks);
                if (isUniboob) {
                    rPosX = lPosX;
                    rPosY = lPosY;
                    rBounce = lBounce;
                } else {
                    rPosX = interp(phys[1].getPrePositionX(), phys[1].getPositionX(), partialTicks);
                    rPosY = interp(phys[1].getPrePositionY(), phys[1].getPositionY(), partialTicks);
                    rBounce = interp(phys[1].getPreBounceRotation(), phys[1].getBounceRotation(), partialTicks);
                }
            }
        }

        // Breathing animation - subtle Y offset when not heavily resisted
        float breathingOffset = 0f;
        try {
            if (player.ticksExisted % 40 < 20) {
                breathingOffset = MathHelper.sin(player.ticksExisted * 0.05f) * 0.02f * breastSize;
            }
        } catch (Throwable ignored) {}

        float renderScale = 0.0625F;
        ModelBiped model = (ModelBiped) this.renderPlayer.getMainModel();

        GlStateManager.pushMatrix();
        try {
            model.bipedBody.postRender(renderScale);
            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            // Fabric positions: breasts are offset from body center
            // Left breast: x = -0.5 - breastOffsetX - cleavage offset, Right: +0.5 + breastOffsetX + cleavage
            // Use Fabric's exact positioning: base separation 0.5 + cleavage/10
            float cleavageOffset = outwardAngle / 10f * 0.1f; // small outward

            // Left side
            float leftX = -0.5f - breastOffsetX - cleavageOffset + lPosX * 0.1f;
            float leftY = breastOffsetY + lPosY * 0.1f + breathingOffset;
            float leftZ = breastOffsetZ + lPosY * 0.05f; // slight Z from physics
            renderSide(player, entityCfg.getLeftBreastUVLayout(), leftX, leftY, leftZ, lBounce, renderScale, breastSize, 0.0F, outwardAngle, true);
            renderSide(player, entityCfg.getLeftBreastOverlayUVLayout(), leftX, leftY, leftZ, lBounce, renderScale, breastSize, 0.25F, outwardAngle, true);

            // Right side
            float rightX = 0.5f + breastOffsetX + cleavageOffset + rPosX * 0.1f;
            float rightY = breastOffsetY + rPosY * 0.1f + breathingOffset;
            float rightZ = breastOffsetZ + rPosY * 0.05f;
            renderSide(player, entityCfg.getRightBreastUVLayout(), rightX, rightY, rightZ, -rBounce, renderScale, breastSize, 0.0F, outwardAngle, false);
            renderSide(player, entityCfg.getRightBreastOverlayUVLayout(), rightX, rightY, rightZ, -rBounce, renderScale, breastSize, 0.25F, outwardAngle, false);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        } catch (Throwable t) {
            System.err.println("[WFG] GenderLayer render error: " + t.getMessage());
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderSide(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, float bounce, float renderScale, float breastSize, float inflate, float outwardAngle, boolean isLeft) {
        if (this.renderPlayer == null || layout == null) {
            return;
        }
        UVQuad north = layout.get(UVDirection.NORTH);
        if (north == null) {
            return;
        }

        // Box size scales with breastSize (Fabric: 4x5x4 base, scaled by breastSize)
        // Use ModelRenderer with proper UVs
        ModelRenderer box = new ModelRenderer((ModelBiped) this.renderPlayer.getMainModel(), north.x1(), north.y1());
        // Fabric box: 4x5x4 with inflate
        box.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, inflate);

        ResourceLocation armorTex = ArmorTextureHelper.getArmorTextureForPlayerUUID(player.getUniqueID(), inflate > 0.0F);
        boolean isWearingArmor = (armorTex != null && inflate > 0.0F);

        if (isWearingArmor) {
            box.setTextureSize(64, 32);
            this.renderPlayer.bindTexture(armorTex);
        } else {
            box.setTextureSize(64, 64);
            ResourceLocation tex = UVStorage.getBreastTexture(player.getUniqueID(), inflate > 0.0F);
            this.renderPlayer.bindTexture(tex != null ? tex : player.getLocationSkin());
        }

        float alpha = player.isInvisible() ? 0.35F : 1.0F;
        GlStateManager.pushMatrix();
        box.setRotationPoint(x * 16f, y * 16f, z * 16f);
        GlStateManager.translate(box.rotationPointX * renderScale, box.rotationPointY * renderScale, box.rotationPointZ * renderScale);
        // Scale Z by breastSize protrusion
        float zScale = 0.5f + breastSize;
        GlStateManager.scale(1.0F, 1.0F, zScale);
        // Apply bounce rotation and outward angle
        box.rotateAngleX = (float) Math.toRadians(-15) + bounce * 0.05f;
        box.rotateAngleY = (float) Math.toRadians(isLeft ? -outwardAngle : outwardAngle);
        box.rotateAngleZ = 0f;
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        box.render(renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
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
        if (player == null) {
            return null;
        }
        @Nonnull UUID id = player.getUniqueID();
        return PHYSICS_CACHE.getUnchecked(id);
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer player) {
        if (!ClientConfig.RENDER_BREASTS) {
            return false;
        }
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (settings == null) {
            return false;
        }
        if (settings.hideInArmor) {
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                if (armor.coversBreasts() && !armor.alwaysHidesBreasts()) {
                    // hideInArmor hides even if coversBreasts
                    return false;
                }
                if (armor.alwaysHidesBreasts()) return false;
            }
        } else {
            // Check alwaysHidesBreasts even when hideInArmor is off
            ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
            if (chest != null && chest.getItem() instanceof ItemArmor) {
                com.wildfire.api.IGenderArmor armor = getArmorForStack(chest);
                if (armor.alwaysHidesBreasts()) return false;
            }
        }
        return settings.breastsEnabled && !"Male".equals(settings.gender);
    }

    private static float interp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
