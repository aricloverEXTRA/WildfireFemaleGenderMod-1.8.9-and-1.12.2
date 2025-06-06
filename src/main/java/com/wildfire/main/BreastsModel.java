package com.wildfire.main;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.physics.BreastPhysics;

public class BreastsModel extends ModelBase {
    private ModelRenderer leftBreastFront;
    private ModelRenderer leftBreastLeft;
    private ModelRenderer leftBreastRight;
    private ModelRenderer leftBreastBack;
    private ModelRenderer leftBreastTop;
    private ModelRenderer leftBreastBottom;
    private ModelRenderer leftBreastNipple;

    private ModelRenderer rightBreastFront;
    private ModelRenderer rightBreastLeft;
    private ModelRenderer rightBreastRight;
    private ModelRenderer rightBreastBack;
    private ModelRenderer rightBreastTop;
    private ModelRenderer rightBreastBottom;
    private ModelRenderer rightBreastNipple;

    private ModelRenderer uniBreastFront;
    private ModelRenderer uniBreastLeft;
    private ModelRenderer uniBreastRight;
    private ModelRenderer uniBreastBack;
    private ModelRenderer uniBreastTop;
    private ModelRenderer uniBreastBottom;
    private ModelRenderer uniBreastNipple;

    private BreastPhysics breastPhysics;
    private Breasts breasts;

    public BreastsModel() {
        this.breastPhysics = new BreastPhysics();

        float leftBreastX = -2.0F;
        float leftBreastY = 18.5F;
        float leftBreastZ = -1.5F;

        this.leftBreastFront = new ModelRenderer(this, 20, 20);
        this.leftBreastFront.addBox(-1.5F, -2.0F, -2.0F, 3, 5, 0);
        this.leftBreastFront.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastFront.setTextureSize(64, 32);

        this.leftBreastLeft = new ModelRenderer(this, 16, 20);
        this.leftBreastLeft.addBox(-1.5F, -2.0F, -2.0F, 0, 5, 3);
        this.leftBreastLeft.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastLeft.setTextureSize(64, 32);

        this.leftBreastRight = new ModelRenderer(this, 28, 20);
        this.leftBreastRight.addBox(1.5F, -2.0F, -2.0F, 0, 5, 3);
        this.leftBreastRight.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastRight.setTextureSize(64, 32);

        this.leftBreastBack = new ModelRenderer(this, 32, 20);
        this.leftBreastBack.addBox(-1.5F, -2.0F, 1.0F, 3, 5, 0);
        this.leftBreastBack.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastBack.setTextureSize(64, 32);

        this.leftBreastTop = new ModelRenderer(this, 20, 16);
        this.leftBreastTop.addBox(-1.5F, -2.0F, -2.0F, 3, 0, 3);
        this.leftBreastTop.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastTop.setTextureSize(64, 32);

        this.leftBreastBottom = new ModelRenderer(this, 20, 32);
        this.leftBreastBottom.addBox(-1.5F, 3.0F, -2.0F, 3, 0, 3);
        this.leftBreastBottom.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastBottom.setTextureSize(64, 32);

        this.leftBreastNipple = new ModelRenderer(this, 24, 20);
        this.leftBreastNipple.addBox(-1.0F, 0.0F, -2.5F, 2, 2, 1);
        this.leftBreastNipple.setRotationPoint(leftBreastX, leftBreastY, leftBreastZ);
        this.leftBreastNipple.setTextureSize(64, 32);

        float rightBreastX = 2.0F;
        float rightBreastY = 18.5F;
        float rightBreastZ = -1.5F;

        this.rightBreastFront = new ModelRenderer(this, 20, 20);
        this.rightBreastFront.addBox(-1.5F, -2.0F, -2.0F, 3, 5, 0);
        this.rightBreastFront.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastFront.setTextureSize(64, 32);

        this.rightBreastLeft = new ModelRenderer(this, 16, 20);
        this.rightBreastLeft.addBox(-1.5F, -2.0F, -2.0F, 0, 5, 3);
        this.rightBreastLeft.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastLeft.setTextureSize(64, 32);

        this.rightBreastRight = new ModelRenderer(this, 28, 20);
        this.rightBreastRight.addBox(1.5F, -2.0F, -2.0F, 0, 5, 3);
        this.rightBreastRight.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastRight.setTextureSize(64, 32);

        this.rightBreastBack = new ModelRenderer(this, 32, 20);
        this.rightBreastBack.addBox(-1.5F, -2.0F, 1.0F, 3, 5, 0);
        this.rightBreastBack.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastBack.setTextureSize(64, 32);

        this.rightBreastTop = new ModelRenderer(this, 20, 16);
        this.rightBreastTop.addBox(-1.5F, -2.0F, -2.0F, 3, 0, 3);
        this.rightBreastTop.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastTop.setTextureSize(64, 32);

        this.rightBreastBottom = new ModelRenderer(this, 20, 32);
        this.rightBreastBottom.addBox(-1.5F, 3.0F, -2.0F, 3, 0, 3);
        this.rightBreastBottom.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastBottom.setTextureSize(64, 32);

        this.rightBreastNipple = new ModelRenderer(this, 24, 20);
        this.rightBreastNipple.addBox(-1.0F, 0.0F, -2.5F, 2, 2, 1);
        this.rightBreastNipple.setRotationPoint(rightBreastX, rightBreastY, rightBreastZ);
        this.rightBreastNipple.setTextureSize(64, 32);

        float uniBreastX = 0.0F;
        float uniBreastY = 18.5F;
        float uniBreastZ = -1.5F;

        this.uniBreastFront = new ModelRenderer(this, 20, 20);
        this.uniBreastFront.addBox(-3.0F, -2.0F, -2.0F, 6, 5, 0);
        this.uniBreastFront.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastFront.setTextureSize(64, 32);

        this.uniBreastLeft = new ModelRenderer(this, 16, 20);
        this.uniBreastLeft.addBox(-3.0F, -2.0F, -2.0F, 0, 5, 3);
        this.uniBreastLeft.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastLeft.setTextureSize(64, 32);

        this.uniBreastRight = new ModelRenderer(this, 28, 20);
        this.uniBreastRight.addBox(3.0F, -2.0F, -2.0F, 0, 5, 3);
        this.uniBreastRight.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastRight.setTextureSize(64, 32);

        this.uniBreastBack = new ModelRenderer(this, 32, 20);
        this.uniBreastBack.addBox(-3.0F, -2.0F, 1.0F, 6, 5, 0);
        this.uniBreastBack.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastBack.setTextureSize(64, 32);

        this.uniBreastTop = new ModelRenderer(this, 20, 16);
        this.uniBreastTop.addBox(-3.0F, -2.0F, -2.0F, 6, 0, 3);
        this.uniBreastTop.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastTop.setTextureSize(64, 32);

        this.uniBreastBottom = new ModelRenderer(this, 20, 32);
        this.uniBreastBottom.addBox(-3.0F, 3.0F, -2.0F, 6, 0, 3);
        this.uniBreastBottom.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastBottom.setTextureSize(64, 32);

        this.uniBreastNipple = new ModelRenderer(this, 24, 20);
        this.uniBreastNipple.addBox(-2.0F, 0.0F, -2.5F, 4, 2, 1);
        this.uniBreastNipple.setRotationPoint(uniBreastX, uniBreastY, uniBreastZ);
        this.uniBreastNipple.setTextureSize(64, 32);
    }

    private void applyTextures(AbstractClientPlayer player) {
        ResourceLocation skin = player.getLocationSkin();
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (shouldRenderBreasts(entity)) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            applyTextures(player);
            if (breasts == null) {
                breasts = new Breasts((EntityPlayer) player);
            }

            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((EntityPlayer) entity);
            if (settings == null) return;

            float breastSize = 1.0F + settings.breastSize / 150.0F;
            float separation = settings.separation / 20.0F - settings.breastsCleavage / 40.0F;
            float depth = settings.depth / 15.0F;
            float height = settings.height / 30.0F;
            float rotation = settings.rotation * (float) Math.PI / 180.0F;

            this.breastPhysics.update(player, settings.breastSize, settings.bounceMultiplier);
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

            if (breasts.isUniboob()) {
                this.uniBreastFront.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastLeft.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastRight.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastBack.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastTop.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastBottom.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);
                this.uniBreastNipple.setRotationPoint(0.0F, 18.5F + height, -1.5F + depth);

                GlStateManager.pushMatrix();
                GlStateManager.translate(this.uniBreastFront.rotationPointX * scaleFactor, this.uniBreastFront.rotationPointY * scaleFactor, this.uniBreastFront.rotationPointZ * scaleFactor);
                GlStateManager.scale(breastSize, breastSize * 0.9f, breastSize * 1.1f);
                GlStateManager.translate(-this.uniBreastFront.rotationPointX * scaleFactor, -this.uniBreastFront.rotationPointY * scaleFactor, -this.uniBreastFront.rotationPointZ * scaleFactor);
                GlStateManager.enableLighting();
                this.uniBreastFront.render(scaleFactor);
                this.uniBreastLeft.render(scaleFactor);
                this.uniBreastRight.render(scaleFactor);
                this.uniBreastBack.render(scaleFactor);
                this.uniBreastTop.render(scaleFactor);
                this.uniBreastBottom.render(scaleFactor);
                this.uniBreastNipple.render(scaleFactor);
                GlStateManager.popMatrix();
            } else {
                this.leftBreastFront.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastLeft.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastRight.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastBack.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastTop.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastBottom.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);
                this.leftBreastNipple.setRotationPoint(-2.0F - separation, 18.5F + height, -1.5F + depth);

                this.rightBreastFront.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastLeft.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastRight.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastBack.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastTop.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastBottom.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);
                this.rightBreastNipple.setRotationPoint(2.0F + separation, 18.5F + height, -1.5F + depth);

                GlStateManager.pushMatrix();
                GlStateManager.translate(this.leftBreastFront.rotationPointX * scaleFactor, this.leftBreastFront.rotationPointY * scaleFactor, this.leftBreastFront.rotationPointZ * scaleFactor);
                GlStateManager.scale(breastSize, breastSize * 0.9f, breastSize * 1.1f);
                GlStateManager.translate(-this.leftBreastFront.rotationPointX * scaleFactor, -this.leftBreastFront.rotationPointY * scaleFactor, -this.leftBreastFront.rotationPointZ * scaleFactor);
                GlStateManager.enableLighting();
                this.leftBreastFront.rotateAngleZ = rotation;
                this.leftBreastFront.render(scaleFactor);
                this.leftBreastLeft.rotateAngleZ = rotation;
                this.leftBreastLeft.render(scaleFactor);
                this.leftBreastRight.rotateAngleZ = rotation;
                this.leftBreastRight.render(scaleFactor);
                this.leftBreastBack.rotateAngleZ = rotation;
                this.leftBreastBack.render(scaleFactor);
                this.leftBreastTop.rotateAngleZ = rotation;
                this.leftBreastTop.render(scaleFactor);
                this.leftBreastBottom.rotateAngleZ = rotation;
                this.leftBreastBottom.render(scaleFactor);
                this.leftBreastNipple.rotateAngleZ = rotation;
                this.leftBreastNipple.render(scaleFactor);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate(this.rightBreastFront.rotationPointX * scaleFactor, this.rightBreastFront.rotationPointY * scaleFactor, this.rightBreastFront.rotationPointZ * scaleFactor);
                GlStateManager.scale(breastSize * 0.98f, breastSize * 0.9f, breastSize * 1.1f);
                GlStateManager.translate(-this.rightBreastFront.rotationPointX * scaleFactor, -this.rightBreastFront.rotationPointY * scaleFactor, -this.rightBreastFront.rotationPointZ * scaleFactor);
                this.rightBreastFront.rotateAngleZ = -rotation;
                this.rightBreastFront.render(scaleFactor);
                this.rightBreastLeft.rotateAngleZ = -rotation;
                this.rightBreastLeft.render(scaleFactor);
                this.rightBreastRight.rotateAngleZ = -rotation;
                this.rightBreastRight.render(scaleFactor);
                this.rightBreastBack.rotateAngleZ = -rotation;
                this.rightBreastBack.render(scaleFactor);
                this.rightBreastTop.rotateAngleZ = -rotation;
                this.rightBreastTop.render(scaleFactor);
                this.rightBreastBottom.rotateAngleZ = -rotation;
                this.rightBreastBottom.render(scaleFactor);
                this.rightBreastNipple.rotateAngleZ = -rotation;
                this.rightBreastNipple.render(scaleFactor);
                GlStateManager.popMatrix();
            }
        }
    }

    private boolean shouldRenderBreasts(Entity entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(player);
        return settings != null && settings.breastsEnabled && "Female".equals(settings.gender);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float bounceX = breastPhysics.getPositionX();
        float bounceY = breastPhysics.getPositionY();
        float bounceRotation = breastPhysics.getBounceRotation();

        if (breasts == null || !shouldRenderBreasts(entityIn)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entityIn;
        float playerYaw = (float) Math.toRadians(player.rotationYaw);

        if (breasts.isUniboob()) {
            this.uniBreastFront.rotateAngleY = playerYaw;
            this.uniBreastLeft.rotateAngleY = playerYaw;
            this.uniBreastRight.rotateAngleY = playerYaw;
            this.uniBreastBack.rotateAngleY = playerYaw;
            this.uniBreastTop.rotateAngleY = playerYaw;
            this.uniBreastBottom.rotateAngleY = playerYaw;
            this.uniBreastNipple.rotateAngleY = playerYaw;

            this.uniBreastFront.rotationPointY = 18.5F + bounceY;
            this.uniBreastLeft.rotationPointY = 18.5F + bounceY;
            this.uniBreastRight.rotationPointY = 18.5F + bounceY;
            this.uniBreastBack.rotationPointY = 18.5F + bounceY;
            this.uniBreastTop.rotationPointY = 18.5F + bounceY;
            this.uniBreastBottom.rotationPointY = 18.5F + bounceY;
            this.uniBreastNipple.rotationPointY = 18.5F + bounceY;

            this.uniBreastFront.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastLeft.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastRight.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastBack.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastTop.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastBottom.rotateAngleX = bounceRotation - 0.1f;
            this.uniBreastNipple.rotateAngleX = bounceRotation - 0.1f;
        } else {
            this.leftBreastFront.rotateAngleY = playerYaw;
            this.leftBreastLeft.rotateAngleY = playerYaw;
            this.leftBreastRight.rotateAngleY = playerYaw;
            this.leftBreastBack.rotateAngleY = playerYaw;
            this.leftBreastTop.rotateAngleY = playerYaw;
            this.leftBreastBottom.rotateAngleY = playerYaw;
            this.leftBreastNipple.rotateAngleY = playerYaw;

            this.leftBreastFront.rotationPointY = 18.5F + bounceY;
            this.leftBreastLeft.rotationPointY = 18.5F + bounceY;
            this.leftBreastRight.rotationPointY = 18.5F + bounceY;
            this.leftBreastBack.rotationPointY = 18.5F + bounceY;
            this.leftBreastTop.rotationPointY = 18.5F + bounceY;
            this.leftBreastBottom.rotationPointY = 18.5F + bounceY;
            this.leftBreastNipple.rotationPointY = 18.5F + bounceY;

            this.leftBreastFront.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastLeft.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastRight.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastBack.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastTop.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastBottom.rotateAngleX = bounceRotation - 0.1f;
            this.leftBreastNipple.rotateAngleX = bounceRotation - 0.1f;

            this.rightBreastFront.rotateAngleY = playerYaw;
            this.rightBreastLeft.rotateAngleY = playerYaw;
            this.rightBreastRight.rotateAngleY = playerYaw;
            this.rightBreastBack.rotateAngleY = playerYaw;
            this.rightBreastTop.rotateAngleY = playerYaw;
            this.rightBreastBottom.rotateAngleY = playerYaw;
            this.rightBreastNipple.rotateAngleY = playerYaw;

            this.rightBreastFront.rotationPointY = 18.5F + bounceY;
            this.rightBreastLeft.rotationPointY = 18.5F + bounceY;
            this.rightBreastRight.rotationPointY = 18.5F + bounceY;
            this.rightBreastBack.rotationPointY = 18.5F + bounceY;
            this.rightBreastTop.rotationPointY = 18.5F + bounceY;
            this.rightBreastBottom.rotationPointY = 18.5F + bounceY;
            this.rightBreastNipple.rotationPointY = 18.5F + bounceY;

            this.rightBreastFront.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastLeft.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastRight.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastBack.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastTop.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastBottom.rotateAngleX = bounceRotation - 0.1f;
            this.rightBreastNipple.rotateAngleX = bounceRotation - 0.1f;
        }
    }
}