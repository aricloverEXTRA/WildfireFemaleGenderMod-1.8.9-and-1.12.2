package com.wildfire.main.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.main.WildfireGenderMod;
import com.wildfire.main.GenderLayer;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);

        RenderPlayer renderDefault = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
        renderDefault.addLayer(new GenderLayer(renderDefault));
        RenderPlayer renderSlim = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim");
        renderSlim.addLayer(new GenderLayer(renderSlim));

        // Register event handler for key input
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onKeyInput(InputEvent.KeyInputEvent event) {
                if (WildfireGenderMod.openGuiKey.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
                }
            }
        });
    }
}