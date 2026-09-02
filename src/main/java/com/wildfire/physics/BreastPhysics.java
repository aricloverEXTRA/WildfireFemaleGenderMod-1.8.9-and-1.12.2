package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.main.WildfireHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class BreastPhysics {

    public static final float TIGHTNESS_REDUCTION_FACTOR = 0.15F;

    private float bounceVelX = 0f, targetBounceX = 0f, velocityX = 0f, positionX = 0f, prePositionX = 0f;

    private float bounceVel = 0f, targetBounceY = 0f, velocity = 0f, positionY = 0f, prePositionY = 0f;

    private float bounceRotVel = 0f, targetRotVel = 0f, rotVelocity = 0f,
                  bounceRotation = 0f, preBounceRotation = 0f;

    private float breastSize = 0f, preBreastSize = 0f;

    private Vec3 prePos = null;
    private int lastSwingDuration = 6, lastSwingTick = 0;
    private int randomB = 1;
    private double lastVerticalMoveVelocity = 0.0;

    private boolean lastSneaking = false;
    private boolean lastSleeping = false;

    public BreastPhysics() {
        resetPhysics();
    }

    public void resetPhysics() {
        prePositionX = positionX = 0.0f;
        prePositionY = positionY = 0.0f;
        velocityX = velocity = 0.0f;
        bounceVel = bounceVelX = bounceRotVel = targetBounceX = targetBounceY = targetRotVel = 0.0f;
        rotVelocity = 0.0f;
        preBounceRotation = bounceRotation = 0.0f;
        preBreastSize = breastSize = 0.0f;
        prePos = null;
        lastSwingDuration = 6;
        lastSwingTick = 0;
        randomB = 1;
        lastVerticalMoveVelocity = 0.0;
        lastSneaking = false;
        lastSleeping = false;
    }

    private void simplifiedTick(IGenderArmor armor, boolean armorPhysicsOverride) {

        GenderConfig.PlayerGenderSettings s = null;

        if (armorPhysicsOverride) {
            this.preBreastSize = this.breastSize;
            return;
        }
        float tightness = MathHelper.clamp_float(armor != null ? armor.tightness() : 0f, 0f, 1f);

        this.preBreastSize = this.breastSize;
        this.breastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
    }

    public void update(EntityLivingBase entity, IGenderArmor armor) {
        if (entity == null) return;
        try {
            boolean isArmorStand = entity instanceof net.minecraft.entity.item.EntityArmorStand;
            boolean forceSimplified = false;

            try {
                com.wildfire.main.entitydata.EntityConfig cfg = com.wildfire.main.entitydata.EntityConfig.getEntity(entity);
                if (cfg != null) forceSimplified = cfg.forceSimplifiedPhysics;
            } catch (Throwable ignored) {}

            boolean armorPhysicsOverride = false;
            GenderConfig.PlayerGenderSettings settings = null;
            if (entity instanceof EntityPlayer) {
                settings = GenderConfig.getPlayerSettings((EntityPlayer) entity);
                if (settings != null) armorPhysicsOverride = settings.overrideArmorPhysics;
            }

            if (isArmorStand || forceSimplified) {

                if (entity instanceof EntityPlayer && settings != null) {
                    float target = settings.breastSize / 100f;

                    if (!"Male".equals(settings.gender) && settings.breastsEnabled) {
                        this.breastSize = target;
                        if (!armorPhysicsOverride) {
                            float tightness = MathHelper.clamp_float(armor != null ? armor.tightness() : 0f, 0f, 1f);
                            this.breastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
                        }
                    } else {
                        this.breastSize = 0f;
                    }
                    this.preBreastSize = this.breastSize;
                }
                return;
            }

            this.prePositionX = this.positionX;
            this.prePositionY = this.positionY;
            this.preBounceRotation = this.bounceRotation;
            this.preBreastSize = this.breastSize;

            if (this.prePos == null) {
                this.prePos = new Vec3(entity.posX, entity.posY, entity.posZ);
                if (entity instanceof EntityPlayer && settings != null) {

                    if (!"Male".equals(settings.gender) && settings.breastsEnabled) {
                        this.breastSize = this.preBreastSize = settings.breastSize / 100f;
                    }
                }
                return;
            }

            float targetBreastSize = 0f;
            float bounceMultiplier = 0.333f;
            float floppiness = 0.75f;
            boolean uniboob = false;
            boolean canHaveBreasts = true;

            if (entity instanceof EntityPlayer && settings != null) {
                canHaveBreasts = !"Male".equals(settings.gender) && settings.breastsEnabled;
                if (canHaveBreasts) {
                    targetBreastSize = settings.breastSize / 100f;

                    bounceMultiplier = MathHelper.clamp_float((settings.intensity / 100f) * 0.333f, 0f, 0.5f);

                    floppiness = MathHelper.clamp_float(settings.momentum / 100f, 0.25f, 1f);
                    uniboob = settings.breastsUniboob;
                } else {
                    targetBreastSize = 0f;
                }
            } else {

                targetBreastSize = 0.6f;
            }

            if (!canHaveBreasts) {
                targetBreastSize = 0f;
            } else {
                float tightness = MathHelper.clamp_float(armor != null ? armor.tightness() : 0f, 0f, 1f);
                if (armorPhysicsOverride) tightness = 0f;
                targetBreastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
            }

            if (this.breastSize < targetBreastSize) {
                this.breastSize += Math.abs(this.breastSize - targetBreastSize) / 2f;
            } else {
                this.breastSize -= Math.abs(this.breastSize - targetBreastSize) / 2f;
            }

            Vec3 curPos = new Vec3(entity.posX, entity.posY, entity.posZ);
            Vec3 motion = curPos.subtract(this.prePos);
            this.prePos = curPos;

            float breastWeight = targetBreastSize * 1.25f;

            float bounceIntensity = targetBreastSize * 3f * Math.round(bounceMultiplier * 3 * 100) / 100f;
            float resistance = MathHelper.clamp_float(armor != null ? armor.physicsResistance() : 0f, 0f, 1f);
            if (armorPhysicsOverride) resistance = 0f;
            bounceIntensity *= 1 - resistance;

            if (!uniboob) {

                bounceIntensity *= WildfireHelper.randFloat(0.5f, 2.5f);
            }

            java.util.Random rand = entity.worldObj != null ? entity.worldObj.rand : new java.util.Random();

            tickMovement(entity, rand, motion, bounceIntensity, breastWeight);
            tickPose(entity, bounceIntensity);
            tickVehicle(entity, rand, bounceIntensity, breastWeight);
            tickArmSwing(entity, rand, bounceIntensity);
            finishTick(floppiness);

            this.positionX = clamp(this.positionX, -0.8f, 0.8f);
            this.positionY = clamp(this.positionY, -0.5f, 1.5f);

        } catch (Throwable t) {

            System.err.println("[WFG] BreastPhysics.update error: " + t.getMessage());
        }
    }

    private void tickMovement(EntityLivingBase entity, java.util.Random random, Vec3 motion,
                              float bounceIntensity, float breastWeight) {
        double vertVelocity = entity.motionY;
        if ((lastVerticalMoveVelocity <= 0 && vertVelocity > 0) ||
            (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {

            boolean isUniboob = false;
            if (entity instanceof EntityPlayer) {
                GenderConfig.PlayerGenderSettings s = GenderConfig.getPlayerSettings((EntityPlayer) entity);
                if (s != null) isUniboob = s.breastsUniboob;
            }
            randomB = isUniboob ? 1 : (random.nextBoolean() ? -1 : 1);
        }
        lastVerticalMoveVelocity = vertVelocity;

        this.targetBounceY = (float) motion.yCoord * bounceIntensity;
        this.targetBounceY += breastWeight;

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motion.yCoord * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float speedValue = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ) / 0.2F;
        speedValue = speedValue * speedValue * speedValue;
        if (speedValue < 1.0F) speedValue = 1.0F;

        this.targetBounceY += MathHelper.cos(entity.limbSwing * 0.6662F + (float) Math.PI) * 0.5F * entity.limbSwingAmount * 0.5F / speedValue;
    }

    private void tickPose(EntityLivingBase entity, float bounceIntensity) {
        boolean isSneaking = entity.isSneaking();
        boolean isSleeping = entity.isPlayerSleeping();
        if (isSneaking != lastSneaking || isSleeping != lastSleeping) {
            if (isSneaking || lastSneaking) {
                this.targetBounceY += bounceIntensity;
            } else if (isSleeping || lastSleeping) {
                this.targetBounceY = bounceIntensity;
            }
            lastSneaking = isSneaking;
            lastSleeping = isSleeping;
        }
    }

    private void tickVehicle(EntityLivingBase entity, java.util.Random random, float bounceIntensity, float breastWeight) {
        Entity vehicle = entity.ridingEntity;
        if (vehicle == null) return;

        double vx = vehicle.motionX, vz = vehicle.motionZ;
        double speedSqr = vx * vx + vehicle.motionY * vehicle.motionY + vz * vz;

        boolean suppressRotation = false;
        if (vehicle instanceof net.minecraft.entity.passive.EntityChicken) suppressRotation = true;
        if (vehicle instanceof net.minecraft.entity.passive.EntityHorse) {
            net.minecraft.entity.passive.EntityHorse horse = (net.minecraft.entity.passive.EntityHorse) vehicle;

            try {
                if (!horse.isHorseSaddled()) suppressRotation = true;
            } catch (Throwable ignored) {}
        }

        if (vehicle instanceof net.minecraft.entity.item.EntityMinecart) {
            float speed = (float) speedSqr;
            if (speed > 0.2F && random.nextDouble() * speed < 0.5) {
                this.targetBounceY = (random.nextBoolean() ? -bounceIntensity : bounceIntensity) / 6f;
                this.targetBounceY += breastWeight;
            }
            return;
        }

        if (vehicle instanceof net.minecraft.entity.passive.EntityHorse) {
            float movement = (float) speedSqr;
            if (vehicle.ticksExisted % clampMovement(movement) == 5 && movement > 0.05f) {
                this.targetBounceY = bounceIntensity / 4f;
                this.targetBounceY += breastWeight;
            }
            return;
        }

        if (vehicle instanceof net.minecraft.entity.passive.EntityPig) {
            float movement = (float) speedSqr;
            if (vehicle.ticksExisted % clampMovement(movement) == 5 && movement > 0.002f) {
                this.targetBounceY = (bounceIntensity * MathHelper.clamp_float(movement * 75, 0.1f, 1f)) / 4f;
                this.targetBounceY += breastWeight;
            }
            return;
        }

        if (vehicle instanceof net.minecraft.entity.item.EntityBoat) {

            this.targetBounceY += bounceIntensity / 4.5f;
            return;
        }

        if (speedSqr > 0.2 && random.nextDouble() * speedSqr < 0.5) {
            this.targetBounceY = (random.nextBoolean() ? -bounceIntensity : bounceIntensity) / 6f;
            this.targetBounceY += breastWeight;
        } else {
            this.targetBounceY += bounceIntensity / 4.5f;
        }
    }

    private void tickArmSwing(EntityLivingBase entity, java.util.Random random, float bounceIntensity) {
        int swingDuration = entity.swingProgressInt;

        if ((swingDuration > 1 || lastSwingDuration > 1) && !entity.isPlayerSleeping()) {
            float rawAmplifier = 0f;
            if (swingDuration < 6) rawAmplifier = 0.15f * (6 - swingDuration);
            else if (swingDuration > 6) rawAmplifier = -0.055f * (swingDuration - 6);

            float amplifier = MathHelper.clamp_float(1 + rawAmplifier, 0.6f, 1.3f);

            boolean isUniboob = false;
            if (entity instanceof EntityPlayer) {
                GenderConfig.PlayerGenderSettings s = GenderConfig.getPlayerSettings((EntityPlayer) entity);
                if (s != null) isUniboob = s.breastsUniboob;
            }

            int swingTickDelta = entity.swingProgressInt - lastSwingTick;
            float swingProgress = distanceFromMedian(0, lastSwingDuration, MathHelper.clamp_float(lastSwingTick, 0, lastSwingDuration));

            boolean towardRight = swingProgress > -0.2f;

            if (entity.isSwingInProgress && entity.ticksExisted % MathHelper.clamp_int(swingDuration - 1, 1, 5) == 0) {
                float sign = isUniboob ? 1f : (random.nextDouble() > 0.5 ? -0.25f : 0.25f);
                this.targetBounceY += sign * amplifier * bounceIntensity * 0.8f;

                float xAmp = MathHelper.clamp_float(1f + (rawAmplifier * (rawAmplifier < 0 ? 1.625f : 0.8f)), 0.25f, 1.225f);
                int handSign = (entity.getEntityId() & 1) == 0 ? -1 : 1;
                this.targetBounceX = 0.325f * xAmp * bounceIntensity * 0.45f * handSign;
            }

            if (swingTickDelta < 0 && lastSwingTick != lastSwingDuration - 1) {
                this.targetRotVel += (towardRight ? -4f : 4f) * Math.abs(swingProgress) * bounceIntensity * 0.45f;
            } else if (entity.isSwingInProgress && swingDuration > 1) {
                this.targetRotVel += (towardRight ? -0.2f : 0.2f) * amplifier * bounceIntensity * 0.45f;
            }
            lastSwingTick = entity.swingProgressInt;
        }
        if (!entity.isSwingInProgress) lastSwingTick = 0;
        lastSwingDuration = Math.max(swingDuration, 1);
    }

    private void finishTick(float floppiness) {
        float percent = floppiness;
        float bounceAmount = 0.45f * (1f - percent) + 0.15f;
        bounceAmount = MathHelper.clamp_float(bounceAmount, 0.15f, 0.6f);
        float delta = 2.25f - bounceAmount;

        float distanceFromMin = Math.abs(bounceVel + 1.5f) * 0.5f;
        float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;
        if (bounceVel < -0.5f) targetBounceY += distanceFromMin;
        if (bounceVel > 2.5f) targetBounceY -= distanceFromMax;

        targetBounceY = MathHelper.clamp_float(targetBounceY, -1.5f, 2.5f);
        targetRotVel = MathHelper.clamp_float(targetRotVel, -25f, 25f);

        this.velocity = lerp(bounceAmount, this.velocity, (targetBounceY - bounceVel) * delta);
        this.bounceVel += this.velocity * percent * 1.1625f;

        this.velocityX = lerp(bounceAmount, this.velocityX, (targetBounceX - bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity = lerp(bounceAmount, this.rotVelocity, (targetRotVel - bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        this.bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        if (this.positionY < -0.5f) this.positionY = -0.5f;
        if (this.positionY > 1.5f) {
            this.positionY = 1.5f;
            this.velocity = 0f;
        }
    }

    private float calcRotation(EntityLivingBase entity, float bounceIntensity) {
        Entity vehicle = entity.ridingEntity;
        if (vehicle != null) {

            if (vehicle instanceof net.minecraft.entity.passive.EntityChicken) return 0f;
            if (vehicle instanceof net.minecraft.entity.passive.EntityHorse) {
                try {
                    net.minecraft.entity.passive.EntityHorse h = (net.minecraft.entity.passive.EntityHorse) vehicle;
                    if (!h.isHorseSaddled()) return 0f;
                } catch (Throwable ignored) {}
            }

            float prev = vehicle.prevRotationYaw;
            float cur = vehicle.rotationYaw;
            return -((cur - prev) / 15f) * bounceIntensity;
        }
        return -((entity.rotationYaw - entity.prevRotationYaw) / 15f) * bounceIntensity;
    }

    public void syncFrom(BreastPhysics source) {
        this.positionX = -source.positionX;
        this.prePositionX = -source.prePositionX;
        this.positionY = source.positionY;
        this.prePositionY = source.prePositionY;
        this.bounceRotation = -source.bounceRotation;
        this.preBounceRotation = -source.preBounceRotation;
        this.breastSize = source.breastSize;
        this.preBreastSize = source.preBreastSize;
        this.velocityX = -source.velocityX;
        this.velocity = source.velocity;
        this.bounceVelX = -source.bounceVelX;
        this.bounceVel = -source.bounceVel;
        this.rotVelocity = -source.rotVelocity;
        this.bounceRotVel = -source.bounceRotVel;
    }

    public float getPrePositionY() { return prePositionY; }
    public float getPositionY() { return positionY; }
    public float getPrePositionX() { return prePositionX; }
    public float getPositionX() { return positionX; }
    public float getBounceRotation() { return bounceRotation; }
    public float getPreBounceRotation() { return preBounceRotation; }
    public float getBreastSize() { return breastSize; }
    public float getPreBreastSize() { return preBreastSize; }

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
    private int clampMovement(float movement) { return Math.max((int) (10 - 2 * movement), 1); }
}
