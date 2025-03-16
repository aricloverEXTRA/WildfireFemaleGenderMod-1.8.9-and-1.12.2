package com.wildfire.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.WildfireModelRenderer.OverlayModelBox;
import com.wildfire.render.WildfireModelRenderer.PositionTextureVertex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import com.wildfire.main.WildfireGender;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;

import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.init.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GenderLayer<ENTITY extends LivingEntity, MODEL extends HumanoidModel<ENTITY>> extends RenderLayer<ENTITY, MODEL> {

    private static final OverlayModelBox lBreastWear = new OverlayModelBox(true, 64, 64, 17, 34, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, false);
    private static final OverlayModelBox rBreastWear = new OverlayModelBox(false, 64, 64, 21, 34, 0, 0.0F, 0F, 4, 5, 3, 0.0F, false);
    private static final BreastModelBox lBoobArmor = new BreastModelBox(64, 32, 16, 17, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, false);
    private static final BreastModelBox rBoobArmor = new BreastModelBox(64, 32, 20, 17, 0, 0.0F, 0F, 4, 5, 3, 0.0F, false);

    private final TextureAtlas armorTrimAtlas;

    private BreastModelBox lBreast, rBreast;
    private float preBreastSize, preBreastOffsetZ;

    public GenderLayer(RenderLayerParent<ENTITY, MODEL> renderer, ModelManager modelManager) {
        super(renderer);

        armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
        lBreast = new BreastModelBox(64, 64, 16, 17, -4F, 0.0F, 0F, 4, 5, 4, 0.0F, false);
        rBreast = new BreastModelBox(64, 64, 20, 17, 0, 0.0F, 0F, 4, 5, 4, 0.0F, false);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferSource, int light, @NotNull ENTITY entity, float limbAngle,
            float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
        if (GeneralClientConfig.INSTANCE.disableRendering.get() || entity.isSpectator()) {
            return;
        }
        try {
            EntityConfig entityConfig = EntityConfig.getEntity(entity);
            if(entityConfig == null) return;

            ItemStack armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
            IGenderArmor genderArmor = WildfireHelper.getArmorConfig(armorStack);
            boolean isChestplateOccupied = genderArmor.coversBreasts();
            if (genderArmor.alwaysHidesBreasts() || !entityConfig.showBreastsInArmor() && isChestplateOccupied) {
                return;
            }

            RenderType breastRenderType = null;
            ResourceLocation entityTexture = getBreastTexture(entity);
            if (entityTexture != null) {
                boolean bodyVisible = !entity.isInvisible();
                Minecraft minecraft = Minecraft.getInstance();
                boolean translucent = !bodyVisible && minecraft.player != null && !entity.isInvisibleTo(minecraft.player);
                if (translucent) {
                    breastRenderType = RenderType.itemEntityTranslucentCull(entityTexture);
                } else if (bodyVisible) {
                    breastRenderType = RenderType.entityTranslucent(entityTexture);
                } else if (minecraft.shouldEntityAppearGlowing(entity)) {
                    breastRenderType = RenderType.outline(entityTexture);
                }
            } else if (!isChestplateOccupied) {
                return;
            }

            Breasts breasts = entityConfig.getBreasts();
            float breastOffsetX = Math.round((Math.round(breasts.getXOffset() * 100f) / 100f) * 10) / 10f;
            float breastOffsetY = -Math.round((Math.round(breasts.getYOffset() * 100f) / 100f) * 10) / 10f;
            float breastOffsetZ = -Math.round((Math.round(breasts.getZOffset() * 100f) / 100f) * 10) / 10f;

            BreastPhysics leftBreastPhysics = entityConfig.getLeftBreastPhysics();
            final float bSize = leftBreastPhysics.getBreastSize(partialTicks);
            float outwardAngle = (Math.round(breasts.getCleavage() * 100f) / 100f) * 100f;
            outwardAngle = Math.min(outwardAngle, 10);

            resizeBox(bSize, breastOffsetZ);

            float overlayAlpha = entity.isInvisible() ? 0.15F : 1;

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            float lPhysPositionY = Mth.lerp(partialTicks, leftBreastPhysics.getPrePositionY(), leftBreastPhysics.getPositionY());
            float lPhysPositionX = Mth.lerp(partialTicks, leftBreastPhysics.getPrePositionX(), leftBreastPhysics.getPositionX());
            float leftBounceRotation = Mth.lerp(partialTicks, leftBreastPhysics.getPreBounceRotation(), leftBreastPhysics.getBounceRotation());
            float rPhysPositionY;
            float rPhysPositionX;
            float rightBounceRotation;
            if (breasts.isUniboob()) {
                rPhysPositionY = lPhysPositionY;
                rPhysPositionX = lPhysPositionX;
                rightBounceRotation = leftBounceRotation;
            } else {
                BreastPhysics rightBreastPhysics = entityConfig.getRightBreastPhysics();
                rPhysPositionY = Mth.lerp(partialTicks, rightBreastPhysics.getPrePositionY(), rightBreastPhysics.getPositionY());
                rPhysPositionX = Mth.lerp(partialTicks, rightBreastPhysics.getPrePositionX(), rightBreastPhysics.getPositionX());
                rightBounceRotation = Mth.lerp(partialTicks, rightBreastPhysics.getPreBounceRotation(), rightBreastPhysics.getBounceRotation());
            }

            float breastSize = bSize * 1.5f;
            if (breastSize > 0.7f) breastSize = 0.7f;
            if (bSize > 0.7f) {
                breastSize = bSize;
            }

            if (breastSize < 0.02f) return;

            float zOff = 0.0625f - (bSize * 0.0625f);
            breastSize = bSize + 0.5f * Math.abs(bSize - 0.7f) * 2f;

            float resistance = entityConfig.getArmorPhysicsOverride() ? 0 : Mth.clamp(genderArmor.physicsResistance(), 0, 1);
            boolean breathingAnimation = entityConfig.canBreathe() && resistance <= 0.5F &&
                                         (!entity.isUnderWater() || MobEffectUtil.hasWaterBreathing(entity) ||
                                          entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ())).is(Blocks.BUBBLE_COLUMN));

            boolean bounceEnabled = entityConfig.hasBreastPhysics() && (!isChestplateOccupied || resistance < 1);
            int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0);
            HumanoidModel<ENTITY> model = getParentModel();
            boolean hasJacketLayer;
			if (entity instanceof Player) {
				Player player = (Player) entity;
				hasJacketLayer = player.isModelPartShown(PlayerModelPart.JACKET);
			} else {
				hasJacketLayer = entityConfig.hasJacketLayer();
			}

            renderBreastWithTransforms(entity, model, armorStack, matrixStack, bufferSource, breastRenderType, light, overlay, overlayAlpha, bounceEnabled,
                lPhysPositionX, lPhysPositionY, leftBounceRotation, breastSize, breastOffsetX, breastOffsetY, breastOffsetZ, zOff, outwardAngle, breasts.isUniboob(),
                isChestplateOccupied, breathingAnimation, true, hasJacketLayer);

            renderBreastWithTransforms(entity, model, armorStack, matrixStack, bufferSource, breastRenderType, light, overlay, overlayAlpha, bounceEnabled,
                rPhysPositionX, rPhysPositionY, rightBounceRotation, breastSize, -breastOffsetX, breastOffsetY, breastOffsetZ, zOff, -outwardAngle, breasts.isUniboob(),
                isChestplateOccupied, breathingAnimation, false, hasJacketLayer);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to render gender layer", e);
        }
    }

    protected void resizeBox(float breastSize, float breastOffsetZ) {
        float reducer = -1;
        if (breastSize < 0.84f) reducer++;
        if (breastSize < 0.77f) reducer++;
        if (breastSize < 0.64f) reducer++;
        if (breastSize < 0.35f) reducer++;

        if (reducer > 1) {
            breastOffsetZ = breastOffsetZ * 0.2f;
        } else {
            breastOffsetZ *= 1.5f;
        }
    }

    private void renderBreastWithTransforms(ENTITY entity, HumanoidModel<ENTITY> model, ItemStack armorStack,
            PoseStack matrixStack, MultiBufferSource bufferSource, RenderType renderType, int light,
            int overlay, float overlayAlpha, boolean bounceEnabled, float positionX, float positionY,
            float bounceRotation, float breastSize, float offsetX, float offsetY, float offsetZ, float zOffset,
            float outwardAngle, boolean isUniboob, boolean isChestplateOccupied, boolean breathingAnimation, boolean leftBreast,
            boolean hasJacketLayer) {
        matrixStack.pushPose();
        model.head().translateAndRotate(matrixStack);
        matrixStack.translate(positionX, positionY, offsetZ);
        matrixStack.scale(breastSize, breastSize, breastSize);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(bounceRotation));
        RenderSystem.setShaderTexture(0, armorTrimAtlas);
        lBreast.render(matrixStack, bufferSource.getBuffer(renderType), light, overlay);
        matrixStack.popPose();
    }

    private ResourceLocation getBreastTexture(ENTITY entity) {
        AbstractClientPlayer player = (AbstractClientPlayer) entity;
        return WildfireHelper.getPlayerTexture(player);
    }
}
