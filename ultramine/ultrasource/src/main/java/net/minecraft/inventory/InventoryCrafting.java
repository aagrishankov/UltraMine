package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryCrafting implements IInventory
{
	private ItemStack[] stackList;
	private int inventoryWidth;
	private Container eventHandler;
	private static final String __OBFID = "CL_00001743";

	public InventoryCrafting(Container p_i1807_1_, int p_i1807_2_, int p_i1807_3_)
	{
		int k = p_i1807_2_ * p_i1807_3_;
		this.stackList = new ItemStack[k];
		this.eventHandler = p_i1807_1_;
		this.inventoryWidth = p_i1807_2_;
	}

	public int getSizeInventory()
	{
		return this.stackList.length;
	}

	public ItemStack getStackInSlot(int p_70301_1_)
	{
		return p_70301_1_ >= this.getSizeInventory() ? null : this.stackList[p_70301_1_];
	}

	public ItemStack getStackInRowAndColumn(int p_70463_1_, int p_70463_2_)
	{
		if (p_70463_1_ >= 0 && p_70463_1_ < this.inventoryWidth)
		{
			int k = p_70463_1_ + p_70463_2_ * this.inventoryWidth;
			return this.getStackInSlot(k);
		}
		else
		{
			return null;
		}
	}

	public String getInventoryName()
	{
		return "container.crafting";
	}

	public boolean hasCustomInventoryName()
	{
		return false;
	}

	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		if (this.stackList[p_70304_1_] != null)
		{
			ItemStack itemstack = this.stackList[p_70304_1_];
			this.stackList[p_70304_1_] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		if (this.stackList[p_70298_1_] != null)
		{
			ItemStack itemstack;

			if (this.stackList[p_70298_1_].stackSize <= p_70298_2_)
			{
				itemstack = this.stackList[p_70298_1_];
				this.stackList[p_70298_1_] = null;
				if(callMatrixChanged)
					this.eventHandler.onCraftMatrixChanged(this);
				return itemstack;
			}
			else
			{
				itemstack = this.stackList[p_70298_1_].splitStack(p_70298_2_);

				if (this.stackList[p_70298_1_].stackSize == 0)
				{
					this.stackList[p_70298_1_] = null;
				}

				if(callMatrixChanged)
					this.eventHandler.onCraftMatrixChanged(this);
				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		this.stackList[p_70299_1_] = p_70299_2_;
		if(callMatrixChanged)
			this.eventHandler.onCraftMatrixChanged(this);
	}

	public int getInventoryStackLimit()
	{
		return 64;
	}

	public void markDirty() {}

	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}

	public void openInventory() {}

	public void closeInventory() {}

	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
	{
		return true;
	}
	
	public int getWidth()
	{
		return inventoryWidth;
	}
	
	public static boolean callMatrixChanged = true;
}