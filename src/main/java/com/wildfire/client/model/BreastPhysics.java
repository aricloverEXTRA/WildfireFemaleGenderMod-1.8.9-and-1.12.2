package com.wildfire.client.model;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.Vec3;
import net.minecraft.util.MathHelper;
import com.wildfire.api.IGenderArmor;

public class BreastPhysics {
    private Vec3 prePos;
    private Vec3 lastRidingEntityPos = new Vec3(0, 0, 0);
    private float targetBounceY, targetBounceX;
    private float velocity, velocityX, rotVelocity;
    private float bounceVel, bounceVelX, bounceRotVel;

    public BreastPhysics() {
        this.prePos = new Vec3(0, 0, 0);
    }

    public void update(EntityPlayer plr, IGenderArmor armor) {
        Vec3 motion = plr.getPositionVector().subtract(this.prePos);
        this.prePos = plr.getPositionVector();

        if (plr.ridingEntity != null) {
            Vec3 currentPos = plr.ridingEntity.getPositionVector();
            float movement = (float) currentPos.distanceTo(lastRidingEntityPos);

            if (plr.ridingEntity instanceof EntityPig) {
                EntityPig ridingPig = (EntityPig) plr.ridingEntity;
                if (ridingPig.ticksExisted % Math.max(5, movement) == 5 && movement > 0.08f) {
                }
            }

            if (plr.ridingEntity instanceof EntityHorse) {
                EntityHorse ridingHorse = (EntityHorse) plr.ridingEntity;

                if (ridingHorse.motionY > 0.0D) {
                    this.targetBounceY += MathHelper.cos(ridingHorse.rotationYaw * 0.6662F) * 0.5F;
                }
            }

            lastRidingEntityPos = currentPos;
        }

        float walkSpeed = calculateWalkSpeed(plr);
        this.targetBounceY += MathHelper.cos(walkSpeed * 0.6662F + (float) Math.PI) * 0.5F * walkSpeed * 0.5F / 0.2F;

        this.targetBounceY = Math.max(-2.0f, Math.min(this.targetBounceY, 2.0f));

        float bounceAmount = 0.2f;
        this.velocity = lerp(this.velocity, this.targetBounceY - this.bounceVel, bounceAmount);
        this.velocityX = lerp(this.velocityX, this.targetBounceX - this.bounceVelX, bounceAmount);
        this.rotVelocity = lerp(this.rotVelocity, this.targetBounceY - this.bounceRotVel, bounceAmount);

        this.bounceVel += this.velocity;
        this.bounceVelX += this.velocityX;
        this.bounceRotVel += this.rotVelocity;
    }

    private float calculateWalkSpeed(EntityPlayer plr) {
        return plr.isSprinting() ? 1.5F : 0.5F;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Getter methods for external use
    public float getPositionX() {
        return this.velocityX;
    }

    public float getPositionY() {
        return this.velocity;
    }

    public float getBounceRotation() {
        return this.rotVelocity;
    }
}
