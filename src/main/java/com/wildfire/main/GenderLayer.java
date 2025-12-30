package com.wildfire.main;

import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVQuad;
import com.wildfire.main.uvs.UVStorage;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer renderPlayer;

    private final ModelRenderer leftBreastFront, rightBreastFront;
    private final ModelRenderer leftBreastWearFront, rightBreastWearFront;
    private final ModelRenderer leftBreastWearBottom, rightBreastWearBottom;

    private static final float MAX_PROTRUSION = 1.6f;
    private static final float YAW_FACTOR = 0.34f;

    private static final ConcurrentHashMap<UUID, BreastPhysics[]> PHYSICS_MAP = new ConcurrentHashMap<>();

    public GenderLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
        ModelBiped main = (ModelBiped) renderPlayer.getMainModel();

        leftBreastFront = new ModelRenderer(main, 20, 20);
        leftBreastFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4);
        leftBreastFront.setTextureSize(64, 64);

        rightBreastFront = new ModelRenderer(main, 24, 20);
        rightBreastFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4);
        rightBreastFront.setTextureSize(64, 64);

        leftBreastWearFront = new ModelRenderer(main, 16, 32);
        leftBreastWearFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, 0.25F);
        leftBreastWearFront.setTextureSize(64, 64);

        rightBreastWearFront = new ModelRenderer(main, 20, 32);
        rightBreastWearFront.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, 0.25F);
        rightBreastWearFront.setTextureSize(64, 64);

        leftBreastWearBottom = new ModelRenderer(main, 20, 36);
        leftBreastWearBottom.addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, 0.30F);
        leftBreastWearBottom.setTextureSize(64, 64);

        rightBreastWearBottom = new ModelRenderer(main, 32, 36);
        rightBreastWearBottom.addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, 0.30F);
        rightBreastWearBottom.setTextureSize(64, 64);
    }

    public static void ensureRegisteredForPlayer(AbstractClientPlayer player) {
        if (player == null) return;
        PHYSICS_MAP.computeIfAbsent(player.getUniqueID(),
                id -> new BreastPhysics[]{new BreastPhysics(), new BreastPhysics()});
    }

    public static BreastPhysics[] getPhysicsForPlayer(AbstractClientPlayer player) {
        if (player == null) return null;
        return PHYSICS_MAP.computeIfAbsent(player.getUniqueID(),
                id -> new BreastPhysics[]{new BreastPhysics(), new BreastPhysics()});
    }

    public static void unregister(UUID playerId) {
        PHYSICS_MAP.remove(playerId);
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float headYaw, float headPitch, float scale) {

        if (!shouldRenderBreasts(player)) return;

        boolean isFake = player instanceof FakeGUIPlayer.FakeEntityPlayer;

        GenderConfig.PlayerGenderSettings cfg =
                isFake ? GenderConfig.getStaticFakeCreditsSettings()
                       : GenderConfig.getPlayerSettings((EntityPlayer) player);

        if (cfg == null) return;
        if (!cfg.breastsEnabled) return;
        if (cfg.breastSize <= 0) return;

        Breasts breasts = new Breasts((EntityPlayer) player);

        float sizeFactor = MathHelper.clamp_float(cfg.breastSize / 100.0f, 0.0f, 1.0f);
        float zScale = 0.1f + (1.0f - 0.1f) * sizeFactor;
        float torsoPush = (1.0f - sizeFactor) * 1.6f;

        float separation = cfg.separation / 30.0F
                - cfg.breastsCleavage / 60.0F
                + 0.0625F + breasts.getCleavage() + 0.125F + 0.625F;

        float depth = -cfg.depth / 10.0F;
        float height = -(cfg.height / 40.0F);
        float rotRad = (float) Math.toRadians(cfg.rotation);

        float leftBaseYawOffset = rotRad;
        float rightBaseYawOffset = -rotRad;

        float baseX = breasts.getXOffset() - 0.350F;
        float baseY = 3.5F + height;
        float baseZ = breasts.getZOffset() + depth - 1.5F + MAX_PROTRUSION * sizeFactor + torsoPush;

        float lPosX, lPosY, lBounce;
        float rPosX, rPosY, rBounce;

        if (isFake) {
            // STATIC breasts for fake players
            lPosX = 0.0F;
            lPosY = 0.0F;
            lBounce = 0.0F;

            if (cfg.breastsUniboob) {
                rPosX = lPosX;
                rPosY = lPosY;
                rBounce = -lBounce;
            } else {
                rPosX = 0.0F;
                rPosY = 0.0F;
                rBounce = 0.0F;
            }
        } else {
            // REAL PLAYER: physics
            ensureRegisteredForPlayer(player);
            BreastPhysics[] phys = getPhysicsForPlayer(player);
            if (phys == null) return;

            lPosX = interp(phys[0].getPrePositionX(), phys[0].getPositionX(), partialTicks);
            lPosY = interp(phys[0].getPrePositionY(), phys[0].getPositionY(), partialTicks);
            lBounce = interp(phys[0].getPreBounceRotation(), phys[0].getBounceRotation(), partialTicks);

            if (cfg.breastsUniboob) {
                rPosX = lPosX;
                rPosY = lPosY;
                rBounce = -lBounce;
            } else {
                rPosX = interp(phys[1].getPrePositionX(), phys[1].getPositionX(), partialTicks);
                rPosY = interp(phys[1].getPrePositionY(), phys[1].getPositionY(), partialTicks);
                rBounce = -interp(phys[1].getPreBounceRotation(), phys[1].getBounceRotation(), partialTicks);
            }
        }

        ModelBiped model = (ModelBiped) renderPlayer.getMainModel();
        ModelRenderer torso = model.bipedBody;
        float renderScale = 0.0625F;

        UUID id = player.getUniqueID();
        ResourceLocation baseTex = UVStorage.getBreastTexture(id, false);
        ResourceLocation overlayTex = UVStorage.getBreastTexture(id, true);

        if (baseTex != null) renderPlayer.bindTexture(baseTex);
        else renderPlayer.bindTexture(player.getLocationSkin());

        GlStateManager.pushMatrix();
        torso.postRender(renderScale);

        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.24F, 0.0F);
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        // MAIN BREASTS
        renderBreast(leftBreastFront, baseX - separation, baseY + lPosY, baseZ,
                lBounce, -lPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

        renderBreast(rightBreastFront, baseX + separation, baseY + rPosY, baseZ,
                rBounce,  rPosX * YAW_FACTOR, 1.0f, 1.0f, zScale, renderScale);

        // OVERLAY UVs
        UVLayout leftOverlay = null, rightOverlay = null;
        try {
            EntityConfig entityCfg = EntityConfig.getEntity(player);
            if (entityCfg != null) {
                leftOverlay = entityCfg.getLeftBreastOverlayUVLayout();
                rightOverlay = entityCfg.getRightBreastOverlayUVLayout();
            }
        } catch (Throwable ignored) {}

        ModelRenderer leftOverlayBox = createOverlayBoxFromUV(model, leftOverlay, 16, 32);
        ModelRenderer rightOverlayBox = createOverlayBoxFromUV(model, rightOverlay, 20, 32);

        renderBreast(leftOverlayBox, baseX - separation, baseY + lPosY, baseZ,
                lBounce, -lPosX * YAW_FACTOR + leftBaseYawOffset, 1.0f, 1.0f, zScale, renderScale);

        renderBreast(rightOverlayBox, baseX + separation, baseY + rPosY, baseZ,
                rBounce,  rPosX * YAW_FACTOR + rightBaseYawOffset, 1.0f, 1.0f, zScale, renderScale);

        // BOTTOM OVERLAY
        ModelRenderer leftBottom = createBottomBoxFromUV(model, leftOverlay, 20, 34);
        ModelRenderer rightBottom = createBottomBoxFromUV(model, rightOverlay, 24, 34);

        renderBreast(leftBottom, baseX - separation, baseY + lPosY + 0.5F, baseZ,
                lBounce + (float)Math.PI + (float)Math.toRadians(4.0),
                -lPosX * YAW_FACTOR + leftBaseYawOffset, 1.0f, 1.0f, zScale, renderScale);

        renderBreast(rightBottom, baseX + separation, baseY + rPosY + 0.5F, baseZ,
                rBounce + (float)Math.PI + (float)Math.toRadians(4.0),
                 rPosX * YAW_FACTOR + rightBaseYawOffset, 1.0f, 1.0f, zScale, renderScale);

        if (baseTex != null) renderPlayer.bindTexture(baseTex);
        else renderPlayer.bindTexture(player.getLocationSkin());

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private ModelRenderer createOverlayBoxFromUV(ModelBiped mainModel, UVLayout layout, int defaultU, int defaultV) {
        int texU = defaultU;
        int texV = defaultV;
        if (layout != null) {
            try {
                UVQuad north = layout.get(UVDirection.NORTH);
                if (north != null) {
                    texU = north.x1();
                    texV = north.y1();
                }
            } catch (Throwable ignored) {}
        }
        ModelRenderer box = new ModelRenderer(mainModel, texU, texV);
        box.addBox(-2.0F, -2.5F, -2.0F, 4, 5, 4, 0.25F);
        box.setTextureSize(64, 64);
        return box;
    }

    private ModelRenderer createBottomBoxFromUV(ModelBiped mainModel, UVLayout layout, int defaultU, int defaultV) {
        int texU = defaultU;
        int texV = defaultV;
        if (layout != null) {
            try {
                UVQuad down = layout.get(UVDirection.DOWN);
                if (down != null) {
                    texU = down.x1();
                    texV = down.y1();
                }
            } catch (Throwable ignored) {}
        }
        ModelRenderer box = new ModelRenderer(mainModel, texU, texV);
        box.addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, 0.30F);
        box.setTextureSize(64, 64);
        return box;
    }

    private void renderBreast(ModelRenderer model, float x, float y, float z,
                              float bounceRot, float yaw,
                              float scaleX, float scaleY, float scaleZ, float renderScale) {
        GlStateManager.pushMatrix();

        model.setRotationPoint(x, y, z);
        GlStateManager.translate(model.rotationPointX * renderScale,
                model.rotationPointY * renderScale,
                model.rotationPointZ * renderScale);

        GlStateManager.scale(scaleX, scaleY, scaleZ);

        GlStateManager.translate(-model.rotationPointX * renderScale,
                -model.rotationPointY * renderScale,
                -model.rotationPointZ * renderScale);

        model.rotateAngleX = (float) Math.toRadians(-26.92285) + bounceRot - 0.1f;
        model.rotateAngleY = yaw;
        model.render(renderScale);

        GlStateManager.popMatrix();
    }

    private boolean shouldRenderBreasts(AbstractClientPlayer player) {
        if (!ClientConfig.RENDER_BREASTS) return false;

        // Fake GUI players
        if (player instanceof FakeGUIPlayer.FakeEntityPlayer) {
            if (!ClientConfig.CREDITS_RENDER_BREASTS) return false;

            GenderConfig.PlayerGenderSettings s = GenderConfig.getStaticFakeCreditsSettings();
            return s.breastsEnabled && ("Female".equals(s.gender) || "Other".equals(s.gender));
        }

        // Real player
        GenderConfig.PlayerGenderSettings s = GenderConfig.getPlayerSettings((EntityPlayer) player);
        if (s == null) return false;

        if (s.hideInArmor) {
            try {
                ItemStack chest = ((EntityPlayer) player).inventory.armorInventory[2];
                if (chest != null && chest.getItem() instanceof ItemArmor) {
                    return false;
                }
            } catch (Throwable ignored) {}
        }

        return s.breastsEnabled && ("Female".equals(s.gender) || "Other".equals(s.gender));
    }

    private static float interp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}