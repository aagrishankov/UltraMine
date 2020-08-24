package org.ultramine.server.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ultramine.server.event.ForgeModIdMappingEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;
import org.ultramine.server.internal.UMInternalRegistry;

public class ItemStackHashMap<V> implements Map<ItemStack, V>, UMInternalRegistry.IRemapHandler
{
	private final Map<ItemStack, V> map = new TreeMap<ItemStack, V>(ItemStackComparator.INSTANCE);
	private final IntObjMap<V> fastMap = HashIntObjMaps.newMutableMap();
	private boolean hasWildcard = false;
	
	public ItemStackHashMap()
	{
		this(true);
	}
	
	public ItemStackHashMap(boolean register)
	{
		if(register)
			UMInternalRegistry.registerRemapHandler(this);
	}
	
	public void remap()
	{
		fastMap.clear();
		for(Map.Entry<ItemStack, V> ent : map.entrySet())
			putToFastMap(ent.getKey(), ent.getValue());
	}
	
	private V putToFastMap(ItemStack key, V value)
	{
		if(key.getItemDamage() == 32767)
			hasWildcard = true;
		return fastMap.put(Item.getIdFromItem(key.getItem()) | (key.getItemDamage() << 16), value);
	}
	
	private V removeFastMap(ItemStack key)
	{
		return fastMap.remove(Item.getIdFromItem(key.getItem()) | (key.getItemDamage() << 16));
	}
	
	public V get(int id, int meta)
	{
		V ret = fastMap.get(id | (meta << 16));
		if(ret == null && hasWildcard)
			ret = fastMap.get(id | (32767 << 16));
		return ret;
	}
	
	public V get(ItemStack key)
	{
		return get(Item.getIdFromItem(key.getItem()), key.getItemDamage());
	}
	
	@Override
	public V put(ItemStack key, V value)
	{
		map.put(key, value);
		return putToFastMap(key, value);
	}
	
	
	
	public V get(Item item, int meta)
	{
		return get(Item.getIdFromItem(item), meta);
	}
	
	public V get(Block block, int meta)
	{
		return get(Block.getIdFromBlock(block), meta);
	}
	
	public V put(Item item, int meta, V value)
	{
		return put(new ItemStack(item, 1, meta), value);
	}
	
	public V put(Block block, int meta, V value)
	{
		return put(new ItemStack(block, 1, meta), value);
	}
	
	@Override
	public V get(Object key)
	{
		return get((ItemStack)key);
	}
	
	@Override
	public int size()
	{
		return map.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}
	
	@Override
	public boolean containsValue(Object value)
	{
		return fastMap.containsValue(value);
	}
	
	@Override
	public V remove(Object key)
	{
		V val = removeFastMap((ItemStack)key);
		if(val != null)
			map.remove(key);
		return val;
	}
	
	@Override
	public void putAll(Map<? extends ItemStack, ? extends V> m)
	{
		map.putAll(m);
		for(Map.Entry<? extends ItemStack, ? extends V> ent : m.entrySet())
			putToFastMap(ent.getKey(), ent.getValue());
	}
	
	@Override
	public void clear()
	{
		map.clear();
		fastMap.clear();
	}
	
	@Override
	public Set<ItemStack> keySet()
	{
		return map.keySet();
	}
	
	@Override
	public Collection<V> values()
	{
		return map.values();
	}
	
	@Override
	public Set<java.util.Map.Entry<ItemStack, V>> entrySet()
	{
		return map.entrySet();
	}
}
