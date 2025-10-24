package com.wildfire.physics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import com.wildfire.main.config.GenderConfig;

/**
 * BreastPhysics (visual curve variant for 1.8.9)
 *
 * - Preserves original API: update(...), applyAttackImpulse(...), getters, resetPhysics()
 * - Internally uses smoothed offsets (visual-only), not true springs.
 * - Maps to the existing GenderLayer usage: getLeft/RightPositionY() and getBounceRotation().
 *
 * Note: This class is intended to drive small rotations/translations applied in GenderLayer.
 */
public class BreastPhysics {

    private float leftOffsetX, leftOffsetY;
    private float rightOffsetX, rightOffsetY;

    private float smoothedAccelX, smoothedAccelZ;
    private float phase;
    private float attackT;
    private float attackDir;
    private float lastMotionX, lastMotionZ;
    private float lastRidingMotionX, lastRidingMotionZ;

    private float bounceRotation;

    public void update(EntityLivingBase entity, float bustSize, float intensity, float momentum) {
        GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings((EntityPlayer) entity);
        if (settings == null || !settings.physicsEnabled) {
            resetPhysics();
            return;
        }

        float sizeN = clamp01(bustSize / 200.0f);
        float intensityN = clamp01(intensity / 100.0f);
        float momentumN = clamp01(momentum / 100.0f);

        float softness = lerp(0.7f, 1.0f, sizeN);
        float response = lerp(0.35f, 0.15f, sizeN);

        float limbSwing = entity.limbSwing;
        float limbSwingAmt = entity.limbSwingAmount;
        float speedFactor = clamp01(limbSwingAmt * (entity.isSprinting() ? 1.6f : entity.isSneaking() ? 0.6f : 1.0f));

        float accelX = (float) (entity.motionX - lastMotionX);
        float accelZ = (float) (entity.motionZ - lastMotionZ);
        lastMotionX = (float) entity.motionX;
        lastMotionZ = (float) entity.motionZ;

        if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase riding = (EntityLivingBase) entity.ridingEntity;
            accelX += (float) (riding.motionX - lastRidingMotionX);
            accelZ += (float) (riding.motionZ - lastRidingMotionZ);
            lastRidingMotionX = (float) riding.motionX;
            lastRidingMotionZ = (float) riding.motionZ;
        }

        float filter = 0.85f;
        smoothedAccelX = smoothedAccelX * filter + accelX * (1.0f - filter);
        smoothedAccelZ = smoothedAccelZ * filter + accelZ * (1.0f - filter);

        phase += 0.06f + speedFactor * 0.02f;

        float walkX = MathHelper.sin(limbSwing * 0.6f) * 0.06f * intensityN;
        float walkY = MathHelper.cos(limbSwing * 0.6f) * 0.05f * intensityN;

        float ambientX = MathHelper.sin(phase * 0.9f) * 0.03f * softness * intensityN;
        float ambientY = MathHelper.cos(phase * 1.1f) * 0.025f * softness * intensityN;

        float inertiaX = smoothedAccelX * 0.8f * momentumN;
        float inertiaY = smoothedAccelZ * 0.7f * momentumN;

        float rideMul = entity.isRiding() ? 0.6f : 1.0f;

        float attackNX = 0.0f;
        if (attackT > 0.0f) {
            float ease = cubicOut(attackT);
            attackNX = 0.18f * ease * intensityN * momentumN * attackDir;
            attackT = Math.max(0.0f, attackT - 0.10f);
        }

        float targetX = (walkX + ambientX + inertiaX + attackNX) * rideMul;
        float targetY = (walkY + ambientY + inertiaY) * rideMul;

        float desync = MathHelper.sin(phase * 0.35f) * 0.012f;

        float leftTargetX  = targetX + desync;
        float rightTargetX = -targetX + desync * 0.7f;
        float leftTargetY  = targetY + desync * 0.5f;
        float rightTargetY = targetY - desync * 0.5f;

        leftOffsetX  = smoothApproach(leftOffsetX,  leftTargetX,  response);
        rightOffsetX = smoothApproach(rightOffsetX, rightTargetX, response);
        leftOffsetY  = smoothApproach(leftOffsetY,  leftTargetY,  response);
        rightOffsetY = smoothApproach(rightOffsetY, rightTargetY, response);

        // NEW: spring-back to neutral when idle
        leftOffsetX  = smoothApproach(leftOffsetX,  0f, 0.05f);
        rightOffsetX = smoothApproach(rightOffsetX, 0f, 0.05f);
        leftOffsetY  = smoothApproach(leftOffsetY,  0f, 0.05f);
        rightOffsetY = smoothApproach(rightOffsetY, 0f, 0.05f);

        bounceRotation = (MathHelper.cos(limbSwing * 0.8f) * 0.15f
                        + MathHelper.sin(phase * 0.5f) * 0.03f) * intensityN;
    }

    public void applyAttackImpulse(EntityLivingBase entity, float bustSize, float intensity, float momentum) {
        attackT = 1.0f;
        attackDir = 1.0f;
    }

    // Getters mapped to visual offsets
    public float getLeftPositionX() { return leftOffsetX; }
    public float getLeftPositionY() { return leftOffsetY; }
    public float getRightPositionX() { return rightOffsetX; }
    public float getRightPositionY() { return rightOffsetY; }
    public float getBounceRotation() { return bounceRotation; }

    public void resetPhysics() {
        leftOffsetX = leftOffsetY = rightOffsetX = rightOffsetY = 0.0f;
        smoothedAccelX = smoothedAccelZ = 0.0f;
        phase = 0.0f;
        attackT = 0.0f;
        attackDir = 0.0f;
        lastMotionX = lastMotionZ = lastRidingMotionX = lastRidingMotionZ = 0.0f;
        bounceRotation = 0.0f;
    }

    // Helpers
    private static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }
    private static float clamp01(float v) { return clamp(v, 0f, 1f); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static float smoothApproach(float current, float target, float rate) {
        return current + (target - current) * clamp(rate, 0.05f, 0.35f);
    }
    private static float cubicOut(float t) {
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv;
    }
}