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

    public GenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        this.leftBreastPhysics = new BreastPhysics();
        this.rightBreastPhysics = new BreastPhysics();

        float torsoBaseY = 12.0F;
        float leftBreastX = -2.0F;
        float leftBreastY = torsoBaseY + 0.49325F;
        float leftBreastZ = 5.55645F;

        this.leftBreastFront = new ModelRenderer(renderPlayer.getMainModel(), 20, 20);
        this.leftBreastFront.addBox(0.02598F, 6.70457F, -2.0F, 4, 5, 4);
        this.leftBreastFront.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastFront.setTextureSize(64, 64);
        this.leftBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);

        float rightBreastX = 2.0F;
        float rightBreastY = torsoBaseY + 0.49325F;
        float rightBreastZ = 5.55645F;

        this.rightBreastFront = new ModelRenderer(renderPlayer.getMainModel(), 20, 20);
        this.rightBreastFront.addBox(-3.98792F, 6.70457F, -2.0F, 4, 5, 4);
        this.rightBreastFront.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastFront.setTextureSize(64, 64);
        this.rightBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285);
        this.rightBreastFront.mirror = true;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {
        if (shouldRenderBreasts(entitylivingbaseIn)) {
            GlStateManager.pushMatrix();
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entitylivingbaseIn);
            if (settings == null) {
                GlStateManager.popMatrix();
                return;
            }

            this.breasts = new Breasts(entitylivingbaseIn);

            ItemStack heldItem = entitylivingbaseIn.getHeldItem();
            boolean isHoldingItem = heldItem != null && heldItem.stackSize > 0;
            if (isHoldingItem) {
                GlStateManager.translate(0.0F, 0.5F, 0.0F);
            }

            float breastSize = 1.0F + (settings.breastSize / 200.0F);
            float separation = settings.separation / 30.0F - settings.breastsCleavage / 60.0F + 0.0625F + breasts.getCleavage();
            float depth = settings.depth / 20.0F;
            float height = settings.height / 40.0F;

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

            float baseX = -2.0F + breasts.getXOffset();
            float baseY = 12.0F + breasts.getYOffset();
            float baseZ = 5.55645F + breasts.getZOffset();

            this.leftBreastFront.setRotationPoint(baseX - separation, baseY + height, baseZ + depth);
            this.rightBreastFront.setRotationPoint(baseX + separation, baseY + height, baseZ + depth);

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftBreastFront.rotationPointX * scale, this.leftBreastFront.rotationPointY * scale, this.leftBreastFront.rotationPointZ * scale);
            GlStateManager.scale(breastSize, breastSize * 0.85f, breastSize * 0.9f);
            GlStateManager.translate(-this.leftBreastFront.rotationPointX * scale, -this.leftBreastFront.rotationPointY * scale, -this.leftBreastFront.rotationPointZ * scale);
            GlStateManager.enableLighting();
            this.leftBreastFront.render(scale);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rightBreastFront.rotationPointX * scale, this.rightBreastFront.rotationPointY * scale, this.rightBreastFront.rotationPointZ * scale);
            GlStateManager.scale(breastSize * 0.98f, breastSize * 0.85f, breastSize * 0.9f);
            GlStateManager.translate(-this.rightBreastFront.rotationPointX * scale, -this.rightBreastFront.rotationPointY * scale, -this.rightBreastFront.rotationPointZ * scale);
            this.rightBreastFront.render(scale);
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
        }
    }

    private void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale, AbstractClientPlayer entity) {
        if (!shouldRenderBreasts(entity)) return;

        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        if (!settings.physicsEnabled) return;

        float leftBounceY = leftBreastPhysics.getLeftPositionY();
        float rightBounceY = settings.breastsUniboob ? leftBreastPhysics.getRightPositionY() : rightBreastPhysics.getRightPositionY();
        float leftBounceRotation = leftBreastPhysics.getBounceRotation();
        float rightBounceRotation = settings.breastsUniboob ? leftBounceRotation : rightBreastPhysics.getBounceRotation();

        float baseY = 12.0F + breasts.getYOffset();
        this.leftBreastFront.rotationPointY = baseY + leftBounceY;
        this.rightBreastFront.rotationPointY = baseY + rightBounceY;

        this.leftBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285) + leftBounceRotation - 0.1f;
        this.rightBreastFront.rotateAngleX = (float) Math.toRadians(-26.92285) + rightBounceRotation - 0.1f;
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer entity) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(entity);
        return settings != null && settings.breastsEnabled && "Female".equals(settings.gender);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}