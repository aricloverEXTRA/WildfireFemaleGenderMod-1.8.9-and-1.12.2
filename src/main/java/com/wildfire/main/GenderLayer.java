package com.wildfire.main;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GenderLayer
 * - Right breast now uses its own right-side UVs (no mirroring).
 * - Proper X/Z scaling (no vertical stretch).
 * - Height slider inverted: positive moves up.
 * - Jacket toggle respected for wear layers.
 * - Added "underboob" wear overlays (bottom-facing dilation) for both sides.
 */
public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderPlayer;

    // Base breast parts
    private ModelRenderer leftBreastFront, rightBreastFront;

    // Wear (hat) overlays: front and underside
    private ModelRenderer leftBreastWearFront, rightBreastWearFront;
    private ModelRenderer leftBreastWearBottom, rightBreastWearBottom;

    private BreastPhysics leftBreastPhysics, rightBreastPhysics;
    private Breasts breasts;

    private static final float TORSO_BASE_Y = -6.0F;
    private static final float CHEST_OFFSET_Y = 4.0F;

    private static final ConcurrentHashMap<UUID, BreastPhysics[]> PHYSICS_MAP = new ConcurrentHashMap<>();

    public GenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        this.leftBreastPhysics = new BreastPhysics();
        this.rightBreastPhysics = new BreastPhysics();

        ModelBiped modelBiped = (ModelBiped) renderPlayer.getMainModel();

        // Base geometry (front). Use distinct UVs per side to match torso left/right.
        // Left breast: use UV (16,16)
        this.leftBreastFront = new ModelRenderer(modelBiped, 16, 16);
        this.leftBreastFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4);
        this.leftBreastFront.setTextureSize(64, 64);
        this.leftBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);

        // Right breast: use UV (32,16) and DO NOT mirror the texture; geometry will be placed on the right
        this.rightBreastFront = new ModelRenderer(modelBiped, 32, 16);
        this.rightBreastFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4);
        this.rightBreastFront.setTextureSize(64, 64);
        this.rightBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);
        this.rightBreastFront.mirror = false;

        // Wear overlays (front-facing, slight dilation like hat layer)
        this.leftBreastWearFront = new ModelRenderer(modelBiped, 16, 32);
        this.leftBreastWearFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4, 0.25F);
        this.leftBreastWearFront.setTextureSize(64, 64);
        this.leftBreastWearFront.rotateAngleX = this.leftBreastFront.rotateAngleX;

        this.rightBreastWearFront = new ModelRenderer(modelBiped, 32, 32);
        this.rightBreastWearFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4, 0.25F);
        this.rightBreastWearFront.setTextureSize(64, 64);
        this.rightBreastWearFront.rotateAngleX = this.rightBreastFront.rotateAngleX;

        // Underboob wear overlays: slightly dilated and offset downward a touch to imply the underside "second layer"
        // Left underside uses a nearby UV block; adjust if you have custom UVs
        this.leftBreastWearBottom = new ModelRenderer(modelBiped, 16, 40);
        this.leftBreastWearBottom.addBox(-2.0F, -1.5F, 0.0F, 4, 3, 4, 0.30F);
        this.leftBreastWearBottom.setTextureSize(64, 64);
        this.leftBreastWearBottom.rotateAngleX = this.leftBreastFront.rotateAngleX + (float) Math.toRadians(4.0);

        // Right underside with right-side UVs
        this.rightBreastWearBottom = new ModelRenderer(modelBiped, 32, 40);
        this.rightBreastWearBottom.addBox(-2.0F, -1.5F, 0.0F, 4, 3, 4, 0.30F);
        this.rightBreastWearBottom.setTextureSize(64, 64);
        this.rightBreastWearBottom.rotateAngleX = this.rightBreastFront.rotateAngleX + (float) Math.toRadians(4.0);
    }

    private void ensureRegistered(AbstractClientPlayer player) {
        PHYSICS_MAP.putIfAbsent(player.getUniqueID(), new BreastPhysics[]{leftBreastPhysics, rightBreastPhysics});
    }

    public static BreastPhysics[] getPhysics(AbstractClientPlayer player) {
        return PHYSICS_MAP.get(player.getUniqueID());
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {
        if (!shouldRenderBreasts(player)) return;
        ensureRegistered(player);

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        if (settings == null) return;

        this.breasts = new Breasts(player);

        // Proper scaling: sideways (X) and forward (Z) only
        float breastScale = 1.0F + (settings.breastSize / 100.0F) * 0.5F;

        // Separation uses per-mod sliders, keep as provided
        float separation = settings.separation / 30.0F - settings.breastsCleavage / 60.0F
                + 0.0625F + breasts.getCleavage() + 0.125F + 0.625F;

        float depth = -settings.depth / 10.0F;

        // Invert height: positive moves up
        float height = -(settings.height / 40.0F) + 2.0F;

        // Physics update
        if (settings.physicsEnabled) {
            if (settings.breastsUniboob) {
                this.leftBreastPhysics.update(player, settings.breastSize, settings.intensity, settings.momentum);
                this.rightBreastPhysics = this.leftBreastPhysics;
            } else {
                this.leftBreastPhysics.update(player, settings.breastSize, settings.intensity, settings.momentum);
                this.rightBreastPhysics.update(player, settings.breastSize, settings.intensity, settings.momentum);
            }
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, player);
        }

        ModelBiped modelBiped = (ModelBiped) renderPlayer.getMainModel();
        ModelRenderer torso = modelBiped.bipedBody;
        float renderScale = 0.0625F;

        // Bind skin to avoid item texture bleed
        ResourceLocation skin = player.getLocationSkin();
        renderPlayer.bindTexture(skin);

        GlStateManager.pushMatrix();
        torso.postRender(renderScale);

        // Centering fix: shift slightly left so both breasts are evenly centered
        float baseX = breasts.getXOffset() - 0.125F;
        float baseY = TORSO_BASE_Y + CHEST_OFFSET_Y + height + 2.0F;
        float baseZ = breasts.getZOffset() + depth - 1.5F;

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        // Left breast (front)
        GlStateManager.pushMatrix();
        this.leftBreastFront.setRotationPoint(baseX - separation, baseY, baseZ);
        GlStateManager.translate(this.leftBreastFront.rotationPointX * renderScale, this.leftBreastFront.rotationPointY * renderScale, this.leftBreastFront.rotationPointZ * renderScale);
        GlStateManager.scale(breastScale, 1.0F, breastScale);
        GlStateManager.translate(-this.leftBreastFront.rotationPointX * renderScale, -this.leftBreastFront.rotationPointY * renderScale, -this.leftBreastFront.rotationPointZ * renderScale);
        this.leftBreastFront.render(renderScale);
        GlStateManager.popMatrix();

        // Right breast (front) — uses right-side UVs, no mirrored texture
        GlStateManager.pushMatrix();
        this.rightBreastFront.setRotationPoint(baseX + separation, baseY, baseZ);
        GlStateManager.translate(this.rightBreastFront.rotationPointX * renderScale, this.rightBreastFront.rotationPointY * renderScale, this.rightBreastFront.rotationPointZ * renderScale);
        GlStateManager.scale(breastScale, 1.0F, breastScale);
        GlStateManager.translate(-this.rightBreastFront.rotationPointX * renderScale, -this.rightBreastFront.rotationPointY * renderScale, -this.rightBreastFront.rotationPointZ * renderScale);
        this.rightBreastFront.render(renderScale);
        GlStateManager.popMatrix();

        // Wear overlays only if jacket layer is enabled
        if (player.isWearing(EnumPlayerModelParts.JACKET)) {
            // Left wear (front)
            GlStateManager.pushMatrix();
            this.leftBreastWearFront.setRotationPoint(baseX - separation, baseY, baseZ);
            GlStateManager.translate(this.leftBreastWearFront.rotationPointX * renderScale, this.leftBreastWearFront.rotationPointY * renderScale, this.leftBreastWearFront.rotationPointZ * renderScale);
            GlStateManager.scale(breastScale, 1.0F, breastScale);
            GlStateManager.translate(-this.leftBreastWearFront.rotationPointX * renderScale, -this.leftBreastWearFront.rotationPointY * renderScale, -this.leftBreastWearFront.rotationPointZ * renderScale);
            this.leftBreastWearFront.render(renderScale);
            GlStateManager.popMatrix();

            // Right wear (front)
            GlStateManager.pushMatrix();
            this.rightBreastWearFront.setRotationPoint(baseX + separation, baseY, baseZ);
            GlStateManager.translate(this.rightBreastWearFront.rotationPointX * renderScale, this.rightBreastWearFront.rotationPointY * renderScale, this.rightBreastWearFront.rotationPointZ * renderScale);
            GlStateManager.scale(breastScale, 1.0F, breastScale);
            GlStateManager.translate(-this.rightBreastWearFront.rotationPointX * renderScale, -this.rightBreastWearFront.rotationPointY * renderScale, -this.rightBreastWearFront.rotationPointZ * renderScale);
            this.rightBreastWearFront.render(renderScale);
            GlStateManager.popMatrix();

            // Left wear (bottom/underboob)
            GlStateManager.pushMatrix();
            this.leftBreastWearBottom.setRotationPoint(baseX - separation, baseY + 0.5F, baseZ);
            GlStateManager.translate(this.leftBreastWearBottom.rotationPointX * renderScale, this.leftBreastWearBottom.rotationPointY * renderScale, this.leftBreastWearBottom.rotationPointZ * renderScale);
            GlStateManager.scale(breastScale, 1.0F, breastScale);
            GlStateManager.translate(-this.leftBreastWearBottom.rotationPointX * renderScale, -this.leftBreastWearBottom.rotationPointY * renderScale, -this.leftBreastWearBottom.rotationPointZ * renderScale);
            this.leftBreastWearBottom.render(renderScale);
            GlStateManager.popMatrix();

            // Right wear (bottom/underboob)
            GlStateManager.pushMatrix();
            this.rightBreastWearBottom.setRotationPoint(baseX + separation, baseY + 0.5F, baseZ);
            GlStateManager.translate(this.rightBreastWearBottom.rotationPointX * renderScale, this.rightBreastWearBottom.rotationPointY * renderScale, this.rightBreastWearBottom.rotationPointZ * renderScale);
            GlStateManager.scale(breastScale, 1.0F, breastScale);
            GlStateManager.translate(-this.rightBreastWearBottom.rotationPointX * renderScale, -this.rightBreastWearBottom.rotationPointY * renderScale, -this.rightBreastWearBottom.rotationPointZ * renderScale);
            this.rightBreastWearBottom.render(renderScale);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                   float headYaw, float headPitch, float scale, AbstractClientPlayer entity) {
        if (!shouldRenderBreasts(entity)) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        if (!settings.physicsEnabled) return;

        float leftBounceY = leftBreastPhysics.getLeftPositionY();
        float rightBounceY = settings.breastsUniboob ? leftBreastPhysics.getRightPositionY() : rightBreastPhysics.getRightPositionY();
        float leftBounceRotation = leftBreastPhysics.getBounceRotation();
        float rightBounceRotation = settings.breastsUniboob ? -leftBounceRotation : -rightBreastPhysics.getBounceRotation();

        float baseY = TORSO_BASE_Y;

        // Y offsets
        this.leftBreastFront.rotationPointY = baseY + leftBounceY;
        this.rightBreastFront.rotationPointY = baseY + rightBounceY;

        // Keep wear aligned with base
        this.leftBreastWearFront.rotationPointY = this.leftBreastFront.rotationPointY;
        this.rightBreastWearFront.rotationPointY = this.rightBreastFront.rotationPointY;
        this.leftBreastWearBottom.rotationPointY = this.leftBreastFront.rotationPointY + 0.5F;
        this.rightBreastWearBottom.rotationPointY = this.rightBreastFront.rotationPointY + 0.5F;

        // Pitch from bounce
        float basePitch = (float) Math.toRadians(-26.92285);
        this.leftBreastFront.rotateAngleX = basePitch + leftBounceRotation - 0.1f;
        this.rightBreastFront.rotateAngleX = basePitch + rightBounceRotation - 0.1f;

        this.leftBreastWearFront.rotateAngleX = this.leftBreastFront.rotateAngleX;
        this.rightBreastWearFront.rotateAngleX = this.rightBreastFront.rotateAngleX;

        // Underboob overlays follow with slight downward bias
        this.leftBreastWearBottom.rotateAngleX = this.leftBreastFront.rotateAngleX + (float) Math.toRadians(4.0);
        this.rightBreastWearBottom.rotateAngleX = this.rightBreastFront.rotateAngleX + (float) Math.toRadians(4.0);

        // Subtle yaw sway from physics X offsets
        float leftYaw = leftBreastPhysics.getLeftPositionX() * 0.3f;
        float rightYaw = -(settings.breastsUniboob ? leftBreastPhysics.getRightPositionX() : rightBreastPhysics.getRightPositionX()) * 0.3f;

        this.leftBreastFront.rotateAngleY = leftYaw;
        this.rightBreastFront.rotateAngleY = rightYaw;

        this.leftBreastWearFront.rotateAngleY = leftYaw;
        this.rightBreastWearFront.rotateAngleY = rightYaw;
        this.leftBreastWearBottom.rotateAngleY = leftYaw;
        this.rightBreastWearBottom.rotateAngleY = rightYaw;
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer entity) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        return settings != null && settings.breastsEnabled
                && ("Female".equals(settings.gender) || "Other".equals(settings.gender));
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}