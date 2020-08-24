package org.ultramine.server.util;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

public class ItemStackComparator implements Comparator<ItemStack>
{
	public static final ItemStackComparator INSTANCE = new ItemStackComparator();
	
	@Override
	public int compare(ItemStack is1, ItemStack is2)
	{
		int c1 = is1.getItem().delegate.name().compareTo(is2.getItem().delegate.name());
		return c1 != 0 ? c1 : Integer.compare(is1.getItemDamage(), is2.getItemDamage());
	}
}
