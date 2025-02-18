package com.wildfire.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import com.wildfire.config.ConfigSettings;

public class BreastsModel extends ModelBase {
    private ModelRenderer leftBreast;
    private ModelRenderer rightBreast;
    private BreastPhysics breastPhysics;
    private Breasts breasts;
    private float preBreastSize = 0f;

    public BreastsModel() {
        this.breasts = new Breasts();
        this.breastPhysics = new BreastPhysics();

        // Initialize the breast model renderers with original texture settings
        this.leftBreast = new ModelRenderer(this, 16, 16);
        this.leftBreast.addBox(-2.0F, -2.0F, -1.0F, 4, 4, 2);
        this.leftBreast.setRotationPoint(-1.5F, 8.0F, -2.0F);

        this.rightBreast = new ModelRenderer(this, 16, 16);
        this.rightBreast.addBox(-2.0F, -2.0F, -1.0F, 4, 4, 2);
        this.rightBreast.setRotationPoint(1.5F, 8.0F, -2.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (shouldRenderBreasts(entity)) {
            ResourceLocation skin = ((AbstractClientPlayer) entity).getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

            float breastSize = ConfigSettings.breastSize / 50.0F; // Scale to 0.0 to 2.0 range
            float separation = ConfigSettings.separation / 10.0F; // Normalize separation
            float depth = ConfigSettings.depth / 10.0F; // Normalize depth
            float height = ConfigSettings.height / 20.0F; // Subtle height adjustment
            float rotation = ConfigSettings.rotation * (float) Math.PI / 180.0F; // Convert to radians

            this.breastPhysics.update((EntityPlayer) entity, ConfigSettings.breastSize, ConfigSettings.bounceMultiplier);
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

            // Adjust the position and scaling of the breasts
            this.leftBreast.setRotationPoint(-1.5F - separation, 18.0F + height, -2.0F + depth); // Raised 18px
            this.rightBreast.setRotationPoint(1.5F + separation, 18.0F + height, -2.0F + depth); // Raised 18px

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftBreast.rotationPointX * scaleFactor, this.leftBreast.rotationPointY * scaleFactor, this.leftBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, breastSize);
            GlStateManager.translate(-this.leftBreast.rotationPointX * scaleFactor, -this.leftBreast.rotationPointY * scaleFactor, -this.leftBreast.rotationPointZ * scaleFactor);
            this.leftBreast.rotateAngleZ = rotation;
            this.leftBreast.rotateAngleX = -0.2F; // Tilt forward
            this.leftBreast.render(scaleFactor);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rightBreast.rotationPointX * scaleFactor, this.rightBreast.rotationPointY * scaleFactor, this.rightBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, breastSize);
            GlStateManager.translate(-this.rightBreast.rotationPointX * scaleFactor, -this.rightBreast.rotationPointY * scaleFactor, -this.rightBreast.rotationPointZ * scaleFactor);
            this.rightBreast.rotateAngleZ = -rotation;
            this.rightBreast.rotateAngleX = -0.2F; // Tilt forward
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

        this.leftBreast.rotateAngleY = bounceX;
        this.leftBreast.rotationPointY = 18.0F + bounceY;

        this.rightBreast.rotateAngleY = bounceX;
        this.rightBreast.rotationPointY = 18.0F + bounceY;
    }
}
