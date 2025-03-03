package com.wildfire.main;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import com.wildfire.main.config.ConfigSettings;
import com.wildfire.physics.BreastPhysics;

public class BreastsModel extends ModelBase {
    private ModelRenderer leftBreast;
    private ModelRenderer rightBreast;
    private BreastPhysics breastPhysics;
    private Breasts breasts;
    private float preBreastSize = 0f;

    public BreastsModel() {
        this.breasts = new Breasts();
        this.breastPhysics = new BreastPhysics();

        this.leftBreast = new ModelRenderer(this, 0, 0);
        this.leftBreast.addBox(-1.0F, -2.0F, -2.0F, 3, 4, 2);
        this.leftBreast.addBox(-1.0F, -1.0F, -3.0F, 3, 2, 2);
        this.leftBreast.setRotationPoint(-2.5F, 19.0F, -2.0F);

        this.rightBreast = new ModelRenderer(this, 0, 0);
        this.rightBreast.addBox(-1.0F, -2.0F, -2.0F, 3, 4, 2);
        this.rightBreast.addBox(-1.0F, -1.0F, -3.0F, 3, 2, 2);
        this.rightBreast.setRotationPoint(2.5F, 19.0F, -2.0F);
    }

    private void applyPlayerTorsoTexture(AbstractClientPlayer player) {
        ResourceLocation skin = player.getLocationSkin();
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

        int torsoTextureX = 32;
        int torsoTextureY = 16;

        this.leftBreast.setTextureOffset(torsoTextureX + 4, torsoTextureY);
        this.rightBreast.setTextureOffset(torsoTextureX, torsoTextureY);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (shouldRenderBreasts(entity)) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            applyPlayerTorsoTexture(player);

            float breastSize = 1.0F + ConfigSettings.breastSize / 200.0F;
            float separation = ConfigSettings.separation / 20.0F;
            float depth = ConfigSettings.depth / 20.0F;
            float height = ConfigSettings.height / 40.0F;
            float rotation = ConfigSettings.rotation * (float) Math.PI / 180.0F;

            this.breastPhysics.update(player, ConfigSettings.breastSize, ConfigSettings.bounceMultiplier);
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

            this.leftBreast.setRotationPoint(-1.0F - separation, 19.0F + height, -2.0F + depth);
            this.rightBreast.setRotationPoint(0.0F + separation, 19.0F + height, -2.0F + depth);

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftBreast.rotationPointX * scaleFactor, this.leftBreast.rotationPointY * scaleFactor, this.leftBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, 1.0F);
            GlStateManager.translate(-this.leftBreast.rotationPointX * scaleFactor, -this.leftBreast.rotationPointY * scaleFactor, -this.leftBreast.rotationPointZ * scaleFactor);
            this.leftBreast.rotateAngleZ = rotation;
            this.leftBreast.rotateAngleX = -0.2F;
            this.leftBreast.render(scaleFactor);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rightBreast.rotationPointX * scaleFactor, this.rightBreast.rotationPointY * scaleFactor, this.rightBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, 1.0F);
            GlStateManager.translate(-this.rightBreast.rotationPointX * scaleFactor, -this.rightBreast.rotationPointY * scaleFactor, -this.rightBreast.rotationPointZ * scaleFactor);
            this.rightBreast.rotateAngleZ = -rotation;
            this.rightBreast.rotateAngleX = -0.2F;
            this.rightBreast.render(scaleFactor);
            GlStateManager.popMatrix();
        }
    }

    private boolean shouldRenderBreasts(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float bounceX = breastPhysics.getPositionX();
        float bounceY = breastPhysics.getPositionY();
        float bounceRotation = breastPhysics.getBounceRotation();

        this.leftBreast.rotateAngleY = bounceX;
        this.leftBreast.rotationPointY = 19.0F + bounceY;
        this.leftBreast.rotateAngleX = bounceRotation;

        this.rightBreast.rotateAngleY = bounceX;
        this.rightBreast.rotationPointY = 19.0F + bounceY;
        this.rightBreast.rotateAngleX = bounceRotation;
    }
}
