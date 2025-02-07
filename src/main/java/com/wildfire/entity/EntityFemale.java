package com.wildfire.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import com.wildfire.model.BreastsModel;

public class EntityFemale extends EntityLivingBase {
    private BreastsModel breastsModel;
    private ItemStack[] inventory;

    public EntityFemale(World worldIn) {
        super(worldIn);
        this.breastsModel = new BreastsModel();
        this.inventory = new ItemStack[5]; // Example inventory size
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.breastsModel.setLeftBreastVelocity((float) (this.motionX * 0.5));
        this.breastsModel.setRightBreastVelocity((float) (this.motionX * 0.5));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);
        // Load inventory data
        for (int i = 0; i < this.inventory.length; i++) {
            this.inventory[i] = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("Inventory" + i));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        // Save inventory data
        for (int i = 0; i < this.inventory.length; i++) {
            NBTTagCompound itemTag = new NBTTagCompound();
            if (this.inventory[i] != null) {
                this.inventory[i].writeToNBT(itemTag);
            }
            tagCompound.setTag("Inventory" + i, itemTag);
        }
    }

    @Override
    public ItemStack[] getInventory() {
        return this.inventory;
    }

    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
        if (slot >= 0 && slot < this.inventory.length) {
            this.inventory[slot] = itemStack;
        }
    }

    @Override
    public ItemStack getCurrentArmor(int slot) {
        if (slot >= 0 && slot < this.inventory.length) {
            return this.inventory[slot];
        }
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int slot) {
        if (slot >= 0 && slot < this.inventory.length) {
            return this.inventory[slot];
        }
        return null;
    }

    @Override
    public ItemStack getHeldItem() {
        return this.inventory[0]; // Example: first slot is the held item
    }
}
