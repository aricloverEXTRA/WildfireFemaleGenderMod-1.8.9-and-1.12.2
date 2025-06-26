package com.wildfire.physics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import com.wildfire.main.config.GenderConfig;

public class BreastPhysics {
    private float leftPositionX, leftPositionY, rightPositionX, rightPositionY;
    private float leftVelocityX, leftVelocityY, rightVelocityX, rightVelocityY;
    private float bounceRotation;
    private float lastMotionX, lastMotionZ, lastRidingMotionX, lastRidingMotionZ;
    private boolean isAttackTriggered;

    public void update(EntityLivingBase entity, float bustSize, float intensity, float momentum) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((net.minecraft.entity.player.EntityPlayer) entity);
        if (!settings.physicsEnabled) {
            resetPhysics();
            return;
        }

        float mass = 0.8f + (bustSize / 200.0f) * 0.5f;
        float adjustedIntensity = intensity / 100.0f;
        float adjustedMomentum = momentum / 100.0f;
        float adjustedStiffness = settings.stiffness * (1.0f - bustSize / 400.0f);
        float adjustedDamping = settings.damping * (1.0f - bustSize / 400.0f);

        float accelX = (float) (entity.motionX - lastMotionX);
        float accelZ = (float) (entity.motionZ - lastMotionZ);
        lastMotionX = (float) entity.motionX;
        lastMotionZ = (float) entity.motionZ;

        float ridingAccelX = 0.0f;
        float ridingAccelZ = 0.0f;
        if (entity.isRiding()) {
            EntityLivingBase riding = (EntityLivingBase) entity.ridingEntity;
            ridingAccelX = (float) (riding.motionX - lastRidingMotionX);
            ridingAccelZ = (float) (riding.motionZ - lastRidingMotionZ);
            lastRidingMotionX = (float) riding.motionX;
            lastRidingMotionZ = (float) riding.motionZ;
        }

        float totalAccelX = accelX + (entity.isRiding() ? ridingAccelX : 0.0f);
        float totalAccelZ = accelZ + (entity.isRiding() ? ridingAccelZ : 0.0f);
        float springForceLeftY = -adjustedStiffness * leftPositionY;
        float dampingForceLeftY = -adjustedDamping * leftVelocityY;
        leftVelocityY += (springForceLeftY + dampingForceLeftY) / mass + totalAccelZ * adjustedIntensity * adjustedMomentum;
        leftPositionY += leftVelocityY;
        leftVelocityX += (totalAccelX * adjustedIntensity * adjustedMomentum - leftPositionX * 0.1f) / mass;
        leftPositionX += leftVelocityX;
        leftVelocityX *= adjustedDamping;

        float springForceRightY = -adjustedStiffness * rightPositionY;
        float dampingForceRightY = -adjustedDamping * rightVelocityY;
        rightVelocityY += (springForceRightY + dampingForceRightY) / mass + totalAccelZ * adjustedIntensity * adjustedMomentum;
        rightPositionY += rightVelocityY;
        rightVelocityX += (totalAccelX * adjustedIntensity * adjustedMomentum - rightPositionX * 0.1f) / mass;
        rightPositionX += rightVelocityX;
        rightVelocityX *= adjustedDamping;

        if (isAttackTriggered) {
            leftVelocityX += 0.2f * adjustedIntensity * adjustedMomentum;
            rightVelocityX -= 0.2f * adjustedIntensity * adjustedMomentum;
            isAttackTriggered = false;
        }

        float yawChange = Math.abs(entity.rotationYaw - entity.prevRotationYaw);
        float accel = (float) Math.sqrt(totalAccelX * totalAccelX + totalAccelZ * totalAccelZ);
        bounceRotation = (MathHelper.cos(entity.limbSwing * 0.8f) * 0.15f + (float) Math.random() * 0.02f) * adjustedIntensity;

        float movementFactor = entity.isSprinting() ? 1.5f : entity.isSneaking() ? 0.5f : 1.0f;
        if (entity.isRiding()) {
            leftPositionX *= 0.7f;
            leftPositionY *= 0.7f;
            rightPositionX *= 0.7f;
            rightPositionY *= 0.7f;
            bounceRotation *= 0.7f;
        }

        leftPositionX = MathHelper.clamp_float(leftPositionX, -0.5f, 0.5f);
        leftPositionY = MathHelper.clamp_float(leftPositionY, -0.3f, 0.3f);
        rightPositionX = MathHelper.clamp_float(rightPositionX, -0.5f, 0.5f);
        rightPositionY = MathHelper.clamp_float(rightPositionY, -0.3f, 0.3f);
    }

    public void applyAttackImpulse(EntityLivingBase entity, float bustSize, float intensity, float momentum) {
        isAttackTriggered = true;
    }

    public float getLeftPositionX() { return leftPositionX; }
    public float getLeftPositionY() { return leftPositionY; }
    public float getRightPositionX() { return rightPositionX; }
    public float getRightPositionY() { return rightPositionY; }
    public float getBounceRotation() { return bounceRotation; }

    public void resetPhysics() {
        leftPositionX = leftPositionY = rightPositionX = rightPositionY = 0.0f;
        leftVelocityX = leftVelocityY = rightVelocityX = rightVelocityY = 0.0f;
        bounceRotation = 0.0f;
        lastMotionX = lastMotionZ = lastRidingMotionX = lastRidingMotionZ = 0.0f;
        isAttackTriggered = false;
    }
}