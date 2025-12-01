package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.main.WildfireHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BreastPhysics {

    // X axis
    private float bounceVelX = 0f, targetBounceX = 0f, velocityX = 0f, positionX = 0f, prePositionX = 0f;
    // Y axis
    private float bounceVel = 0f, targetBounceY = 0f, velocity = 0f, positionY = 0f, prePositionY = 0f;
    // Rotation
    private float bounceRotVel = 0f, targetRotVel = 0f, rotVelocity = 0f,
                  wfg_bounceRotation = 0f, wfg_preBounceRotation = 0f;
    // Visual size (cosmetic)
    private float breastSize = 0f, preBreastSize = 0f;

    private Vec3d prePos = null;
    private int lastSwingDuration = 6, lastSwingTick = 0;
    private int randomB = 1;
    private double lastVerticalMoveVelocity = 0.0;

    private static final float VISUAL_BREAST_WEIGHT = 0.1f;

    // Rotation-flop tuning
    private static final float ROTATION_FLOP_STRENGTH = 0.035f;
    private static final float ROTATION_FLOP_MAX = 0.65f;

    // Momentum tuning multipliers
    private static final float MOMENTUM_BASE = 0.25f;
    private static final float MOMENTUM_SCALE = 2.75f;

    public BreastPhysics() {
        resetPhysics();
    }

    public void resetPhysics() {
        prePositionX = positionX = 0.0f;
        prePositionY = positionY = 0.0f;
        velocityX = velocity = 0.0f;
        bounceVel = bounceVelX = bounceRotVel = targetBounceX = targetBounceY = targetRotVel = 0.0f;
        rotVelocity = 0.0f;
        wfg_preBounceRotation = wfg_bounceRotation = 0.0f;
        preBreastSize = breastSize = 0.0f;
        prePos = null;
        lastSwingDuration = 6;
        lastSwingTick = 0;
        randomB = 1;
        lastVerticalMoveVelocity = 0.0;
    }

    /**
     * Update physics for entity.
     */
    public void update(EntityLivingBase entity, IGenderArmor armor) {
        if (entity == null) return;

        this.prePositionX = this.positionX;
        this.prePositionY = this.positionY;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preBreastSize = this.breastSize;

        if (this.prePos == null) {
            this.prePos = new Vec3d(entity.posX, entity.posY, entity.posZ);
            if (entity instanceof EntityPlayer) {
                GenderConfig.PlayerGenderSettings s = GenderConfig.getPlayerSettings((EntityPlayer) entity);
                if (s != null) {
                    this.breastSize = this.preBreastSize = s.breastSize;
                }
            }
            return;
        }

        float intensity = 1.0f;
        float momentumNormalized = 1.0f; // 0..1
        GenderConfig.PlayerGenderSettings settings = null;
        boolean uniboob = false;
        if (entity instanceof EntityPlayer) {
            settings = GenderConfig.getPlayerSettings((EntityPlayer) entity);
            if (settings != null) {
                intensity = clamp(settings.intensity / 100.0f, 0f, 1.5f);
                momentumNormalized = clamp(settings.momentum / 100.0f, 0f, 1.0f);
                uniboob   = settings.breastsUniboob;
            }
        }

        if (settings != null) {
            float target = settings.breastSize;
            if (this.breastSize < target) this.breastSize += Math.abs(this.breastSize - target) / 2f;
            else                         this.breastSize -= Math.abs(this.breastSize - target) / 2f;
        }

        // Suppress physics under armor that covers breasts unless overridden
        try {
            if (armor != null && armor.coversBreasts() && settings != null && !settings.overrideArmorPhysics) {
                this.targetBounceX = 0f;
                this.targetBounceY = 0f;
                this.targetRotVel  = 0f;
                this.velocity = 0f;
                this.velocityX = 0f;
                this.rotVelocity = 0f;
                this.bounceVel = 0f;
                this.bounceVelX = 0f;
                this.bounceRotVel = 0f;
                finishTick();
                return;
            }
        } catch (Throwable ignored) {
            // continue with normal physics if armor impl misbehaves
        }

        Vec3d curPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
        Vec3d motion = curPos.subtract(this.prePos);
        this.prePos = curPos;

        float bounceIntensity = intensity * 0.9f;
        float resistance = MathHelper.clamp(armor != null ? armor.physicsResistance() : 0f, 0f, 1f);
        bounceIntensity *= (1f - resistance);

        if (!uniboob) {
            bounceIntensity *= WildfireHelper.randFloat(0.95f, 1.05f);
        }

        tickMovement(entity, motion, bounceIntensity, VISUAL_BREAST_WEIGHT, momentumNormalized, uniboob);
        tickPose(entity, bounceIntensity);
        tickRide(entity, bounceIntensity, VISUAL_BREAST_WEIGHT, uniboob);
        tickArmSwing(entity, bounceIntensity, uniboob);
        finishTick();

        this.positionX = clamp(this.positionX, -0.8f, 0.8f);
        this.positionY = clamp(this.positionY, -0.5f, 1.0f);
    }

    private void tickMovement(EntityLivingBase entity, Vec3d motion,
                              float bounceIntensity, float breastWeight,
                              float momentumNorm, boolean uniboob) {
        double vert = entity.motionY;
        if ((lastVerticalMoveVelocity <= 0 && vert > 0) ||
            (lastVerticalMoveVelocity < 0 && vert == 0)) {
            randomB = uniboob ? 1 : (entity.getEntityWorld().rand.nextBoolean() ? -1 : 1);
        }
        lastVerticalMoveVelocity = vert;

        // vertical target from motion
        this.targetBounceY = (float) motion.y * bounceIntensity;
        this.targetBounceY += breastWeight;

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motion.y * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float momentumMultiplier = MOMENTUM_BASE + MOMENTUM_SCALE * momentumNorm;
        float inertiaX = (float) motion.x * momentumMultiplier;
        float inertiaZ = (float) motion.z * momentumMultiplier;
        this.targetBounceX += inertiaX;
        this.targetBounceY += inertiaZ * 0.35f;

        float yawDelta = MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw);
        float rotationFlop = yawDelta * ROTATION_FLOP_STRENGTH * bounceIntensity;
        rotationFlop = clamp(rotationFlop, -ROTATION_FLOP_MAX, ROTATION_FLOP_MAX);
        this.targetBounceX += rotationFlop;

        float f2 = (float) (entity.motionX * entity.motionX +
                            entity.motionY * entity.motionY +
                            entity.motionZ * entity.motionZ) / 0.2F;
        f2 = f2 * f2 * f2;
        if (f2 < 1.0F) f2 = 1.0F;

        this.targetBounceY += MathHelper.cos(entity.limbSwing * 0.6662F + (float) Math.PI) *
                              0.5F * entity.limbSwingAmount * 0.5F / f2 * momentumNorm;
    }

    private void tickPose(EntityLivingBase entity, float bounceIntensity) {
        if (entity.isSneaking()) {
            this.targetBounceY += bounceIntensity * 0.6f;
        }
    }

    private void tickRide(EntityLivingBase entity, float bounceIntensity,
                          float breastWeight, boolean uniboob) {
        Entity vehicle = entity.getRidingEntity();
        if (vehicle == null) return;

        double vx = vehicle.motionX, vz = vehicle.motionZ;
        double speed = vx * vx + vehicle.motionY * vehicle.motionY + vz * vz;

        if (!uniboob && Math.random() * speed < 0.5 && speed > 0.2) {
            this.targetBounceY = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
            this.targetBounceY += breastWeight;
        } else {
            this.targetBounceY += bounceIntensity / 4.5f;
        }
    }

    private void tickArmSwing(EntityLivingBase entity, float bounceIntensity, boolean uniboob) {
        int swingTicks = entity.swingProgressInt;
        if ((swingTicks > 1 || lastSwingDuration > 1) && !entity.isPlayerSleeping()) {
            float rawAmp = 0f;
            if (swingTicks < 6) rawAmp = 0.15f * (6 - swingTicks);
            else if (swingTicks > 6) rawAmp = -0.055f * (swingTicks - 6);
            float amp = MathHelper.clamp(1f + rawAmp, 0.6f, 1.3f);
            int everyN = MathHelper.clamp(swingTicks - 1, 1, 5);

            if (entity.isSwingInProgress && entity.ticksExisted % everyN == 0) {
                float sign = uniboob ? 1f : (Math.random() > 0.5 ? -0.25f : 0.25f);
                this.targetBounceY += sign * amp * bounceIntensity * 0.8f;

                float xAmp = MathHelper.clamp(1f + (rawAmp * (rawAmp < 0 ? 1.625f : 0.8f)),
                                              0.25f, 1.225f);
                int handSign = (entity.getEntityId() & 1) == 0 ? -1 : 1;
                this.targetBounceX = (0.325f * xAmp * bounceIntensity * 0.45f) * handSign;
            }

            int delta = entity.swingProgressInt - lastSwingTick;
            float prog = distanceFromMedian(0, lastSwingDuration,
                                            MathHelper.clamp(lastSwingTick, 0, lastSwingDuration));
            boolean towardRight = prog > -0.2f;

            if (delta < 0 && lastSwingTick != lastSwingDuration - 1) {
                this.targetRotVel += (towardRight ? -4f : 4f) *
                                     Math.abs(prog) * bounceIntensity * 0.45f;
            } else if (entity.isSwingInProgress && swingTicks > 1) {
                this.targetRotVel += (towardRight ? -0.2f : 0.2f) *
                                     amp * bounceIntensity * 0.45f;
            }

            lastSwingTick = entity.swingProgressInt;
        }
        if (!entity.isSwingInProgress) lastSwingTick = 0;
        lastSwingDuration = Math.max(swingTicks, 1);
    }

    private void finishTick() {
        float percent = 0.45f;
        float bounceAmt = MathHelper.clamp(0.45f * (1f - percent) + 0.15f, 0.15f, 0.6f);
        float delta = 2.25f - bounceAmt;

        float distMin = Math.abs(bounceVel + 1.5f) * 0.5f;
        float distMax = Math.abs(bounceVel - 2.65f) * 0.5f;
        if (bounceVel < -0.5f) targetBounceY += distMin;
        if (bounceVel > 2.5f)  targetBounceY -= distMax;

        targetBounceY = MathHelper.clamp(targetBounceY, -1.5f, 2.5f);
        targetRotVel  = MathHelper.clamp(targetRotVel,  -25f,  25f);

        this.velocity   = lerp(bounceAmt, this.velocity,   (targetBounceY - bounceVel)   * delta);
        this.bounceVel += this.velocity * percent * 1.1625f;

        this.velocityX   = lerp(bounceAmt, this.velocityX, (targetBounceX - bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity   = lerp(bounceAmt, this.rotVelocity, (targetRotVel - bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        this.wfg_bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        if (this.positionY < -0.5f) this.positionY = -0.5f;
        if (this.positionY > 1.0f) {
            this.positionY = 1.0f;
            this.velocity = 0f;
        }
    }

    private float calcRotation(EntityLivingBase entity, float bounceIntensity) {
        Entity vehicle = entity.getRidingEntity();
        float prev = vehicle != null ? vehicle.prevRotationYaw : entity.prevRotationYaw;
        float cur  = vehicle != null ? vehicle.rotationYaw    : entity.rotationYaw;
        return -((cur - prev) / 15f) * bounceIntensity;
    }

    public void syncFrom(BreastPhysics source) {
        this.positionX = -source.positionX;
        this.prePositionX = -source.prePositionX;
        this.positionY = source.positionY;
        this.prePositionY = source.prePositionY;

        this.wfg_bounceRotation = -source.wfg_bounceRotation;
        this.wfg_preBounceRotation = -source.wfg_preBounceRotation;

        this.breastSize = source.breastSize;
        this.preBreastSize = source.preBreastSize;

        this.velocityX = -source.velocityX;
        this.velocity = source.velocity;
        this.bounceVelX = -source.bounceVelX;
        this.bounceVel = -source.bounceVel;
        this.rotVelocity = -source.rotVelocity;
        this.bounceRotVel = -source.bounceRotVel;
    }

    public float getPrePositionY()      { return prePositionY; }
    public float getPositionY()         { return positionY; }
    public float getPrePositionX()      { return prePositionX; }
    public float getPositionX()         { return positionX; }
    public float getBounceRotation()    { return wfg_bounceRotation; }
    public float getPreBounceRotation() { return wfg_preBounceRotation; }
    public float getBreastSize()        { return breastSize; }
    public float getPreBreastSize()     { return preBreastSize; }

    private static float distanceFromMedian(int p1, int p2, float point) {
        if (p1 >= p2) throw new IllegalArgumentException("p2 must be > p1");
        if (point < p1 || point > p2) throw new IllegalArgumentException(point + " out of bounds");
        if (point == p1 || point == p2) return 0f;
        float median = (p2 - p1) / 2f;
        point -= p1;
        if (point > median) point = -(median - (point - median));
        return point / median;
    }

    private static float lerp(float t, float a, float b) { return a + t * (b - a); }
    private static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }
}