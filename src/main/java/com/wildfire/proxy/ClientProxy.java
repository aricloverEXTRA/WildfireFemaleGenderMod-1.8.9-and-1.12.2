package com.wildfire.proxy;

import com.wildfire.WildfireGenderMod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import com.wildfire.client.gui.screen.GuiWardrobe;
import com.wildfire.client.renderer.RenderBreasts;
import com.wildfire.client.renderer.layers.LayerBreasts;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RenderBreasts());
		System.out.println("Registering RenderBreasts event handler.");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WildfireGenderMod.openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
        }
    }
}
