package com.wildfire;

import com.wildfire.config.ConfigSettings;
import com.wildfire.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import com.wildfire.gui.screen.GuiWardrobe;

@Mod(modid = WildfireGenderMod.MODID, version = WildfireGenderMod.VERSION)
public class WildfireGenderMod {
    public static final String MODID = "femalegendermodlegacy";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.wildfire.proxy.ClientProxy", serverSide = "com.wildfire.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static KeyBinding openGuiKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigSettings.loadConfig(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register the keybinding
        openGuiKey = new KeyBinding("Open Breast Customization GUI", Keyboard.KEY_G, "Wildfire Gender Mod");
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Save configuration settings if needed
    }
}