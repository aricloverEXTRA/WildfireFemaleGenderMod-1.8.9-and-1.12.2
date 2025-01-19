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

package com.wildfire.render;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.Buttocks;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.physics.ButtocksPhysics;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.WildfireModelRenderer.ButtocksModelBox;
import com.wildfire.render.WildfireModelRenderer.OverlayModelBox;
import com.wildfire.render.WildfireModelRenderer.PositionTextureVertex;

import java.lang.Math;
import java.util.Objects;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

@Environment(EnvType.CLIENT)
public class GenderLayer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {

	private static final float DEG_TO_RAD = (float) (Math.PI / 180);

	private BreastModelBox lBreast, rBreast;
	private ButtocksModelBox lButtocks, rButtocks;
	private static final OverlayModelBox lBreastWear, rBreastWear;
	private static final OverlayModelBox lButtocksWear, rButtocksWear;

	private final FeatureRendererContext<S, M> context;

	private float preBreastSize, preBreastOffsetZ;
	private float preButtocksSize, preButtocksOffsetZ;
	private Breasts breasts;
	private Buttocks buttocks;
	protected ItemStack armorStack;
	protected IGenderArmor genderArmor;
	protected boolean isChestplateOccupied, bounceEnabled, breathingAnimation;
	protected float breastOffsetX, breastOffsetY, breastOffsetZ, lPhysPositionY, lPhysPositionX, rPhysPositionY, rPhysPositionX,
			lPhysBounceRotation, rPhysBounceRotation, breastSize, zOffset, outwardAngle;
	private float lPhysButtocksPositionY, lPhysButtocksPositionX, rPhysButtocksPositionY, rPhysButtocksPositionX,
  	            lPhysButtocksBounceRotation, rPhysButtocksBounceRotation, buttocksSize, buttocksOffsetX, buttocksOffsetY, buttocksOffsetZ;

	static {
		lBreastWear = new OverlayModelBox(true, 64, 64, 17, 34, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, false);
		rBreastWear = new OverlayModelBox(false, 64, 64, 21, 34, 0, 0.0F, 0F, 4, 5, 3, 0.0F, false);
	    lButtocksWear = new OverlayModelBox(true, 64, 64, 17, 34, -4F, -1.0F, 4F, 4, 5, 3, 0.0F, false);
 	   	rButtocksWear = new OverlayModelBox(false, 64, 64, 21, 34, 0, -1.0F, 4F, 4, 5, 3, 0.0F, false);
	}

	public GenderLayer(FeatureRendererContext<S, M> render) {
		super(render);
		this.context = render;
		// this can't be static or final as we need the ability to resize this during render time
		lBreast = new BreastModelBox(64, 64, 16, 17, -4F, 0.0F, 0F, 4, 5, 4, 0.0F, false);
		rBreast = new BreastModelBox(64, 64, 20, 17, 0, 0.0F, 0F, 4, 5, 4, 0.0F, false);
		lButtocks = new ButtocksModelBox(64, 64, 16, 17, -4F, -1.0F, 4F, 4, 5, 4, 0.0F, false);
		rButtocks = new ButtocksModelBox(64, 64, 20, 17, 0, -1.0F, 4F, 4, 5, 4, 0.0F, false);
	}

	/**
	 * Convenience method for getting the captured entity object from a render state
	 */
	protected @Nullable LivingEntity getEntity(S state) {
		return ((RenderStateEntityCapture)state).getEntity();
	}

	/**
	 * Copy of {@code LivingEntityRenderer#getRenderLayer}
	 */
	private @Nullable RenderLayer getRenderLayer(S state) {
		boolean bodyVisible = !state.invisible;
		boolean translucent = state.invisible && !state.invisibleToPlayer;
		boolean glowing = state.hasOutline;

		Identifier texture;
		if(this.context instanceof LivingEntityRenderer<?, S, M> livingEntityRenderer) {
			texture = livingEntityRenderer.getTexture(state);
		} else {
			throw new IllegalStateException("context renderer is not a LivingEntityRenderer subclass");
		}

		if(translucent) {
			return RenderLayer.getItemEntityTranslucentCull(texture);
		} else if(bodyVisible) {
			return this.getContextModel().getLayer(texture);
		} else {
			return glowing ? RenderLayer.getOutline(texture) : null;
		}
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, S state, float limbAngle, float limbDistance) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player == null) {
			// we're currently in a menu; we won't have any data loaded to begin with, so just give up early
			return;
		}

		LivingEntity ent = getEntity(state);
		if(ent == null) return;

		EntityConfig entityConfig = EntityConfig.getEntity(ent);

		try {
			if(!setupRender(state, entityConfig)) return;
			int overlay = LivingEntityRenderer.getOverlay(state, 0);

			//noinspection CodeBlock2Expr
			renderSides(state, getContextModel(), matrixStack, side -> {
				renderBreast(state, matrixStack, vertexConsumerProvider, light, overlay, side);
				renderButtocks(state, matrixStack, vertexConsumerProvider, light, overlay, side);
			});
		} catch(Exception e) {
			WildfireGender.LOGGER.error("Failed to render breast and buttocks layer", e);
		}
	}

	/**
	 * Common logic for setting up breast rendering
	 *
	 * @return {@code true} if rendering should continue
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected boolean setupRender(S state, EntityConfig entityConfig) {
		if (!GlobalConfig.RENDER_BREASTS) return false;

		float partialTicks = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
		LivingEntity entity = Objects.requireNonNull(getEntity(state), "getEntity()");

		armorStack = state.equippedChestStack;
		genderArmor = WildfireHelper.getArmorConfig(armorStack);
		isChestplateOccupied = genderArmor.coversBreasts() && !entityConfig.getArmorPhysicsOverride();

		if (genderArmor.alwaysHidesBreasts() || (!entityConfig.showBreastsInArmor() && isChestplateOccupied)) {
			return false;
		}

		if (!isLayerVisible(state)) {
			return false;
		}

		// Set up breasts
		breasts = entityConfig.getBreasts();
		breastOffsetX = Math.round((Math.round(breasts.getXOffset() * 100f) / 100f) * 10) / 10f;
		breastOffsetY = -Math.round((Math.round(breasts.getYOffset() * 100f) / 100f) * 10) / 10f;
		breastOffsetZ = -Math.round((Math.round(breasts.getZOffset() * 100f) / 100f) * 10) / 10f;

		BreastPhysics leftBreastPhysics = entityConfig.getLeftBreastPhysics();
		final float bSize = leftBreastPhysics.getBreastSize(partialTicks);
		outwardAngle = (Math.round(breasts.getCleavage() * 100f) / 100f) * 100f;
		outwardAngle = Math.min(outwardAngle, 10);
		resizeBox(bSize);

		lPhysPositionY = MathHelper.lerp(partialTicks, leftBreastPhysics.getPrePositionY(), leftBreastPhysics.getPositionY());
		lPhysPositionX = MathHelper.lerp(partialTicks, leftBreastPhysics.getPrePositionX(), leftBreastPhysics.getPositionX());
		lPhysBounceRotation = MathHelper.lerp(partialTicks, leftBreastPhysics.getPreBounceRotation(), leftBreastPhysics.getBounceRotation());
    
		if (breasts.isUniboob()) {
			rPhysPositionY = lPhysPositionY;
			rPhysPositionX = lPhysPositionX;
			rPhysBounceRotation = lPhysBounceRotation;
		} else {
			BreastPhysics rightBreastPhysics = entityConfig.getRightBreastPhysics();
			rPhysPositionY = MathHelper.lerp(partialTicks, rightBreastPhysics.getPrePositionY(), rightBreastPhysics.getPositionY());
			rPhysPositionX = MathHelper.lerp(partialTicks, rightBreastPhysics.getPrePositionX(), rightBreastPhysics.getPositionX());
			rPhysBounceRotation = MathHelper.lerp(partialTicks, rightBreastPhysics.getPreBounceRotation(), rightBreastPhysics.getBounceRotation());
		}

		breastSize = Math.min(bSize * 1.5f, 0.7f);
		if (bSize > 0.7f) {
        breastSize = bSize;
		}
		if (breastSize < 0.02f) {
			return false;
		}

		// Set up buttocks
		buttocks = entityConfig.getButtocks();
		buttocksOffsetX = Math.round((Math.round(buttocks.getXOffset() * 100f) / 100f) * 10) / 10f;
		buttocksOffsetY = -Math.round((Math.round(buttocks.getYOffset() * 100f) / 100f) * 10) / 10f;
		buttocksOffsetZ = -Math.round((Math.round(buttocks.getZOffset() * 100f) / 100f) * 10) / 10f;

		ButtocksPhysics leftButtocksPhysics = entityConfig.getLeftButtocksPhysics();
		final float buttSize = leftButtocksPhysics.getButtocksSize(partialTicks);
		outwardAngle = (Math.round(buttocks.getCleavage() * 100f) / 100f) * 100f;
		outwardAngle = Math.min(outwardAngle, 10);
		resizeButtocksBox(buttSize);

		lPhysButtocksPositionY = MathHelper.lerp(partialTicks, leftButtocksPhysics.getPrePositionY(), leftButtocksPhysics.getPositionY());
		lPhysButtocksPositionX = MathHelper.lerp(partialTicks, leftButtocksPhysics.getPrePositionX(), leftButtocksPhysics.getPositionX());
		lPhysButtocksBounceRotation = MathHelper.lerp(partialTicks, leftButtocksPhysics.getPreBounceRotation(), leftButtocksPhysics.getBounceRotation());

		if (buttocks.isUnibutt()) {
			rPhysButtocksPositionY = lPhysButtocksPositionY;
			rPhysButtocksPositionX = lPhysButtocksPositionX;
			rPhysButtocksBounceRotation = lPhysButtocksBounceRotation;
		} else {
			ButtocksPhysics rightButtocksPhysics = entityConfig.getRightButtocksPhysics();
			rPhysButtocksPositionY = MathHelper.lerp(partialTicks, rightButtocksPhysics.getPrePositionY(), rightButtocksPhysics.getPositionY());
			rPhysButtocksPositionX = MathHelper.lerp(partialTicks, rightButtocksPhysics.getPrePositionX(), rightButtocksPhysics.getPositionX());
			rPhysButtocksBounceRotation = MathHelper.lerp(partialTicks, rightButtocksPhysics.getPreBounceRotation(), rightButtocksPhysics.getBounceRotation());
		}

		buttocksSize = Math.min(buttSize * 1.5f, 0.7f);
		if (buttSize > 0.7f) {
			buttocksSize = buttSize;
		}
		if (buttocksSize < 0.02f) {
			return false;
		}

		float resistance = MathHelper.clamp(genderArmor.physicsResistance(), 0, 1);
		breathingAnimation = ((entityConfig.getArmorPhysicsOverride() || resistance <= 0.5F) &&
			(!entity.isSubmergedInWater() || StatusEffectUtil.hasWaterBreathing(entity) ||
			entity.getWorld().getBlockState(new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ())).isOf(Blocks.BUBBLE_COLUMN)));
		bounceEnabled = entityConfig.hasBreastPhysics() && (!isChestplateOccupied || resistance < 1);

		return true;
	}

	protected boolean isLayerVisible(S state) {
		return !state.invisibleToPlayer || state.hasOutline;
	}

	protected void resizeBox(float breastSize) {
		float reducer = -1;
		if(breastSize < 0.84f) reducer++;
		if(breastSize < 0.72f) reducer++;

		if(preBreastSize != breastSize || preBreastOffsetZ != breastOffsetZ) {
			lBreast = new BreastModelBox(64, 64, 16, 17, -4F, 0.0F, 0F, 4, 5, (int) (4 - breastOffsetZ - reducer), 0.0F, false);
			rBreast = new BreastModelBox(64, 64, 20, 17, 0, 0.0F, 0F, 4, 5, (int) (4 - breastOffsetZ - reducer), 0.0F, false);
			preBreastSize = breastSize;
			preBreastOffsetZ = breastOffsetZ;
		}
	}

	protected void resizeButtocksBox(float buttocksSize) {
		float reducer = -1;
		if(buttocksSize < 0.84f) reducer++;
		if(buttocksSize < 0.72f) reducer++;

		if(preButtocksSize != buttocksSize || preButtocksOffsetZ != buttocksOffsetZ) {
			lButtocks = new ButtocksModelBox(64, 64, 16, 17, -4F, 0.0F, 0F, 4, 5, (int) (4 - buttocksOffsetZ - reducer), 0.0F, false);
			rButtocks = new ButtocksModelBox(64, 64, 20, 17, 0, 0.0F, 0F, 4, 5, (int) (4 - buttocksOffsetZ - reducer), 0.0F, false);
			preButtocksSize = buttocksSize;
			preButtocksOffsetZ = buttocksOffsetZ;
		}
	}

    protected void setupTransformations(S state, M model, MatrixStack matrixStack, BreastSide side) {
        if(state.baby) {
            matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
            matrixStack.translate(0f, 0.75f, 0f);
        }

        ModelPart body = model.body;
        matrixStack.translate(body.pivotX * 0.0625f, body.pivotY * 0.0625f, body.pivotZ * 0.0625f);
        if(body.roll != 0.0F || body.yaw != 0.0F || body.pitch != 0.0F) {
            matrixStack.multiply(new Quaternionf().rotationZYX(body.roll, body.yaw, body.pitch));
        }

        if(bounceEnabled) {
            matrixStack.translate((side.isLeft ? lPhysPositionX : rPhysPositionX) / 32f, 0, 0);
            matrixStack.translate(0, (side.isLeft ? lPhysPositionY : rPhysPositionY) / 32f, 0);
        }

        matrixStack.translate((side.isLeft ? breastOffsetX : -breastOffsetX) * 0.0625f, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0625f)); //shift down to correct position

        if(!breasts.isUniboob()) {
            matrixStack.translate(-0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }
        if(bounceEnabled) {
            matrixStack.multiply(new Quaternionf().rotationXYZ(0, (float)((side.isLeft ? lPhysBounceRotation : rPhysBounceRotation) * (Math.PI / 180f)), 0));
        }
        if(!breasts.isUniboob()) {
            matrixStack.translate(0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }

        float rotation = breastSize;
        if(bounceEnabled) {
            matrixStack.translate(0, -0.035f * breastSize, 0); //shift down to correct position
            rotation -= (side.isLeft ? lPhysPositionY : rPhysPositionY) / 12f;
        }

        rotation = Math.min(rotation, breastSize + 0.2f);
        rotation = Math.min(rotation, 1); //hard limit for MAX

        if(isChestplateOccupied) {
            matrixStack.translate(0, 0, 0.01f);
        }

        Quaternionf rotationTransform = new Quaternionf()
                .rotationY((side.isLeft ? outwardAngle : -outwardAngle) * DEG_TO_RAD)
                .rotateX(-35f * rotation * DEG_TO_RAD);

        if(breathingAnimation) {
            float f5 = -MathHelper.cos(state.age * 0.09F) * 0.45F + 0.45F;
            rotationTransform.rotateX(f5 * DEG_TO_RAD);
        }

        matrixStack.multiply(rotationTransform);
        matrixStack.scale(0.9995f, 1f, 1f); //z-fighting FIXXX
    }

    private void renderBreast(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                              int overlay, BreastSide side) {
        RenderLayer breastRenderType = getRenderLayer(state);
        if(breastRenderType == null) return; // only render if the player is visible in some capacity
        int alpha = state.invisible ? ColorHelper.channelFromFloat(0.15f) : 255;
        int color = ColorHelper.getArgb(alpha, 255, 255, 255);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(breastRenderType);
        renderBox(side.isLeft ? lBreast : rBreast, matrixStack, vertexConsumer, light, overlay, color);
        if(state instanceof PlayerEntityRenderState playerState && playerState.jacketVisible) {
            matrixStack.translate(0, 0, -0.015f);
            matrixStack.scale(1.05f, 1.05f, 1.05f);
            renderBox(side.isLeft ? lBreastWear : rBreastWear, matrixStack, vertexConsumer, light, overlay, color);
        }
    }

    protected void renderSides(S state, M model, MatrixStack matrixStack, Consumer<BreastSide> renderer) {
        matrixStack.push();
        try {
            setupTransformations(state, model, matrixStack, BreastSide.LEFT);
            renderer.accept(BreastSide.LEFT);
        } finally {
            matrixStack.pop();
        }

        matrixStack.push();
        try {
            setupTransformations(state, model, matrixStack, BreastSide.RIGHT);
            renderer.accept(BreastSide.RIGHT);
        } finally {
            matrixStack.pop();
        }
    }

    private void renderButtocks(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                              int overlay, BreastSide side) {
        RenderLayer buttocksRenderType = getRenderLayer(state);
        if(buttocksRenderType == null) return; // only render if the player is visible in some capacity
        int alpha = state.invisible ? ColorHelper.channelFromFloat(0.15f) : 255;
        int color = ColorHelper.getArgb(alpha, 255, 255, 255);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(buttocksRenderType);
        renderBox(side.isLeft ? lButtocks : rButtocks, matrixStack, vertexConsumer, light, overlay, color);
        if(state instanceof PlayerEntityRenderState playerState && playerState.jacketVisible) {
            matrixStack.translate(0, 0, -0.015f);
            matrixStack.scale(1.05f, 1.05f, 1.05f);
            renderBox(side.isLeft ? lButtocksWear : rButtocksWear, matrixStack, vertexConsumer, light, overlay, color);
        }
    }

    protected void renderButtocksSides(S state, M model, MatrixStack matrixStack, Consumer<BreastSide> renderer) {
            matrixStack.push();
        try {
                setupTransformationsButtocks(state, model, matrixStack, BreastSide.LEFT);
                renderer.accept(BreastSide.LEFT);
        } finally {
            matrixStack.pop();
        }

        matrixStack.push();
        try {
                setupTransformationsButtocks(state, model, matrixStack, BreastSide.RIGHT);
                renderer.accept(BreastSide.RIGHT);
        } finally {
            matrixStack.pop();
        }
    }

    protected void setupTransformationsButtocks(S state, M model, MatrixStack matrixStack, BreastSide side) {
        if(state.baby) {
            matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
            matrixStack.translate(0f, 0.75f, 0f);
        }

        ModelPart body = model.body;
        matrixStack.translate(body.pivotX * 0.0625f, body.pivotY * 0.0625f, body.pivotZ * 0.0625f);
        if(body.roll != 0.0F || body.yaw != 0.0F || body.pitch != 0.0F) {
            matrixStack.multiply(new Quaternionf().rotationZYX(body.roll, body.yaw, body.pitch));
        }

        if(bounceEnabled) {
            matrixStack.translate((side.isLeft ? lPhysButtocksPositionX : rPhysButtocksPositionX) / 32f, 0, 0);
            matrixStack.translate(0, (side.isLeft ? lPhysButtocksPositionY : rPhysButtocksPositionY) / 32f, 0);
        }

        matrixStack.translate((side.isLeft ? buttocksOffsetX : -buttocksOffsetX) * 0.0625f, 0.05625f + (buttocksOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (buttocksOffsetZ * 0.0625f)); //shift down to correct position

        if(!buttocks.isUnibutt()) {
            matrixStack.translate(-0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }
        if(bounceEnabled) {
            matrixStack.multiply(new Quaternionf().rotationXYZ(0, (float)((side.isLeft ? lPhysButtocksBounceRotation : rPhysButtocksBounceRotation) * (Math.PI / 180f)), 0));
        }
        if(!buttocks.isUnibutt()) {
            matrixStack.translate(0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }

        float rotation = buttocksSize;
        if(bounceEnabled) {
            matrixStack.translate(0, -0.035f * buttocksSize, 0); //shift down to correct position
            rotation -= (side.isLeft ? lPhysButtocksPositionY : rPhysButtocksPositionY) / 12f;
        }

        rotation = Math.min(rotation, buttocksSize + 0.2f);
        rotation = Math.min(rotation, 1); //hard limit for MAX

        if(isChestplateOccupied) {
            matrixStack.translate(0, 0, 0.01f);
        }

        Quaternionf rotationTransform = new Quaternionf()
                .rotationY((side.isLeft ? outwardAngle : -outwardAngle) * DEG_TO_RAD)
                .rotateX(-35f * rotation * DEG_TO_RAD);

        if(breathingAnimation) {
            float f5 = -MathHelper.cos(state.age * 0.09F) * 0.45F + 0.45F;
            rotationTransform.rotateX(f5 * DEG_TO_RAD);
        }

        matrixStack.multiply(rotationTransform);
        matrixStack.scale(0.9995f, 1f, 1f); //z-fighting FIXXX
    }

	protected static void renderBox(WildfireModelRenderer.ModelBox model, MatrixStack matrixStack, VertexConsumer vertexConsumer,
									int light, int overlay, int color) {
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
		Matrix3f matrix3f = matrixStack.peek().getNormalMatrix();
		for(WildfireModelRenderer.TexturedQuad quad : model.quads) {
			Vector3f vector3f = new Vector3f(quad.normal.x, quad.normal.y, quad.normal.z).mul(matrix3f);
			float normalX = vector3f.x;
			float normalY = vector3f.y;
			float normalZ = vector3f.z;
			for(PositionTextureVertex vertex : quad.vertexPositions) {
				float j = vertex.x() / 16.0F;
				float k = vertex.y() / 16.0F;
				float l = vertex.z() / 16.0F;
				Vector4f vector4f = new Vector4f(j, k, l, 1.0F).mul(matrix4f);
				vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), color, vertex.u(), vertex.v(),
						overlay, light, normalX, normalY, normalZ);
			}
		}
	}
}
