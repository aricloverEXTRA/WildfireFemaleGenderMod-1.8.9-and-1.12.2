package com.wildfire.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import com.wildfire.model.BreastsModel;
import com.wildfire.config.ConfigSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class BreastRenderHandler {

    private final BreastsModel breastsModel = new BreastsModel();

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;

        if (ConfigSettings.breastsEnabled && player == Minecraft.getMinecraft().thePlayer) {
            System.out.println("BreastRenderHandler: onPlayerRender event triggered.");

            GlStateManager.pushMatrix();

            // Translate to the player's render position
            GlStateManager.translate(event.x, event.y, event.z);

            // Rotate to match the player's rotation
            float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.partialRenderTick;
            GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);

            // Bind the player's skin texture
            ResourceLocation skin = ((AbstractClientPlayer) player).getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);

            // Render the breasts model
            breastsModel.render(player, event.partialRenderTick);

            GlStateManager.popMatrix();
        }
    }
}
