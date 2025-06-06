package com.wildfire.main.proxy;

import com.wildfire.main.WildfireGenderMod;
import com.wildfire.main.config.GenderConfig;
import com.wildfire.gui.screen.GuiWardrobe;
import com.wildfire.gui.screen.GuiFirstTime;
import com.wildfire.render.GenderLayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    private boolean eventsRegistered = false;

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(WildfireGenderMod.openGuiKey);
        if (!eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new GenderLayer());
            eventsRegistered = true;
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WildfireGenderMod.openGuiKey.isPressed()) {
            GenderConfig.PlayerGenderSettings settings = GenderConfig.getPlayerSettings(Minecraft.getMinecraft().thePlayer);
            if (settings != null && settings.showFirstTimeGui) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiFirstTime());
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiWardrobe());
            }
        }
    }
}