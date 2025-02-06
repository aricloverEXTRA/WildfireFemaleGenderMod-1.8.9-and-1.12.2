package com.wildfire.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.wildfire.config.ConfigSettings;

public class BreastsModel extends ModelBase {
    private final ModelRenderer leftBreast;
    private final ModelRenderer rightBreast;
    private final BreastPhysics leftBreastPhysics;
    private final BreastPhysics rightBreastPhysics;

    public BreastsModel() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        // Left Breast
        this.leftBreast = new ModelRenderer(this, 0, 0);
        this.leftBreast.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4);
        this.leftBreast.setRotationPoint(-2.5F, 12.0F, -2.0F);

        // Right Breast
        this.rightBreast = new ModelRenderer(this, 0, 0);
        this.rightBreast.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4);
        this.rightBreast.setRotationPoint(2.5F, 12.0F, -2.0F);

        // Initialize BreastPhysics
        this.leftBreastPhysics = new BreastPhysics();
        this.rightBreastPhysics = new BreastPhysics();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.leftBreast.render(scale);
        this.rightBreast.render(scale);
    }

    @Override
    public void setRotationAngles(
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scaleFactor,
        Entity entityIn
    ) {
        // Apply ConfigSettings adjustments
        float sizeMultiplier = (float) (ConfigSettings.breastSize / 50.0); // Normalize to a scale factor

        // Update BreastPhysics
        this.leftBreastPhysics.applyForce(-limbSwingAmount * 0.1f); // Apply force based on limb swing
        this.rightBreastPhysics.applyForce(-limbSwingAmount * 0.1f);
        this.leftBreastPhysics.update();
        this.rightBreastPhysics.update();

        // Left Breast Adjustments
        this.leftBreast.rotateAngleX = headPitch * 0.017453292F + (float) ConfigSettings.height * 0.017453292F;
        this.leftBreast.rotateAngleY = netHeadYaw * 0.017453292F + (float) ConfigSettings.rotation * 0.017453292F;
        this.leftBreast.rotationPointX = -2.5F - (float) ConfigSettings.separation;
        this.leftBreast.rotationPointY = 12.0F - (float) ConfigSettings.height + this.leftBreastPhysics.getPosition();
        this.leftBreast.rotationPointZ = -2.0F + (float) ConfigSettings.depth;

        // Right Breast Adjustments
        this.rightBreast.rotateAngleX = headPitch * 0.017453292F + (float) ConfigSettings.height * 0.017453292F;
        this.rightBreast.rotateAngleY = netHeadYaw * 0.017453292F - (float) ConfigSettings.rotation * 0.017453292F;
        this.rightBreast.rotationPointX = 2.5F + (float) ConfigSettings.separation;
        this.rightBreast.rotationPointY = 12.0F - (float) ConfigSettings.height + this.rightBreastPhysics.getPosition();
        this.rightBreast.rotationPointZ = -2.0F + (float) ConfigSettings.depth;
    }

    public void setLeftBreastVelocity(float velocity) {
        this.leftBreastPhysics.applyForce(velocity);
    }

    public void setRightBreastVelocity(float velocity) {
        this.rightBreastPhysics.applyForce(velocity);
    }
}
