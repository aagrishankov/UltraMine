package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryLargeChest implements IInventory
{
	private String name;
	private IInventory upperChest;
	private IInventory lowerChest;
	private static final String __OBFID = "CL_00001507";

	public InventoryLargeChest(String p_i1559_1_, IInventory p_i1559_2_, IInventory p_i1559_3_)
	{
		this.name = p_i1559_1_;

		if (p_i1559_2_ == null)
		{
			p_i1559_2_ = p_i1559_3_;
		}

		if (p_i1559_3_ == null)
		{
			p_i1559_3_ = p_i1559_2_;
		}

		this.upperChest = p_i1559_2_;
		this.lowerChest = p_i1559_3_;
	}

	public int getSizeInventory()
	{
		return this.upperChest.getSizeInventory() + this.lowerChest.getSizeInventory();
	}

	public boolean isPartOfLargeChest(IInventory p_90010_1_)
	{
		return this.upperChest == p_90010_1_ || this.lowerChest == p_90010_1_;
	}

	public String getInventoryName()
	{
		return this.upperChest.hasCustomInventoryName() ? this.upperChest.getInventoryName() : (this.lowerChest.hasCustomInventoryName() ? this.lowerChest.getInventoryName() : this.name);
	}

	public boolean hasCustomInventoryName()
	{
		return this.upperChest.hasCustomInventoryName() || this.lowerChest.hasCustomInventoryName();
	}

	public ItemStack getStackInSlot(int p_70301_1_)
	{
		return p_70301_1_ >= this.upperChest.getSizeInventory() ? this.lowerChest.getStackInSlot(p_70301_1_ - this.upperChest.getSizeInventory()) : this.upperChest.getStackInSlot(p_70301_1_);
	}

	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		return p_70298_1_ >= this.upperChest.getSizeInventory() ? this.lowerChest.decrStackSize(p_70298_1_ - this.upperChest.getSizeInventory(), p_70298_2_) : this.upperChest.decrStackSize(p_70298_1_, p_70298_2_);
	}

	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		return p_70304_1_ >= this.upperChest.getSizeInventory() ? this.lowerChest.getStackInSlotOnClosing(p_70304_1_ - this.upperChest.getSizeInventory()) : this.upperChest.getStackInSlotOnClosing(p_70304_1_);
	}

	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		if (p_70299_1_ >= this.upperChest.getSizeInventory())
		{
			this.lowerChest.setInventorySlotContents(p_70299_1_ - this.upperChest.getSizeInventory(), p_70299_2_);
		}
		else
		{
			this.upperChest.setInventorySlotContents(p_70299_1_, p_70299_2_);
		}
	}

	public int getInventoryStackLimit()
	{
		return this.upperChest.getInventoryStackLimit();
	}

	public void markDirty()
	{
		this.upperChest.markDirty();
		this.lowerChest.markDirty();
	}

	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return this.upperChest.isUseableByPlayer(p_70300_1_) && this.lowerChest.isUseableByPlayer(p_70300_1_);
	}

	public void openInventory()
	{
		this.upperChest.openInventory();
		this.lowerChest.openInventory();
	}

	public void closeInventory()
	{
		this.upperChest.closeInventory();
		this.lowerChest.closeInventory();
	}

	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
	{
		return true;
	}
}