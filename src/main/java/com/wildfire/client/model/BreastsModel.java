package com.wildfire.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import com.wildfire.config.ConfigSettings;

public class BreastsModel extends ModelBase {
    private ModelRenderer leftBreast;
    private ModelRenderer rightBreast;
    private BreastPhysics breastPhysics;
    private Breasts breasts;

    public BreastsModel() {
        this.breasts = new Breasts();
        this.breastPhysics = new BreastPhysics();

        // Initialize with the correct texture offsets for the main layer
        this.leftBreast = new ModelRenderer(this, 34, 16);
        this.leftBreast.addBox(-2.0F, -2.0F, -1.0F, 4, 4, 2);

        this.rightBreast = new ModelRenderer(this, 34, 16);
        this.rightBreast.addBox(-2.0F, -2.0F, -1.0F, 4, 4, 2);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (shouldRenderBreasts(entity)) {
            ResourceLocation skin = ((AbstractClientPlayer) entity).getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

            float breastSize = ConfigSettings.breastSize / 50.0F; // Scale to 0.0 to 2.0 range
            float separation = ConfigSettings.separation;
            float depth = ConfigSettings.depth;
            float height = ConfigSettings.height;
            float rotation = ConfigSettings.rotation;

            this.breastPhysics.update((EntityPlayer) entity, ConfigSettings.breastSize, ConfigSettings.bounceMultiplier);
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

            // Create new ModelRenderers with adjusted sizes and correct texture offsets
            this.leftBreast = new ModelRenderer(this, 16, 16);
            this.leftBreast.addBox(-2.0F * breastSize, -2.0F * breastSize, -1.0F * breastSize, (int) (4 * breastSize), (int) (4 * breastSize), (int) (2 * breastSize));
            this.leftBreast.setRotationPoint(breasts.getXOffset() - 2.5F - separation, breasts.getYOffset() + 8.0F + height, breasts.getZOffset() - 2.0F + depth);
            this.leftBreast.rotateAngleZ = rotation;

            this.rightBreast = new ModelRenderer(this, 16, 16);
            this.rightBreast.addBox(-2.0F * breastSize, -2.0F * breastSize, -1.0F * breastSize, (int) (4 * breastSize), (int) (4 * breastSize), (int) (2 * breastSize));
            this.rightBreast.setRotationPoint(breasts.getXOffset() + 2.5F + separation, breasts.getYOffset() + 8.0F + height, breasts.getZOffset() - 2.0F + depth);
            this.rightBreast.rotateAngleZ = -rotation;

            this.leftBreast.render(scaleFactor);
            this.rightBreast.render(scaleFactor);
        }
    }

    private boolean shouldRenderBreasts(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float bounceX = breastPhysics.getPositionX();
        float bounceY = breastPhysics.getPositionY();

        this.leftBreast.rotateAngleY = bounceX;
        this.leftBreast.rotationPointY = breasts.getYOffset() + 8.0F + bounceY;

        this.rightBreast.rotateAngleY = bounceX;
        this.rightBreast.rotationPointY = breasts.getYOffset() + 8.0F + bounceY;
    }
}
