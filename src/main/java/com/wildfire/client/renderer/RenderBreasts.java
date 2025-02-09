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

        System.out.println("Pre-render event for player: " + player.getName());

        if (shouldRenderBreasts(player)) {
            System.out.println("Rendering breasts for player: " + player.getName());
            renderBreasts(player, event.partialRenderTick);
        } else {
            System.out.println("Breasts will not render for player: " + player.getName());
        }
    }

    private boolean shouldRenderBreasts(EntityPlayer player) {
        String gender = ConfigSettings.gender;
        System.out.println("Checking gender for player: " + player.getName() + ", Gender: " + gender);
        return "Female".equals(gender) || "Other".equals(gender);
    }

    private void renderBreasts(EntityLivingBase entity, float partialTicks) {
        float limbSwing = entity.limbSwing;
        float limbSwingAmount = entity.limbSwingAmount;
        float ageInTicks = entity.ticksExisted + partialTicks;
        float netHeadYaw = entity.rotationYawHead - entity.prevRotationYawHead;
        float headPitch = entity.rotationPitch;
        float scale = 0.0625F;

        System.out.println("Rendering breasts model with parameters: limbSwing=" + limbSwing + ", limbSwingAmount=" + limbSwingAmount
                + ", ageInTicks=" + ageInTicks + ", netHeadYaw=" + netHeadYaw + ", headPitch=" + headPitch + ", scale=" + scale);
        breastsModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
