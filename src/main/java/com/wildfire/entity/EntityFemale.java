package com.wildfire.entity;

import com.wildfire.client.model.BreastsModel;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityFemale extends Entity {
    private final BreastsModel breastsModel;

    public EntityFemale(World world) {
        super(world);
        this.breastsModel = new BreastsModel();
    }

    @Override
    protected void entityInit() {
        // Initialize entity data
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        // Read data from NBT when loading
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        // Write data to NBT when saving
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.breastsModel.setLeftBreastVelocity((float) (this.motionX * 0.5));
        this.breastsModel.setRightBreastVelocity((float) (this.motionX * 0.5));
    }
}
