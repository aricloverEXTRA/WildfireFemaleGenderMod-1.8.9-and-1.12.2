package com.wildfire.main;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.main.config.ClientConfig;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer renderPlayer;

    private final ModelRenderer leftBreastFront, rightBreastFront;
    private final ModelRenderer leftBreastWearFront, rightBreastWearFront;
    private final ModelRenderer leftBreastWearBottom, rightBreastWearBottom;

    private static final float MAX_PROTRUSION = 1.6f;
    private static final float YAW_FACTOR = 0.34f;

    private static final ConcurrentHashMap<UUID, BreastPhysics[]> PHYSICS_MAP = new ConcurrentHashMap<>();

    public GenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        ModelBiped main = (ModelBiped) renderPlayer.getMainModel();

        leftBreastFront = new ModelRenderer(main, 20, 20);
        leftBreastFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4);
        leftBreastFront.setTextureSize(64, 64);

        rightBreastFront = new ModelRenderer(main, 24, 20);
        rightBreastFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4);
        rightBreastFront.setTextureSize(64, 64);

        leftBreastWearFront = new ModelRenderer(main, 16, 32);
        leftBreastWearFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, 0.25F);
        leftBreastWearFront.setTextureSize(64, 64);

        rightBreastWearFront = new ModelRenderer(main, 20, 32);
        rightBreastWearFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, 0.25F);
        rightBreastWearFront.setTextureSize(64, 64);

        leftBreastWearBottom = new ModelRenderer(main, 20, 36);
        leftBreastWearBottom.addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, 0.30F);
        leftBreastWearBottom.setTextureSize(64, 64);

        rightBreastWearBottom = new ModelRenderer(main, 32, 36);
        rightBreastWearBottom.addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, 0.30F);
        rightBreastWearBottom.setTextureSize(64, 64);
    }

    public static void ensureRegisteredForPlayer(AbstractClientPlayer player) {
        if (player == null) return;
        PHYSICS_MAP.computeIfAbsent(player.getUniqueID(),
                id -> new BreastPhysics[]{new BreastPhysics(), new BreastPhysics()});
    }

    public static BreastPhysics[] getPhysicsForPlayer(AbstractClientPlayer player) {
        if (player == null) return null;
        return PHYSICS_MAP.computeIfAbsent(player.getUniqueID(),
                id -> new BreastPhysics[]{new BreastPhysics(), new BreastPhysics()});
    }

    public static void unregister(UUID playerId) {
        PHYSICS_MAP.remove(playerId);
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {

        if (!shouldRenderBreasts(player)) return;

        GenderConfig.PlayerGenderSettings cfg = GenderConfig.getPlayerSettings(player);
        if (cfg == null) return;

        if (cfg.breastSize <= 0) return;

        ensureRegisteredForPlayer(player);
        Breasts breasts = new Breasts(player);
        BreastPhysics[] phys = getPhysicsForPlayer(player);
        if (phys == null) return;

        float sizeFactor = MathHelper.clamp_float(cfg.breastSize / 100.0f, 0.0f, 1.0f);
        float zScale = 0.1f + (1.0f - 0.1f) * sizeFactor;
        float torsoPush = (1.0f - sizeFactor) * 1.6f;

        float separation = cfg.separation / 30.0F
                - cfg.breastsCleavage / 60.0F
                + 0.0625F + breasts.getCleavage() + 0.125F + 0.625F;

        float depth = -cfg.depth / 10.0F;
        float height = -(cfg.height / 40.0F);

        float baseX = breasts.getXOffset() - 0.350F;
        float baseY = 3.5F + height;
        float baseZ = breasts.getZOffset() + depth - 1.5F + MAX_PROTRUSION * sizeFactor + torsoPush;

        float lPosX = interp(phys[0].getPrePositionX(), phys[0].getPositionX(), partialTicks);
        float lPosY = interp(phys[0].getPrePositionY(), phys[0].getPositionY(), partialTicks);
        float lBounce = interp(phys[0].getPreBounceRotation(), phys[0].getBounceRotation(), partialTicks);

        float rPosX, rPosY, rBounce;
        if (cfg.breastsUniboob) {
            rPosX = lPosX;
            rPosY = lPosY;
            rBounce = -lBounce;
        } else {
            rPosX = interp(phys[1].getPrePositionX(), phys[1].getPositionX(), partialTicks);
            rPosY = interp(phys[1].getPrePositionY(), phys[1].getPositionY(), partialTicks);
            rBounce = -interp(phys[1].getPreBounceRotation(), phys[1].getBounceRotation(), partialTicks);
        }

        // --- Render ---
        ModelBiped model = (ModelBiped) renderPlayer.getMainModel();
        ModelRenderer torso = model.bipedBody;
        float renderScale = 0.0625F;

        renderPlayer.bindTexture(player.getLocationSkin());
        GlStateManager.pushMatrix();
        torso.postRender(renderScale);

        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.24F, 0.0F);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        renderBreast(leftBreastFront, baseX - separation, baseY + lPosY, baseZ,
                lBounce, -lPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

        renderBreast(rightBreastFront, baseX + separation, baseY + rPosY, baseZ,
                rBounce, rPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

        if (player.isWearing(EnumPlayerModelParts.JACKET)) {
            renderBreast(leftBreastWearFront, baseX - separation, baseY + lPosY, baseZ,
                    lBounce, -lPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

            renderBreast(rightBreastWearFront, baseX + separation, baseY + rPosY, baseZ,
                    rBounce, rPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

            renderBreast(leftBreastWearBottom, baseX - separation, baseY + lPosY + 0.5F, baseZ,
                    lBounce + (float)Math.PI + (float)Math.toRadians(4.0), -lPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

            renderBreast(rightBreastWearBottom, baseX + separation, baseY + rPosY + 0.5F, baseZ,
                    rBounce + (float)Math.PI + (float)Math.toRadians(4.0), rPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderBreast(ModelRenderer model, float x, float y, float z,
                              float bounceRot, float yaw,
                              float scaleX, float scaleY, float scaleZ, float renderScale) {
        GlStateManager.pushMatrix();

        model.setRotationPoint(x, y, z);
        GlStateManager.translate(model.rotationPointX * renderScale,
                model.rotationPointY * renderScale,
                model.rotationPointZ * renderScale);

        GlStateManager.scale(scaleX, scaleY, scaleZ);

        GlStateManager.translate(-model.rotationPointX * renderScale,
                -model.rotationPointY * renderScale,
                -model.rotationPointZ * renderScale);

        model.rotateAngleX = (float) Math.toRadians(-26.92285) + bounceRot - 0.1f;
        model.rotateAngleY = yaw;
        model.render(renderScale);

        GlStateManager.popMatrix();
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer player) {
        if (!ClientConfig.RENDER_BREASTS) return false;

        GenderConfig.PlayerGenderSettings s = GenderConfig.getPlayerSettings(player);
        if (s == null) return false;

        if (s.hideInArmor) {
            try {
                net.minecraft.item.ItemStack chest = player.inventory.armorInventory[2];
                if (chest != null && chest.getItem() instanceof net.minecraft.item.ItemArmor) {
                    return false;
                }
            } catch (Throwable ignored) {}
        }

        return s.breastsEnabled && ("Female".equals(s.gender) || "Other".equals(s.gender));
    }

    private static float interp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}