package com.wildfire.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import com.wildfire.config.ConfigSettings;
import com.wildfire.api.IGenderArmor;

public class BreastsModel extends ModelBase {

    private final ModelRenderer leftBreast;
    private final ModelRenderer rightBreast;
    private final BreastPhysics breastPhysics;

    public BreastsModel() {
        textureWidth = 64;
        textureHeight = 64;

        leftBreast = new ModelRenderer(this, 32, 32);
        leftBreast.addBox(-1.5F, -1.0F, -1.5F, 3, 4, 3);
        leftBreast.setRotationPoint(-2.0F, 18.0F, -2.0F);

        rightBreast = new ModelRenderer(this, 32, 32);
        rightBreast.addBox(-1.5F, -1.0F, -1.5F, 3, 4, 3);
        rightBreast.setRotationPoint(2.0F, 18.0F, -2.0F);

        breastPhysics = new BreastPhysics(); // No need for GenderPlayer
    }

    private void applyPlayerTorsoTexture(EntityPlayer player) {
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
        ResourceLocation skin = clientPlayer.getLocationSkin();
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

        int torsoTextureX = 0;
        int torsoTextureY = 16;

        this.leftBreast.setTextureOffset(torsoTextureX, torsoTextureY);
        this.rightBreast.setTextureOffset(torsoTextureX, torsoTextureY);
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (shouldRenderBreasts(entity)) {
            EntityPlayer player = (EntityPlayer) entity;
            applyPlayerTorsoTexture(player);

            IGenderArmor armor = null;  // Adjust as necessary
            breastPhysics.update(player, armor);

            float breastSize = 1.0F + ConfigSettings.breastSize / 200.0F;
            float separation = ConfigSettings.separation / 20.0F;
            float depth = ConfigSettings.depth / 20.0F;
            float height = ConfigSettings.height / 40.0F;
            float rotation = ConfigSettings.rotation * (float) Math.PI / 180.0F;

            setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

            if (separation < 1.0F) separation = 1.0F;

            float posX = breastPhysics.getPositionX();
            float posY = breastPhysics.getPositionY();
            float bounceRotation = breastPhysics.getBounceRotation();

            this.leftBreast.setRotationPoint(-separation - 1.0F + posX, 18.0F + height + posY, -2.0F + depth);
            this.rightBreast.setRotationPoint(separation + 1.0F - posX, 18.0F + height + posY, -2.0F + depth);

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftBreast.rotationPointX * scaleFactor, this.leftBreast.rotationPointY * scaleFactor, this.leftBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, 1.0F);
            GlStateManager.translate(-this.leftBreast.rotationPointX * scaleFactor, -this.leftBreast.rotationPointY * scaleFactor, -this.leftBreast.rotationPointZ * scaleFactor);
            this.leftBreast.rotateAngleZ = bounceRotation + rotation;
            this.leftBreast.rotateAngleX = -0.2F;
            this.leftBreast.render(scaleFactor);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rightBreast.rotationPointX * scaleFactor, this.rightBreast.rotationPointY * scaleFactor, this.rightBreast.rotationPointZ * scaleFactor);
            GlStateManager.scale(breastSize, breastSize, 1.0F);
            GlStateManager.translate(-this.rightBreast.rotationPointX * scaleFactor, -this.rightBreast.rotationPointY * scaleFactor, -this.rightBreast.rotationPointZ * scaleFactor);
            this.rightBreast.rotateAngleZ = -bounceRotation - rotation;
            this.rightBreast.rotateAngleX = -0.2F;
            this.rightBreast.render(scaleFactor);
            GlStateManager.popMatrix();
        }
    }

    private boolean shouldRenderBreasts(Entity entity) {
        return ConfigSettings.breastsEnabled && entity instanceof EntityPlayer;
    }
}
