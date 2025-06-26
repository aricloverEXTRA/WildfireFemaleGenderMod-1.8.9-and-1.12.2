package com.wildfire.main;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;

public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderPlayer;
    private ModelRenderer leftBreastFront;
    private ModelRenderer rightBreastFront;
    private BreastPhysics leftBreastPhysics;
    private BreastPhysics rightBreastPhysics;
    private Breasts breasts;

    private static final float TORSO_BASE_Y = -6.0F;
    private static final float CHEST_OFFSET_Y = 4.0F;

    public GenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        this.leftBreastPhysics = new BreastPhysics();
        this.rightBreastPhysics = new BreastPhysics();

        ModelBiped modelBiped = (ModelBiped) renderPlayer.getMainModel();
        ModelRenderer torso = modelBiped.bipedBody;

        this.leftBreastFront = new ModelRenderer(modelBiped, 16, 16);
        this.leftBreastFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4);
        this.leftBreastFront.setTextureSize(64, 32);
        this.leftBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);
        this.leftBreastFront.mirror = false;

        this.rightBreastFront = new ModelRenderer(modelBiped, 32, 16);
        this.rightBreastFront.addBox(-2.0F, -2.0F, 0.0F, 4, 5, 4);
        this.rightBreastFront.setTextureSize(64, 32);
        this.rightBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);
        this.rightBreastFront.mirror = true;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {
        if (!shouldRenderBreasts(entitylivingbaseIn)) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entitylivingbaseIn);
        if (settings == null) return;

        this.breasts = new Breasts(entitylivingbaseIn);

        float breastSize = 1.0F + (settings.breastSize / 100.0F) * 0.5F;
        float separation = settings.separation / 30.0F - settings.breastsCleavage / 60.0F + 0.0625F + breasts.getCleavage() + 0.125F + 0.625F;
        float depth = -settings.depth / 10.0F;
        float height = settings.height / 40.0F + 2.0F;

        if (settings.physicsEnabled) {
            if (settings.breastsUniboob) {
                this.leftBreastPhysics.update(entitylivingbaseIn, settings.breastSize, settings.intensity, settings.momentum);
                this.rightBreastPhysics = this.leftBreastPhysics;
            } else {
                this.leftBreastPhysics.update(entitylivingbaseIn, settings.breastSize, settings.intensity, settings.momentum);
                this.rightBreastPhysics.update(entitylivingbaseIn, settings.breastSize, settings.intensity, settings.momentum);
            }
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entitylivingbaseIn);
        }

        ModelBiped modelBiped = (ModelBiped) renderPlayer.getMainModel();
        ModelRenderer torso = modelBiped.bipedBody;

        float renderScale = 0.0625F;

        GlStateManager.pushMatrix();
        torso.postRender(renderScale);

        float baseX = breasts.getXOffset() + 0.0625F;
        float baseY = TORSO_BASE_Y + CHEST_OFFSET_Y + height + 2.0F;
        float baseZ = breasts.getZOffset() + depth - 1.5F;

        // Left breast
        GlStateManager.pushMatrix();
        this.leftBreastFront.setRotationPoint(baseX - separation, baseY, baseZ);
        GlStateManager.translate(this.leftBreastFront.rotationPointX * renderScale, this.leftBreastFront.rotationPointY * renderScale, this.leftBreastFront.rotationPointZ * renderScale);
        GlStateManager.scale(1.0F, breastSize, breastSize * 0.9F);
        GlStateManager.translate(-this.leftBreastFront.rotationPointX * renderScale, -this.leftBreastFront.rotationPointY * renderScale, -this.leftBreastFront.rotationPointZ * renderScale);
        this.leftBreastFront.render(renderScale);
        GlStateManager.popMatrix();

        // Right breast
        GlStateManager.pushMatrix();
        this.rightBreastFront.setRotationPoint(baseX + separation, baseY, baseZ);
        GlStateManager.translate(this.rightBreastFront.rotationPointX * renderScale, this.rightBreastFront.rotationPointY * renderScale, this.rightBreastFront.rotationPointZ * renderScale);
        GlStateManager.scale(1.0F, breastSize, breastSize * 0.9F);
        GlStateManager.translate(-this.rightBreastFront.rotationPointX * renderScale, -this.rightBreastFront.rotationPointY * renderScale, -this.rightBreastFront.rotationPointZ * renderScale);
        this.rightBreastFront.render(renderScale);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    private void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale, AbstractClientPlayer entity) {
        if (!shouldRenderBreasts(entity)) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        if (!settings.physicsEnabled) return;

        float leftBounceY = leftBreastPhysics.getLeftPositionY();
        float rightBounceY = settings.breastsUniboob ? leftBreastPhysics.getRightPositionY() : rightBreastPhysics.getRightPositionY();
        float leftBounceRotation = leftBreastPhysics.getBounceRotation();
        float rightBounceRotation = settings.breastsUniboob ? -leftBounceRotation : -rightBreastPhysics.getBounceRotation(); // Invert for right

        float baseY = TORSO_BASE_Y;
        this.leftBreastFront.rotationPointY = baseY + leftBounceY;
        this.rightBreastFront.rotationPointY = baseY + rightBounceY;

        this.leftBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285) + leftBounceRotation - 0.1f;
        this.rightBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285) + rightBounceRotation - 0.1f;
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer entity) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        return settings != null && settings.breastsEnabled && ("Female".equals(settings.gender) || "Other".equals(settings.gender));
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}