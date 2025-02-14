package com.wildfire.client.model;

import com.wildfire.config.ConfigSettings;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;

public class BreastsModel extends ModelBase {
    private final ModelRenderer leftBreast;
    private final ModelRenderer rightBreast;
    private final BreastPhysics breastPhysics;

    public BreastsModel() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        // Left Breast
        this.leftBreast = new ModelRenderer(this, 0, 16);
        this.leftBreast.addBox(-1.5F, 0.0F, -2.0F, 3, 3, 2);
        this.leftBreast.setRotationPoint(-1.5F, 11.0F, -2.0F);

        // Right Breast
        this.rightBreast = new ModelRenderer(this, 0, 16);
        this.rightBreast.addBox(-1.5F, 0.0F, -2.0F, 3, 3, 2);
        this.rightBreast.setRotationPoint(1.5F, 11.0F, -2.0F);

        this.breastPhysics = new BreastPhysics();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (shouldRenderBreasts(entity)) {
            // Bind the player's skin texture
            bindPlayerSkinTexture((EntityPlayer) entity);

            // Update breast physics
            breastPhysics.update((EntityPlayer) entity, ConfigSettings.breastSize, ConfigSettings.bounceMultiplier);

            // Apply transformations
            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);

            // Render breasts
            this.leftBreast.render(scale);
            this.rightBreast.render(scale);
        }
    }

    private void bindPlayerSkinTexture(EntityPlayer player) {
        if (player instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractPlayer = (AbstractClientPlayer) player;
            ResourceLocation skin = abstractPlayer.getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // Adjust the rotation and position of the breasts based on physics
        this.leftBreast.rotateAngleX = breastPhysics.getPositionY();
        this.leftBreast.rotateAngleY = breastPhysics.getPositionX();
        this.leftBreast.rotateAngleZ = breastPhysics.getBounceRotation();

        this.rightBreast.rotateAngleX = breastPhysics.getPositionY();
        this.rightBreast.rotateAngleY = breastPhysics.getPositionX();
        this.rightBreast.rotateAngleZ = breastPhysics.getBounceRotation();

        this.leftBreast.setRotationPoint(-1.5F, 12.0F + breastPhysics.getPositionY(), -2.0F);
        this.rightBreast.setRotationPoint(1.5F, 12.0F + breastPhysics.getPositionY(), -2.0F);
    }

    public boolean shouldRenderBreasts(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            String gender = ConfigSettings.gender;
            return "Female".equals(gender) || "Other".equals(gender);
        }
        return false;
    }
}
