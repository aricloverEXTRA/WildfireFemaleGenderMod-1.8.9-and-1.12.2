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
    private static final float MAX_PROTRUSION = 1.6F;

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

        boolean isFake = player.getEntityData().getBoolean("WFG_FakeGUIPlayer");
        GenderConfig.PlayerGenderSettings cfg = isFake ? GenderConfig.getStaticFakeCreditsSettings()
                : GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (cfg == null || !cfg.breastsEnabled || cfg.breastSize <= 0.0F) {
            return;
        }

        float sizeFactor = MathHelper.clamp_float(cfg.breastSize / 100.0F, 0.0F, 1.0F);
        float zScale = 0.1F + (0.9F * sizeFactor);
        float torsoPush = (1.0F - sizeFactor) * 1.6F;

        float separationBase = 0.8125F + (cfg.breastsCleavage / 60.0F);
        float userXOffset = cfg.breastsOffsetX;
        float baseY = 3.5F + cfg.breastsOffsetY - (cfg.height / 40.0F);
        float baseZ = cfg.breastsOffsetZ - (cfg.depth / 10.0F) - 1.5F + (MAX_PROTRUSION * sizeFactor) + torsoPush;

        BreastPhysics[] phys = isFake ? null : getPhysicsForPlayer(player);
        float renderScale = 0.0625F;
        ModelBiped model = (ModelBiped) this.renderPlayer.getMainModel();

        GlStateManager.pushMatrix();
        model.bipedBody.postRender(renderScale);
        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.24F, 0.0F);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        float lPosX = 0.0F;
        float lPosY = 0.0F;
        float lBounce = 0.0F;
        if (phys != null) {
            lPosX = interp(phys[0].getPrePositionX(), phys[0].getPositionX(), partialTicks);
            lPosY = interp(phys[0].getPrePositionY(), phys[0].getPositionY(), partialTicks);
            lBounce = interp(phys[0].getPreBounceRotation(), phys[0].getBounceRotation(), partialTicks);
        }
        renderSide(player, entityCfg.getLeftBreastUVLayout(), (userXOffset - separationBase) - (lPosX * 0.34F), baseY + lPosY, baseZ, lBounce, renderScale, zScale, 0.0F);
        renderSide(player, entityCfg.getLeftBreastOverlayUVLayout(), (userXOffset - separationBase) - (lPosX * 0.34F), baseY + lPosY, baseZ, lBounce, renderScale, zScale, 0.25F);

        float rPosX = 0.0F;
        float rPosY = 0.0F;
        float rBounce = 0.0F;
        if (phys != null) {
            rPosX = interp(phys[1].getPrePositionX(), phys[1].getPositionX(), partialTicks);
            rPosY = interp(phys[1].getPrePositionY(), phys[1].getPositionY(), partialTicks);
            rBounce = -interp(phys[1].getPreBounceRotation(), phys[1].getBounceRotation(), partialTicks);
        }
        renderSide(player, entityCfg.getRightBreastUVLayout(), (userXOffset + separationBase) + (rPosX * 0.34F), baseY + rPosY, baseZ, rBounce, renderScale, zScale, 0.0F);
        renderSide(player, entityCfg.getRightBreastOverlayUVLayout(), (userXOffset + separationBase) + (rPosX * 0.34F), baseY + rPosY, baseZ, rBounce, renderScale, zScale, 0.25F);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderSide(AbstractClientPlayer player, UVLayout layout, float x, float y, float z, float bounce, float renderScale, float zScale, float inflate) {
        if (this.renderPlayer == null || layout == null) {
            return;
        }
        UVQuad north = layout.get(UVDirection.NORTH);
        if (north == null) {
            return;
        }

        ModelRenderer box = new ModelRenderer((ModelBiped) this.renderPlayer.getMainModel(), north.x1(), north.y1());
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
        box.setRotationPoint(x, y, z);
        GlStateManager.translate(box.rotationPointX * renderScale, box.rotationPointY * renderScale, box.rotationPointZ * renderScale);
        GlStateManager.scale(1.0F, 1.0F, zScale);
        box.rotateAngleX = (float) Math.toRadians(-26.92D) + bounce;
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        box.render(renderScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
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
                return false;
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