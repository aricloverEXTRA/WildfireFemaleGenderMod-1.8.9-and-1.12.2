package com.wildfire.proxy;

import com.wildfire.WildfireGenderMod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.event.BreastRenderHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);
        MinecraftForge.EVENT_BUS.register(this);
        // Register the BreastRenderHandler
        MinecraftForge.EVENT_BUS.register(new BreastRenderHandler());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WildfireGenderMod.openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
        }
    }
}
