package com.wildfire.physics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class BreastPhysics {
    private float positionY;
    private float positionX;
    private float velocityY;
    private float velocityX;
    private float bounceRotation;
    private float dampingFactor = 0.9f;
    private float gravity = 0.08f;
    private float bounceIntensity = 0.1f;

    public void update(EntityLivingBase entity, float bustSize, float bounceMultiplier) {
        float playerVelocityX = (float) entity.motionX;
        float playerVelocityY = (float) entity.motionY;

        boolean isRiding = entity.isRiding();

        if (isRiding) {
            playerVelocityX = 0;
            playerVelocityY = 0;
        }

        this.velocityX += playerVelocityX * bounceMultiplier - (this.positionX * 0.1f);
        this.positionX += this.velocityX;
        this.velocityX *= dampingFactor;

        if (!entity.onGround) {
            this.velocityY -= gravity;
        } else {
            if (this.velocityY < 0) {
                this.velocityY = 0;
            }
            this.velocityY += gravity;
        }

        this.positionY += this.velocityY;
        this.velocityY *= dampingFactor;

        this.bounceRotation = (MathHelper.cos(entity.limbSwing * 0.6662F) * bounceIntensity) * bounceMultiplier;
    }

    public float getPositionY() {
        return positionY;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getBounceRotation() {
        return bounceRotation;
    }

    public void resetPhysics() {
        this.positionY = 0;
        this.positionX = 0;
        this.bounceRotation = 0;
        this.velocityY = 0;
        this.velocityX = 0;
    }
}
