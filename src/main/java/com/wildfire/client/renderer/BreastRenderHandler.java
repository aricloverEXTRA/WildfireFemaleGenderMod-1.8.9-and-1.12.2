package com.wildfire.client.renderer;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.wildfire.config.ConfigSettings;
import com.wildfire.client.model.BreastsModel;

public class BreastRenderHandler {

    private final BreastsModel breastsModel = new BreastsModel();
    private float partialTicks;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        partialTicks = event.renderTickTime;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderLivingEvent.Post event) {
        if (ConfigSettings.breastsEnabled && event.entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) event.entity;
            if (player.isEntityAlive()) {
                float limbSwing = player.limbSwing;
                float limbSwingAmount = player.limbSwingAmount;
                float ageInTicks = player.ticksExisted + partialTicks;
                float netHeadYaw = player.rotationYawHead - player.prevRotationYawHead;
                float headPitch = player.rotationPitch;
                float scale = 0.0625F;

                breastsModel.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }
}
