package com.wildfire.main;

import com.wildfire.main.config.GenderConfig;
import com.wildfire.main.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

@Mod(modid = WildfireGenderMod.MODID, version = WildfireGenderMod.VERSION)
public class WildfireGenderMod {
    public static final String MODID = "femalegendermodlegacyforge";
    public static final String VERSION = "5.0";

    @SidedProxy(clientSide = "com.wildfire.main.proxy.ClientProxy", serverSide = "com.wildfire.main.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static KeyBinding openGuiKey;
    public static KeyBinding toggleBreastsKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GenderConfig.loadConfig(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        openGuiKey = new KeyBinding("key.wildfire_gender.gender_menu", Keyboard.KEY_G, "Female Gender Mod");
        toggleBreastsKey = new KeyBinding("key.wildfire_gender.toggle", 0, "Female Gender Mod");

        proxy.init(event);
    }
}