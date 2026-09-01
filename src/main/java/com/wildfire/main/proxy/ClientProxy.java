package com.wildfire.main.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.main.WildfireGenderMod;
import com.wildfire.main.WildfireSounds;
import com.wildfire.main.GenderLayer;
import com.wildfire.main.WildfireEventHandler;
import com.wildfire.main.handlers.ArmorTooltipHandler;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);
        ClientRegistry.registerKeyBinding(WildfireGenderMod.toggleBreastsKey);

        try {
            RenderPlayer renderDefault = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
            if (renderDefault != null) renderDefault.addLayer(new GenderLayer(renderDefault));
            RenderPlayer renderSlim = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim");
            if (renderSlim != null) renderSlim.addLayer(new GenderLayer(renderSlim));
        } catch (Throwable t) {
            System.err.println("[WFG] Failed to register GenderLayer: " + t.getMessage());
        }

        MinecraftForge.EVENT_BUS.register(new Object() {
            @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
            public void onKeyInput(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
                if (WildfireGenderMod.openGuiKey.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
                }
                if (WildfireGenderMod.toggleBreastsKey.isPressed()) {
                    com.wildfire.main.config.ClientConfig.RENDER_BREASTS = !com.wildfire.main.config.ClientConfig.RENDER_BREASTS;
                    System.out.println("[WFG] ToggleBreastsKey pressed -> RENDER_BREASTS = " + com.wildfire.main.config.ClientConfig.RENDER_BREASTS);
                }
            }
        });

        MinecraftForge.EVENT_BUS.register(new WildfireEventHandler());
        MinecraftForge.EVENT_BUS.register(new WildfireSounds());
        MinecraftForge.EVENT_BUS.register(new ArmorTooltipHandler());
    }
}