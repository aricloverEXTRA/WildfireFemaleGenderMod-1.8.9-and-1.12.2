package com.wildfire.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityFemale extends Entity {
    private final BreastsModel breastsModel;

    public EntityFemale(World world) {
        super(world);
        this.breastsModel = new BreastsModel();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.breastsModel.leftBreastVelocity = this.motionX * 0.5F;
        this.breastsModel.rightBreastVelocity = this.motionX * 0.5F;
    }
}
