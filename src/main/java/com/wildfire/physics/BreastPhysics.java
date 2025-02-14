package com.wildfire.physics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class BreastPhysics {
    // X-Axis
    private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
    // Y-Axis
    private float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;
    // Rotation
    private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, bounceRotation, preBounceRotation;

    private float breastSize = 0, preBreastSize = 0;

    private Vec3 prePos;
    private int randomB = 1;
    private double lastVerticalMoveVelocity;

    public void update(EntityLivingBase entity, float bustSize, float bounceMultiplier) {
        if (!entity.worldObj.isRemote || entity.isDead) {
            return;
        }

        this.prePositionY = this.positionY;
        this.prePositionX = this.positionX;
        this.preBounceRotation = this.bounceRotation;
        this.preBreastSize = this.breastSize;

        if (this.prePos == null) {
            this.prePos = entity.getPositionVector();
            return;
        }

        this.breastSize = bustSize;
        Vec3 motion = entity.getPositionVector().subtract(this.prePos);
        this.prePos = entity.getPositionVector();

        float bounceIntensity = (bustSize * 3f) * Math.round((bounceMultiplier * 3) * 100) / 100f;

        double vertVelocity = entity.motionY;
        if ((lastVerticalMoveVelocity <= 0 && vertVelocity > 0) || (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {
            randomB = entity.worldObj.rand.nextBoolean() ? -1 : 1;
        }
        lastVerticalMoveVelocity = vertVelocity;

        this.targetBounceY = (float) motion.yCoord * bounceIntensity;
        this.targetBounceY += bustSize;
        float horizVel = (float) Math.sqrt(motion.xCoord * motion.xCoord + motion.zCoord * motion.zCoord) * bounceIntensity;

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motion.yCoord * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float speed = (float) Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ) / 0.2F;
        speed = speed * speed * speed;
        if (speed < 1.0F) speed = 1.0F;
        this.targetBounceY += MathHelper.cos(entity.limbSwing * 0.6662F + (float) Math.PI) * 0.5F * entity.limbSwingAmount * 0.5F / speed;

        float percent = 0.35f;
        float bounceAmount = 0.45f * (1f - percent) + 0.15f;
        bounceAmount = MathHelper.clamp_float(bounceAmount, 0.15f, 0.6f);
        float delta = 1.75f - bounceAmount;

        float distanceFromMin = Math.abs(bounceVel + 1.5f) * 0.5f;
        float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;

        if (bounceVel < -0.5f) {
            targetBounceY += distanceFromMin;
        }
        if (bounceVel > 2.5f) {
            targetBounceY -= distanceFromMax;
        }

        targetBounceY = MathHelper.clamp_float(targetBounceY, -1.5f, 2.5f);
        targetRotVel = MathHelper.clamp_float(targetRotVel, -15f, 15f);

        this.velocity = MathHelper.clamp_float(this.velocity + (this.targetBounceY - this.bounceVel) * delta, -3f, 3f);
        this.bounceVel += this.velocity * percent * 1.1625f;

        this.velocityX = MathHelper.clamp_float(this.velocityX + (this.targetBounceX - this.bounceVelX) * delta, -3f, 3f);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity = MathHelper.clamp_float(this.rotVelocity + (this.targetRotVel - this.bounceRotVel) * delta, -3f, 3f);
        this.bounceRotVel += this.rotVelocity * percent;

        this.bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        if (this.positionY < -0.5f) this.positionY = -0.5f;
        if (this.positionY > 1.5f) {
            this.positionY = 1.5f;
            this.velocity = 0;
        }
    }

    private float calcRotation(EntityLivingBase entity, float bounceIntensity) {
        return -((entity.rotationYawHead - entity.prevRotationYawHead) / 20f) * bounceIntensity;
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

    public float getBreastSize(float partialTicks) {
        return this.preBreastSize + (this.breastSize - this.preBreastSize) * partialTicks;
    }
}
