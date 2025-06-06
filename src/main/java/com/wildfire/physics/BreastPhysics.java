package com.wildfire.physics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import com.wildfire.main.config.GenderConfig;

public class BreastPhysics {
    private float positionX, positionY;
    private float velocityX, velocityY;
    private float bounceRotation;
    private float stiffness = 0.1f;
    private float damping = 0.85f;
    private float mass = 1.0f;
    private float bounceIntensity = 0.15f;
    private float lastMotionX, lastMotionZ;

    public void update(EntityLivingBase entity, float bustSize, float bounceMultiplier) {
        this.mass = 0.8f + (bustSize / 200.0f) * 0.5f;
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((net.minecraft.entity.player.EntityPlayer) entity);
        float adjustedStiffness = settings.stiffness * (1.0f - bustSize / 400.0f);
        float adjustedDamping = settings.damping * (1.0f - bustSize / 400.0f);

        float accelX = (float) (entity.motionX - lastMotionX);
        float accelZ = (float) (entity.motionZ - lastMotionZ);
        lastMotionX = (float) entity.motionX;
        lastMotionZ = (float) entity.motionZ;

        float springForceY = -adjustedStiffness * positionY;
        float dampingForceY = -adjustedDamping * velocityY;
        velocityY += (springForceY + dampingForceY) / mass;
        positionY += velocityY;

        float yawChange = Math.abs(entity.rotationYaw - entity.prevRotationYaw);
        float accel = (float) Math.sqrt(accelX * accelX + accelZ * accelZ);
        velocityX += (accel * bounceMultiplier + yawChange * 0.01f - positionX * 0.1f) / mass;
        positionX += velocityX;
        velocityX *= adjustedDamping;

        float movementFactor = entity.isSprinting() ? 1.5f : entity.isSneaking() ? 0.5f : 1.0f;
        bounceRotation = (MathHelper.cos(entity.limbSwing * 0.8f) * bounceIntensity + (float) Math.random() * 0.02f) * movementFactor * bounceMultiplier;

        if (entity.isRiding()) {
            positionX *= 0.5f;
            positionY *= 0.5f;
            bounceRotation *= 0.5f;
        }

        positionX = MathHelper.clamp_float(positionX, -0.5f, 0.5f);
        positionY = MathHelper.clamp_float(positionY, -0.3f, 0.3f);
    }

    public float getPositionX() { return positionX; }
    public float getPositionY() { return positionY; }
    public float getBounceRotation() { return bounceRotation; }

    public void resetPhysics() {
        positionX = positionY = velocityX = velocityY = bounceRotation = 0;
        lastMotionX = lastMotionZ = 0;
    }
}