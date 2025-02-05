package com.wildfire.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class BreastsModel extends ModelBase {
    private final ModelRenderer leftBreast;
    private final ModelRenderer rightBreast;
    private float leftBreastVelocity;
    private float rightBreastVelocity;

    public BreastsModel() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.leftBreast = new ModelRenderer(this, 0, 0);
        this.leftBreast.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
        this.leftBreast.setRotationPoint(2.0F, 1.0F, -2.0F);

        this.rightBreast = new ModelRenderer(this, 0, 0);
        this.rightBreast.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2);
        this.rightBreast.setRotationPoint(-2.0F, 1.0F, -2.0F);

        this.leftBreastVelocity = 0.0F;
        this.rightBreastVelocity = 0.0F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.leftBreastVelocity += 0.1F;
        this.rightBreastVelocity += 0.1F;

        this.leftBreast.rotateAngleX += this.leftBreastVelocity;
        this.rightBreast.rotateAngleX += this.rightBreastVelocity;

        this.leftBreast.render(scale);
        this.rightBreast.render(scale);
    }
}
