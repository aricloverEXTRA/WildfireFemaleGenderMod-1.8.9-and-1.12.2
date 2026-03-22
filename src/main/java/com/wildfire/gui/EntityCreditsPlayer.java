package com.wildfire.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCreditsPlayer extends EntityOtherPlayerMP {

    private final ResourceLocation forcedSkin;

    public EntityCreditsPlayer(World world, GameProfile profile, ResourceLocation forcedSkin) {
        super(world, profile);
        this.forcedSkin = forcedSkin;
    }

    @Override
    public ResourceLocation getLocationSkin() {
        return forcedSkin != null ? forcedSkin : super.getLocationSkin();
    }

    @Override
    public boolean isWearing(EnumPlayerModelParts part) {
        return true;
    }
}
