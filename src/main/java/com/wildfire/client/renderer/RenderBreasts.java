package com.wildfire.client.renderer;

import com.wildfire.client.model.BreastsModel;
import com.wildfire.config.ConfigSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderBreasts {
    private final BreastsModel breastsModel = new BreastsModel();

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.entityPlayer;

        if (shouldRenderBreasts(player)) {
            renderBreasts(player, event.partialRenderTick);
        }
    }

    private boolean shouldRenderBreasts(EntityPlayer player) {
        String gender = ConfigSettings.gender;
        return "Female".equals(gender) || "Other".equals(gender);
    }

    private void renderBreasts(EntityLivingBase entity, float partialTicks) {
        float limbSwing = entity.limbSwing;
        float limbSwingAmount = entity.limbSwingAmount;
        float ageInTicks = entity.ticksExisted + partialTicks;
        float netHeadYaw = entity.rotationYawHead - entity.prevRotationYawHead;
        float headPitch = entity.rotationPitch;
        float scale = 0.0625F;

        breastsModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
