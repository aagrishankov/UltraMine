package org.ultramine.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ultramine.server.event.ForgeModIdMappingEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;
import org.ultramine.server.internal.UMInternalRegistry;

public class ItemStackHashSet implements Set<ItemStack>, UMInternalRegistry.IRemapHandler
{
	private final List<ItemStack> list = new ArrayList<ItemStack>();
	private final IntSet fastSet = HashIntSets.newMutableSet();
	private boolean hasWildcard = false;
	
	public ItemStackHashSet()
	{
		this(true);
	}
	
	public ItemStackHashSet(boolean register)
	{
		if(register)
			UMInternalRegistry.registerRemapHandler(this);
	}
	
	public void remap()
	{
		fastSet.clear();
		for(ItemStack is : list)
			addToFastMap(is);
	}
	
	private boolean addToFastMap(ItemStack is)
	{
		if(is.getItemDamage() == 32767)
			hasWildcard = true;
		return fastSet.add(Item.getIdFromItem(is.getItem()) | (is.getItemDamage() << 16));
	}
	
	private boolean removeFastMap(ItemStack is)
	{
		return fastSet.removeInt(Item.getIdFromItem(is.getItem()) | (is.getItemDamage() << 16));
	}
	
	public boolean contains(int id, int meta)
	{
		boolean ret = fastSet.contains(id | (meta << 16));
		if(!ret && hasWildcard)
			ret = fastSet.contains(id | (32767 << 16));
		return ret;
	}
	
	public boolean contains(ItemStack is)
	{
		return contains(Item.getIdFromItem(is.getItem()), is.getItemDamage());
	}
	
	public boolean contains(Block block, int meta)
	{
		return contains(Block.getIdFromBlock(block), meta);
	}
	
	public boolean contains(Item item, int meta)
	{
		return contains(Item.getIdFromItem(item), meta);
	}
	
	@Override
	public boolean add(ItemStack is)
	{
		boolean added = addToFastMap(is);
		if(added)
			list.add(is);
		return added;
	}
	
	public boolean add(Block block, int meta)
	{
		return add(new ItemStack(block, 1, meta));
	}
	
	public boolean add(Item item, int meta)
	{
		return add(new ItemStack(item, 1, meta));
	}
	
	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return contains((ItemStack)o);
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return list.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return list.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		boolean removed = removeFastMap((ItemStack)o);
		if(removed)
			list.remove(o);
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		if(c instanceof ItemStackHashSet)
			return fastSet.containsAll(((ItemStackHashSet)c).fastSet);
		for(Object o : c)
			if(!contains(o))
				return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends ItemStack> c)
	{
		boolean ret = false;
		for(ItemStack is : c)
			ret |= add(is);
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean ret = false;
		for(Object is : c)
			ret |= remove(is);
		return ret;
	}

	@Override
	public void clear()
	{
		list.clear();
		fastSet.clear();
	}
}
