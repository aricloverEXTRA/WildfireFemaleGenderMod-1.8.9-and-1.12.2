package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ButtocksPhysics {

    // X-Axis
    private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
    // Y-Axis
    private float bounceVelY = 0, targetBounceY = 0, velocityY = 0, positionY, prePositionY;
    // Rotation
    private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, bounceRotation, preBounceRotation;

    private float buttocksSize = 0, preButtocksSize = 0;

    private EntityPose lastPose;
    private Vec3d prePos;

    private final EntityConfig entityConfig;

    public ButtocksPhysics(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    @Environment(EnvType.CLIENT)
    public void update(LivingEntity entity, IGenderArmor armor) {
        this.prePositionY = this.positionY;
        this.prePositionX = this.positionX;
        this.preBounceRotation = this.bounceRotation;
        this.preButtocksSize = this.buttocksSize;

        if (this.prePos == null) {
            this.prePos = entity.getPos();
            return;
        }

        float buttocksWeight = entityConfig.getButtocksSize() * 1.25f;
        float targetButtocksSize = entityConfig.getButtocksSize();

        if (!entityConfig.getGender().canHaveButtocks()) {
            targetButtocksSize = 0;
        } else {
            float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
            if (entityConfig.getArmorPhysicsOverride()) tightness = 0;
            targetButtocksSize *= 1 - 0.15F * tightness;
        }

        buttocksSize += (buttocksSize < targetButtocksSize) ? Math.abs(buttocksSize - targetButtocksSize) / 2f : -Math.abs(buttocksSize - targetButtocksSize) / 2f;

        Vec3d motion = entity.getPos().subtract(this.prePos);
        this.prePos = entity.getPos();

        float bounceIntensity = (targetButtocksSize * 3f) * Math.round((entityConfig.getBounceMultiplier() * 3) * 100) / 100f;
        float resistance = MathHelper.clamp(armor.physicsResistance(), 0, 1);
        if (entityConfig.getArmorPhysicsOverride()) resistance = 0;

        bounceIntensity *= 1 - resistance;

        this.targetBounceY = (float) motion.y * bounceIntensity;
        this.targetBounceY += buttocksWeight;
        float horizVel = (float) Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2)) * (bounceIntensity);

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float f2 = (float) entity.getVelocity().lengthSquared() / 0.2F;
        f2 = f2 * f2 * f2;
        if (f2 < 1.0F) f2 = 1.0F;
        this.targetBounceY += MathHelper.cos(entity.limbAnimator.getPos() * 0.6662F + (float) Math.PI) * 0.5F * entity.limbAnimator.getSpeed() * 0.5F / f2;

        EntityPose pose = entity.getPose();
        if (pose != lastPose) {
            if (pose == EntityPose.CROUCHING || lastPose == EntityPose.CROUCHING) {
                this.targetBounceY += bounceIntensity;
            } else if (pose == EntityPose.SLEEPING || lastPose == EntityPose.SLEEPING) {
                this.targetBounceY = bounceIntensity;
            }
            lastPose = pose;
        }
    }

    private static float calcRotation(LivingEntity entity, float bounceIntensity) {
        return -((entity.bodyYaw - entity.prevBodyYaw) / 15f) * bounceIntensity;
    }
}
