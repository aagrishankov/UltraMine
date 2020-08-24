package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryBasic implements IInventory
{
	private String inventoryTitle;
	private int slotsCount;
	private ItemStack[] inventoryContents;
	private List field_70480_d;
	private boolean field_94051_e;
	private static final String __OBFID = "CL_00001514";

	public InventoryBasic(String p_i1561_1_, boolean p_i1561_2_, int p_i1561_3_)
	{
		this.inventoryTitle = p_i1561_1_;
		this.field_94051_e = p_i1561_2_;
		this.slotsCount = p_i1561_3_;
		this.inventoryContents = new ItemStack[p_i1561_3_];
	}

	public void func_110134_a(IInvBasic p_110134_1_)
	{
		if (this.field_70480_d == null)
		{
			this.field_70480_d = new ArrayList();
		}

		this.field_70480_d.add(p_110134_1_);
	}

	public void func_110132_b(IInvBasic p_110132_1_)
	{
		this.field_70480_d.remove(p_110132_1_);
	}

	public ItemStack getStackInSlot(int p_70301_1_)
	{
		return p_70301_1_ >= 0 && p_70301_1_ < this.inventoryContents.length ? this.inventoryContents[p_70301_1_] : null;
	}

	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		if (this.inventoryContents[p_70298_1_] != null)
		{
			ItemStack itemstack;

			if (this.inventoryContents[p_70298_1_].stackSize <= p_70298_2_)
			{
				itemstack = this.inventoryContents[p_70298_1_];
				this.inventoryContents[p_70298_1_] = null;
				this.markDirty();
				return itemstack;
			}
			else
			{
				itemstack = this.inventoryContents[p_70298_1_].splitStack(p_70298_2_);

				if (this.inventoryContents[p_70298_1_].stackSize == 0)
				{
					this.inventoryContents[p_70298_1_] = null;
				}

				this.markDirty();
				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		if (this.inventoryContents[p_70304_1_] != null)
		{
			ItemStack itemstack = this.inventoryContents[p_70304_1_];
			this.inventoryContents[p_70304_1_] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		this.inventoryContents[p_70299_1_] = p_70299_2_;

		if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit())
		{
			p_70299_2_.stackSize = this.getInventoryStackLimit();
		}

		this.markDirty();
	}

	public int getSizeInventory()
	{
		return this.slotsCount;
	}

	public String getInventoryName()
	{
		return this.inventoryTitle;
	}

	public boolean hasCustomInventoryName()
	{
		return this.field_94051_e;
	}

	public void func_110133_a(String p_110133_1_)
	{
		this.field_94051_e = true;
		this.inventoryTitle = p_110133_1_;
	}

	public int getInventoryStackLimit()
	{
		return 64;
	}

	public void markDirty()
	{
		if (this.field_70480_d != null)
		{
			for (int i = 0; i < this.field_70480_d.size(); ++i)
			{
				((IInvBasic)this.field_70480_d.get(i)).onInventoryChanged(this);
			}
		}
	}

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
}