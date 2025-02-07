package com.wildfire.layer;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import com.wildfire.config.ConfigSettings;
import com.wildfire.model.BreastsModel;
import net.minecraft.client.Minecraft;

public class LayerBreasts implements LayerRenderer<EntityLivingBase> {

    private final BreastsModel breastsModel = new BreastsModel();

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (entitylivingbaseIn instanceof AbstractClientPlayer && ConfigSettings.breastsEnabled) {
            AbstractClientPlayer player = (AbstractClientPlayer) entitylivingbaseIn;

            GlStateManager.pushMatrix();

            // Bind the player's skin texture
            ResourceLocation skin = player.getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

            // Position and rotate the breasts to match the player model
            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            // Get the player's renderer
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            RenderPlayer renderer = renderManager.getSkinMap().get(player.getSkinType());

            // Align the model attributes
            ModelBiped playerModel = renderer.getMainModel();
            breastsModel.setModelAttributes(playerModel);

            // Render the breasts model
            breastsModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
