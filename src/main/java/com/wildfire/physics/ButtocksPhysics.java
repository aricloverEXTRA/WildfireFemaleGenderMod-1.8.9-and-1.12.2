/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.physics;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.WildfireHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ButtocksPhysics {

    //X-Axis
    private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
    //Y-Axis
    private float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;
    //Rotation
    private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, wfg_bounceRotation, wfg_preBounceRotation;

    private float buttocksSize = 0, preButtocksSize = 0;

    private EntityPose lastPose;
    private int lastSwingDuration = 6, lastSwingTick = 0;
    private Vec3d prePos;

    private final EntityConfig entityConfig;
    private int randomB = 1;
    private double lastVerticalMoveVelocity;

    public ButtocksPhysics(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    private static boolean vehicleSuppressesRotation(Entity vehicle) {
        return
                vehicle instanceof ChickenEntity
                || vehicle instanceof AbstractHorseEntity horseLike && !horseLike.isSaddled()
                || vehicle instanceof CamelEntity camel && camel.isStationary();
    }

    private static boolean shouldUseVehicleYaw(LivingEntity rider, Entity vehicle) {
        return vehicle.hasControllingPassenger()
                || vehicle instanceof BoatEntity
                || vehicle.getBodyYaw() == rider.getBodyYaw();
    }

    private static float calcRotation(LivingEntity entity, float bounceIntensity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (vehicleSuppressesRotation(vehicle)) {
                return 0f;
            } else if (shouldUseVehicleYaw(entity, vehicle)) {
                if (vehicle instanceof LivingEntity livingVehicle) {
                    return -((livingVehicle.bodyYaw - livingVehicle.prevBodyYaw) / 15f) * bounceIntensity;
                } else {
                    return -((vehicle.getYaw() - vehicle.prevYaw) / 15f) * bounceIntensity;
                }
            }
        }

        return -((entity.bodyYaw - entity.prevBodyYaw) / 15f) * bounceIntensity;
    }

    @Environment(EnvType.CLIENT)
    public void update(LivingEntity entity, IGenderArmor armor) {
        if (entity instanceof ArmorStandEntity) {
            if (entityConfig.getGender().canHaveButtocks()) {
                this.buttocksSize = entityConfig.getButtocksSize();
                if (!entityConfig.getArmorPhysicsOverride()) {
                    float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
                    this.buttocksSize *= 1 - 0.15F * tightness;
                }
                this.preButtocksSize = this.buttocksSize;
            } else {
                this.preButtocksSize = this.buttocksSize = 0f;
            }
            return;
        }

        this.prePositionY = this.positionY;
        this.prePositionX = this.positionX;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preButtocksSize = this.buttocksSize;

        if(this.prePos == null) {
            this.prePos = entity.getPos();
            return;
        }

        /*{
            float h = 0; //tickDelta
            float i = entity.getLeaningPitch(0);
            float j;
            float k;

            float bodyXRotation = 0;
            float bodyYRotation = 0;

            if (entity.isFallFlying()) {
                j = (float) entity.getRoll() + h;
                k = MathHelper.clamp(j * j / 100.0F, 0.0F, 1.0F);
                if (!entity.isUsingRiptide()) {
                    bodyXRotation = k * (-90.0F - entity.getPitch());
                }

                if (entity instanceof AbstractClientPlayerEntity player) {
                    Vec3d vec3d = entity.getRotationVec(h);
                    Vec3d vec3d2 = player.lerpVelocity(h);
                    double d = vec3d2.horizontalLengthSquared();
                    double e = vec3d.horizontalLengthSquared();
                    if (d > 0.0 && e > 0.0) {
                        double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                        double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                        bodyYRotation = (float) (Math.signum(m) * Math.acos(l));
                    }
                }
            } else if (i > 0.0F) {
                j = entity.isTouchingWater() ? -90.0F - entity.getPitch() : -90.0F;
                k = MathHelper.lerp(i, 0.0F, j);
                bodyXRotation = k;
            } else if (entity.isSleeping()) {
                bodyXRotation = 90f;
            } else if (entity.getPose() == EntityPose.CROUCHING) {
                bodyXRotation = -15f;
            }
        }*/ //unused currently, might be later

        float buttocksWeight = entityConfig.getButtocksSize() * 1.25f;
        float targetButtocksSize = entityConfig.getButtocksSize();

        if (!entityConfig.getGender().canHaveButtocks()) {
            targetButtocksSize = 0;
        } else {
            float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
            if(entityConfig.getArmorPhysicsOverride()) tightness = 0; //override resistance
            //Scale buttocks size by how tight the armor is, clamping at a max adjustment of shrinking by 0.15
            targetButtocksSize *= 1 - 0.15F * tightness;
        }

        buttocksSize += (buttocksSize < targetButtocksSize) ? Math.abs(buttocksSize - targetButtocksSize) / 2f : -Math.abs(buttocksSize - targetButtocksSize) / 2f;

        Vec3d motion = entity.getPos().subtract(this.prePos);
        this.prePos = entity.getPos();

        float bounceIntensity = (targetButtocksSize * 3f) * Math.round((entityConfig.getBounceMultiplier() * 3) * 100) / 100f;
        float resistance = MathHelper.clamp(armor.physicsResistance(), 0, 1);
        if(entityConfig.getArmorPhysicsOverride()) resistance = 0; //override resistance

        //Adjust bounce intensity by physics resistance of the worn armor
        bounceIntensity *= 1 - resistance;

        if(!entityConfig.getButtocks().isUnibutt()) {
            bounceIntensity = bounceIntensity * WildfireHelper.randFloat(0.5f, 1.5f);
        }

        double vertVelocity = entity.getVelocity().y;
        // Randomize which side the buttocks will angle toward when the player jumps/has upward velocity applied to them,
        // or stops falling
        if((lastVerticalMoveVelocity <= 0 && vertVelocity > 0) || (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {
            randomB = entity.getWorld().random.nextBoolean() ? -1 : 1;
        }
        lastVerticalMoveVelocity = vertVelocity;

        this.targetBounceY = (float) motion.y * bounceIntensity;
        this.targetBounceY += buttocksWeight;
        float horizVel = (float) Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2)) * (bounceIntensity);

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motion.y * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float f2 = (float) entity.getVelocity().lengthSquared() / 0.2F;
        f2 = f2 * f2 * f2;
        if(f2 < 1.0F) f2 = 1.0F;
        this.targetBounceY += MathHelper.cos(entity.limbAnimator.getPos() * 0.6662F + (float)Math.PI) * 0.5F * entity.limbAnimator.getSpeed() * 0.5F / f2;

        EntityPose pose = entity.getPose();
        if(pose != lastPose) {
            if(pose == EntityPose.CROUCHING || lastPose == EntityPose.CROUCHING) {
                this.targetBounceY += bounceIntensity;
            } else if(pose == EntityPose.SLEEPING || lastPose == EntityPose.SLEEPING) {
                this.targetBounceY = bounceIntensity;
            }
            lastPose = pose;
        }

        //button option for extra entities
        if(entity.getVehicle() != null) {
            if(entity.getVehicle() instanceof BoatEntity boat) {
                int rowTime = (int) boat.lerpPaddlePhase(0, entity.limbAnimator.getPos());
                int rowTime2 = (int) boat.lerpPaddlePhase(1, entity.limbAnimator.getPos());

                float rotationL = (float) MathHelper.clampedLerp(-(float)Math.PI / 3F, -0.2617994F, (double) ((MathHelper.sin(-rowTime2) + 1.0F) / 2.0F));
                float rotationR = (float) MathHelper.clampedLerp(-(float)Math.PI / 4F, (float)Math.PI / 4F, (double) ((MathHelper.sin(-rowTime + 1.0F) + 1.0F) / 2.0F));
                if(rotationL < -1 || rotationR < -0.6f) {
                    this.targetBounceY = bounceIntensity / 3.25f;
                }
            } else if(entity.getVehicle() instanceof MinecartEntity cart) {
                float speed = (float) cart.getVelocity().lengthSquared();
                if(Math.random() * speed < 0.5f && speed > 0.2f) {
                    this.targetBounceY = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
                    this.targetBounceY += buttocksWeight;
                }
            } else if(entity.getVehicle() instanceof AbstractHorseEntity horse) {
                float movement = (float) horse.getVelocity().lengthSquared();
                if(horse.age % clampMovement(movement) == 5 && movement > 0.05f) {
                    this.targetBounceY = bounceIntensity / 4f;
                    this.targetBounceY += buttocksWeight;
                }
            } else if(entity.getVehicle() instanceof PigEntity pig) {
                float movement = (float) pig.getVelocity().lengthSquared();
                if(pig.age % clampMovement(movement) == 5 && movement > 0.002f) {
                    this.targetBounceY = (bounceIntensity * MathHelper.clamp(movement * 75, 0.1f, 1f)) / 4f;
                    this.targetBounceY += buttocksWeight;
                }
            } else if(entity.getVehicle() instanceof StriderEntity strider) {
                double heightOffset = (double)strider.getHeight() - 0.19
                        + (double)(0.12F * MathHelper.cos(strider.limbAnimator.getPos() * 1.5f)
                        * 2F * Math.min(0.25F, strider.limbAnimator.getSpeed()));
                this.targetBounceY += ((float) (heightOffset * 3f) - 4.5f) * bounceIntensity;
            }
        }

        int swingDuration = entity.getHandSwingDuration();
        // Require that either the current swing duration is 2 ticks, or the swing duration from the previous tick is,
        // as any faster and the arm effectively doesn't swing at all; we check the previous tick's swing duration for
        // reasons explained later on in this block
        if((swingDuration > 1 || lastSwingDuration > 1) && pose != EntityPose.SLEEPING) {

            float amplifier = 0f;
            if(swingDuration < 6) {
                amplifier = 0.15f * (6 - swingDuration);
            } else if(swingDuration > 6) {
                amplifier = -0.067f * (swingDuration - 6);
            }
            // Cap our amplifier at the swing durations of Mining Fatigue III/Haste II
            amplifier = MathHelper.clamp(1 + amplifier, 0.6f, 1.3f);

            // consistently apply even with short swing durations, such as with haste
            int everyNthTick = MathHelper.clamp(swingDuration - 1, 1, 5);
            if(entity.handSwinging && entity.age % everyNthTick == 0) {
                float hasteMult = MathHelper.clamp(everyNthTick / 5f, 0.4f, 1f);
                this.targetBounceY += (Math.random() > 0.5 ? -0.25f : 0.25f) * amplifier * bounceIntensity * hasteMult;

                this.targetBounceX = (0.5f * bounceIntensity) * (entity.getMainArm()==Arm.RIGHT?1f:-1f);
            }

            int swingTickDelta = entity.handSwingTicks - lastSwingTick;
            float swingProgress = distanceFromMedian(0, lastSwingDuration, MathHelper.clamp(lastSwingTick, 0, lastSwingDuration));
            Arm swingingArm = entity.preferredHand == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();

            if(swingTickDelta < 0 && lastSwingTick != lastSwingDuration - 1) {
                // Add a bit of counter-rotation back toward the currently swinging arm if the previous arm swing
                // animation is interrupted
                // Note that we don't check if the player's arm is currently swinging here to account for cases like
                // haste being used to reset a player's swing; one notable example of this is Wynncraft's spell casting,
                // which applies haste to the player when a spell is successfully cast.
                this.targetRotVel += (swingingArm == Arm.RIGHT ? -2.5f : 2.5f) * Math.abs(swingProgress) * bounceIntensity;
            } else if(entity.handSwinging && swingDuration > 1) {
                // Otherwise if the swing animation isn't interrupted, attempt to rotate slightly counter to the
                // direction that the body is currently moving
                Arm swingingToward = swingProgress > 0f ? swingingArm.getOpposite() : swingingArm;
                this.targetRotVel += (swingingToward == Arm.RIGHT ? -0.2f : 0.2f) * amplifier * bounceIntensity;
            }
            lastSwingTick = entity.handSwingTicks;
        }
        if(!entity.handSwinging) {
            lastSwingTick = 0;
        }
        lastSwingDuration = Math.max(swingDuration, 1);

        /*if(plr.getPose() == EntityPose.SWIMMING) {
            //System.out.println(1 - plr.getRotationVec(tickDelta).getY());
            rotationMultiplier = 1 - (float) plr.getRotationVec(tickDelta).getY();
        }*/


        float percent =  entityConfig.getFloppiness();
        float bounceAmount = 0.45f * (1f - percent) + 0.15f; //0.6f * percent - 0.15f;
        bounceAmount = MathHelper.clamp(bounceAmount, 0.15f, 0.6f);
        float delta = 2.25f - bounceAmount;
        //if(plr.isInWater()) delta = 0.75f - (1f * bounceAmount); //water resistance

        float distanceFromMin = Math.abs(bounceVel + 1.5f) * 0.5f;
        float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;

        if(bounceVel < -0.5f) {
            targetBounceY += distanceFromMin;
        }
        if(bounceVel > 2.5f) {
            targetBounceY -= distanceFromMax;
        }

        targetBounceY = MathHelper.clamp(targetBounceY, -1.5f, 2.5f);
        targetRotVel = MathHelper.clamp(targetRotVel, -25f, 25f);

        this.velocity = MathHelper.lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
        this.bounceVel += this.velocity * percent * 1.1625f;

        //X
        this.velocityX = MathHelper.lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity = MathHelper.lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        this.wfg_bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        if(this.positionY < -0.5f) this.positionY = -0.5f;
        if(this.positionY > 1.5f) {
            this.positionY = 1.5f;
            this.velocity = 0;
        }

    }

    public float getButtocksSize(float partialTicks) {
        return MathHelper.lerp(partialTicks, preButtocksSize, buttocksSize);
    }

	public float getPrePositionY() {
		return this.prePositionY;
	}
	public float getPositionY() {
		return this.positionY;
	}

	public float getPrePositionX() {
		return this.prePositionX;
	}
	public float getPositionX() {
		return this.positionX;
	}

	public float getBounceRotation() {
		return this.wfg_bounceRotation;
	}
	public float getPreBounceRotation() {
		return this.wfg_preBounceRotation;
	}

	private int clampMovement(float movement) {
		return Math.max((int) (10 - movement*2f), 1);
	}

	/**
	 * Return the distance from the median of the two provided boundary points from a given point
	 *
	 * @param p1    Lower boundary point
	 * @param p2    Upper boundary point
	 * @param point The target point within the range of {@code p1} and {@code p2} to get the distance from the median of
	 *
	 * @return A {@code float} indicating how far the provided {@code point} is from the median of the two boundary
	 *         points, with {@code 1f} being at the median exactly, and {@code 0f} being at either of the two
	 *         provided boundary points.<br>
	 *         If the provided point is in the latter half of the range between the two boundary points, the returned
	 *         float will be negative.
	 *
	 * @throws IllegalArgumentException If {@code p1} is equal to or greater than {@code p2},
	 *                                  or if {@code point} is not within the specified range.
	 */
	@SuppressWarnings("SameParameterValue")
	private static float distanceFromMedian(final int p1, final int p2, float point) {
		// sanity checks
		if(p1 >= p2) {
			throw new IllegalArgumentException("p2 must be greater than p1");
		}
		if(point < p1 || point > p2) {
			throw new IllegalArgumentException(point + " is not within bounds of (" + p1 + ", " + p2 + ")");
		}

		if(point == p1 || point == p2) {
			return 0;
		}
		// subtract p1 to get the actual inner range, then divide to get the median
		float median = (p2 - p1) / 2f;
		point -= p1;
		if(point > median) {
			// invert the provided point to instead become smaller the further we are away from the median
			// in the latter half of the specified range
			point = -(median - (point - median));
		}
		return point / median;
	}
}
