package com.wildfire.main.proxy;

import com.wildfire.main.WildfireGenderMod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.render.GenderLayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GenderLayer());
        System.out.println("Registering GenderLayer event handler.");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WildfireGenderMod.openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
        }
    }
}
