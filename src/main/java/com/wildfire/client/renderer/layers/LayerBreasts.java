package com.wildfire.client.renderer.layers;

import com.wildfire.client.model.BreastsModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class LayerBreasts implements LayerRenderer<AbstractClientPlayer> {
    private final BreastsModel breastsModel = new BreastsModel();

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (breastsModel.shouldRenderBreasts(player)) {
            breastsModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}
