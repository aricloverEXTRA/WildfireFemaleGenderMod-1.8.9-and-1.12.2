package com.wildfire.render;

import com.wildfire.main.BreastsModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GenderLayer implements LayerRenderer<AbstractClientPlayer> {

    private final BreastsModel breastsModel = new BreastsModel();

    public GenderLayer() {
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) {
            return;
        }

        this.breastsModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
        this.breastsModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.entity;
        float partialTicks = event.partialRenderTick;
        doRenderLayer(player, player.limbSwing, player.limbSwingAmount, partialTicks, player.ticksExisted + partialTicks, player.rotationYawHead, player.rotationPitch, 0.0625F);
    }
}
