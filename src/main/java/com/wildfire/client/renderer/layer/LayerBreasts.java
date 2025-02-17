package com.wildfire.client.renderer.layer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import com.wildfire.client.model.BreastsModel;
import com.wildfire.config.ConfigSettings;

public class LayerBreasts implements LayerRenderer<AbstractClientPlayer> {

    private final BreastsModel breastsModel = new BreastsModel();

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (ConfigSettings.breastsEnabled) {
            GlStateManager.pushMatrix();
            breastsModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
