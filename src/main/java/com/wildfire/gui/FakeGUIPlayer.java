package com.wildfire.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

@SideOnly(Side.CLIENT)
public class FakeGUIPlayer {

    private final String name;
    private final UUID uuid;
    private EntityOtherPlayerMP entity;

    public static float STATIC_FAKE_BREAST_SIZE = 100.0F;

    public static final int LEFT_X1 = 20, LEFT_Y1 = 20, LEFT_X2 = 23, LEFT_Y2 = 23;
    public static final int RIGHT_X1 = 24, RIGHT_Y1 = 20, RIGHT_X2 = 27, RIGHT_Y2 = 23;

    public FakeGUIPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid != null ? uuid : UUID.nameUUIDFromBytes(("fake:" + name).getBytes());
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public float getPreviewBreastSize() {
        return STATIC_FAKE_BREAST_SIZE;
    }

    public EntityOtherPlayerMP getEntity() {
        if (entity == null) {
            Minecraft mc = Minecraft.getMinecraft();
            World world = mc.theWorld != null ? mc.theWorld : mc.thePlayer.worldObj;

            String dummyName = "uuid_" + uuid.toString().replace("-", "").substring(0, 12);
            GameProfile profile = new GameProfile(this.uuid, dummyName);

            ResourceLocation localSkin = new ResourceLocation(
                    "wildfire_gender",
                    "textures/credit_skins/" + uuid.toString() + ".png"
            );

            entity = new EntityCreditsPlayer(world, profile, localSkin);

            entity.getEntityData().setBoolean("WFG_FakeGUIPlayer", true);

            entity.rotationYawHead = 0.0F;
            entity.rotationYaw = 0.0F;
            entity.rotationPitch = 0.0F;
        }
        return entity;
    }

    public void tick() {
        if (entity != null) {
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.ticksExisted++;
            try {
                com.wildfire.main.entitydata.EntityConfig cfg = com.wildfire.main.entitydata.EntityConfig.getEntity(entity);
                if (cfg != null) cfg.tickBreastPhysics(entity);
            } catch (Throwable ignored) {}
        }
    }
}
