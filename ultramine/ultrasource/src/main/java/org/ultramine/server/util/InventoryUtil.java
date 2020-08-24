package org.ultramine.server.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class InventoryUtil
{
	public static boolean isStacksEquals(ItemStack is1, ItemStack is2)
	{
		return
				is1 == null && is2 == null || is1 != null && is2 != null && is1.isItemEqual(is2) &&
				(!(is1.stackTagCompound == null && is2.stackTagCompound != null) && (is1.stackTagCompound == null || is1.stackTagCompound.equals(is2.stackTagCompound)));
	}
	
	public static boolean contains(IInventory inv, ItemStack item)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack is = inv.getStackInSlot(i);
			if(isStacksEquals(is, item))
				return true;
		}

		return false;
	}

	public static int first(IInventory inv, ItemStack item)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack is = inv.getStackInSlot(i);
			if(isStacksEquals(is, item))
				return i;
		}

		return -1;
	}

	public static int firstEmpty(IInventory inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(inv.getStackInSlot(i) == null)
				return i;
		}

		return -1;
	}

	public static int firstPartial(IInventory inv, ItemStack filteredItem)
	{
		if(filteredItem == null)
			return -1;
		
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack cItem = inv.getStackInSlot(i);
			if(cItem != null && cItem.stackSize < cItem.getMaxStackSize() && isStacksEquals(cItem, filteredItem))
				return i;
		}
		
		return -1;
	}

	public static List<ItemStack> removeItem(IInventory inv, ItemStack... items)
	{
		List<ItemStack> ret = null;
		for(ItemStack item : items)
		{
			ItemStack leftover = removeItem(inv, item);
			if(leftover != null)
			{
				if(ret == null)
					ret = new LinkedList<ItemStack>();
				ret.add(leftover);
			}
		}
		return ret == null ? Collections.<ItemStack>emptyList() : ret;
	}
	
	public static ItemStack removeItem(IInventory inv, ItemStack is)
	{
		int toDelete = is.stackSize;

		for(int i = 0, s = inv.getSizeInventory(); i < s; i++)
		{
			ItemStack it = inv.getStackInSlot(i);
			if(it != null && isStacksEquals(it, is))
			{
				int amount = it.stackSize;

				if(amount <= toDelete)
				{
					toDelete -= amount;
					inv.setInventorySlotContents(i, null);
					if(toDelete == 0)
						break;
				}
				else
				{
					it.stackSize = amount - toDelete;
					inv.setInventorySlotContents(i, it);
					toDelete = 0;
					break;
				}
			}
		}
		
		is.stackSize = toDelete;
		if(toDelete == 0)
			is = null;
		return is;
	}
	
	public static List<ItemStack> addItem(IInventory inv, ItemStack... items)
	{
		List<ItemStack> ret = null;
		for(ItemStack item : items)
		{
			ItemStack leftover = addItem(inv, item);
			if(leftover != null)
			{
				if(ret == null)
					ret = new LinkedList<ItemStack>();
				ret.add(leftover);
			}
		}
		return ret == null ? Collections.<ItemStack>emptyList() : ret;
	}
	
	public static void addItem(EntityPlayer player, ItemStack item)
	{
		ItemStack ret = addItem(player.inventory, item);
		if(ret != null)
			dropItem(player.worldObj, player.posX, player.posY, player.posZ, item);
	}

	public static ItemStack addItem(IInventory inv, ItemStack is)
	{
		return addItem(inv, is, true, true);
	}
	
	public static ItemStack addItem(IInventory inv, ItemStack is, boolean checkValid, boolean markDirty)
	{
		if(is == null)
			return null;
		int oldCount = is.stackSize;
		int maxSize = Math.min(inv.getInventoryStackLimit(), is.getMaxStackSize());
		if(maxSize > 1)
		{
			for(int i = 0, s = inv.getSizeInventory(); i < s; i++)
			{
				ItemStack it = inv.getStackInSlot(i);
				if(it != null && it.stackSize < maxSize && isStacksEquals(is, it) && (!checkValid || inv.isItemValidForSlot(i, is)))
				{
					int free = maxSize - it.stackSize;
					if(is.stackSize > free)
					{
						is.stackSize -= free;
						it.stackSize = maxSize;
					}
					else
					{
						it.stackSize = it.stackSize + is.stackSize;
						is.stackSize = 0;
						is = null;
						break;
					}
				}
			}
		}
		
		if(is != null)
		{
			for(int i = 0, s = inv.getSizeInventory(); i < s; i++)
			{
				if(inv.getStackInSlot(i) == null && (!checkValid || inv.isItemValidForSlot(i, is)))
				{
					if(is.stackSize <= maxSize)
					{
						inv.setInventorySlotContents(i, is.copy());
						is.stackSize = 0;
						is = null;
						break;
					}
					else
					{
						ItemStack to = is.copy();
						to.stackSize = maxSize;
						is.stackSize -= maxSize;
						inv.setInventorySlotContents(i, to);
					}
				}
			}
		}
		
		if(markDirty && (is == null || is.stackSize != oldCount))
			inv.markDirty();
		return is;
	}

	public static void dropItem(World world, int x, int y, int z, ItemStack is)
	{
		if(is == null)
			return;
		double rx = world.rand.nextDouble() * 0.8D + 0.1D;
		double ry = world.rand.nextDouble() * 0.8D + 0.1D;
		double rz = world.rand.nextDouble() * 0.8D + 0.1D;
		dropItem(world, x + rx, y + ry, z + rz, is);
	}

	public static void dropItem(World world, double x, double y, double z, ItemStack is)
	{
		if(is == null)
			return;
		while(is.stackSize > is.getMaxStackSize())
			dropItem(world, x, y, z, is.splitStack(is.getMaxStackSize()));
		EntityItem entity = new EntityItem(world, x, y, z, is.copy());
		entity.motionX = world.rand.nextGaussian() * 0.05D;
		entity.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
		entity.motionZ = world.rand.nextGaussian() * 0.05D;
		world.spawnEntityInWorld(entity);
	}

	public static void dropItemFixed(World world, int x, int y, int z, ItemStack is)
	{
		if(is == null)
			return;
		while(is.stackSize > is.getMaxStackSize())
			dropItemFixed(world, x, y, z, is.splitStack(is.getMaxStackSize()));
		double rx = world.rand.nextDouble() * 0.8D + 0.1D;
		double ry = world.rand.nextDouble() * 0.8D + 0.1D;
		double rz = world.rand.nextDouble() * 0.8D + 0.1D;
		world.spawnEntityInWorld(new EntityItem(world, x + rx, y + ry, z + rz, is.copy()));
	}

	public static void dropItemFixed(World world, double x, double y, double z, ItemStack is)
	{
		if(is == null)
			return;
		while(is.stackSize > is.getMaxStackSize())
			dropItemFixed(world, x, y, z, is.splitStack(is.getMaxStackSize()));
		world.spawnEntityInWorld(new EntityItem(world, x, y, z, is.copy()));
	}

	public static int countItems(IInventory inv, ItemStack is)
	{
		int ret = 0;
		for(int i = 0, s = inv.getSizeInventory(); i < s; i++)
		{
			ItemStack it = inv.getStackInSlot(i);
			if(it != null && isStacksEquals(it, is))
				ret += it.stackSize;
		}
		return ret;
	}
	
	public static boolean containsAmount(IInventory inv, ItemStack is, int amount)
	{
		int counted = 0;
		for(int i = 0, s = inv.getSizeInventory(); i < s; i++)
		{
			ItemStack it = inv.getStackInSlot(i);
			if(it != null && isStacksEquals(it, is))
			{
				counted += it.stackSize;
				if(counted >= amount)
					return true;
			}
		}
		
		return false;
	}
	
	public static void readInventoryFromNBT(ItemStack[] inv, NBTTagList list)
	{
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			int slot = nbt.getByte("Slot") & 0xff;

			if(slot >= 0 && slot < inv.length)
				inv[slot] = ItemStack.loadItemStackFromNBT(nbt);
		}
	}

	public static NBTTagList writeInventorytoNBT(ItemStack[] inv)
	{
		NBTTagList list = new NBTTagList();
		for(int i = 0; i < inv.length; i++)
		{
			if(inv[i] != null)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("Slot", (byte)i);
				inv[i].writeToNBT(nbt);
				list.appendTag(nbt);
			}
		}
		return list;
	}
}
