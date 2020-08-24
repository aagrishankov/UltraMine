package org.ultramine.server.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ultramine.server.chunk.ChunkHash;

import com.google.common.collect.Iterators;

import net.minecraft.world.ChunkCoordIntPair;
import net.openhft.koloboke.collect.set.IntSet;

public class VanillaChunkCoordIntPairSet implements Set<ChunkCoordIntPair>
{
	private final IntSet intset;
	
	public VanillaChunkCoordIntPairSet(IntSet intset)
	{
		this.intset = intset;
	}

	private static int v2um(ChunkCoordIntPair coord)
	{
		return ChunkHash.chunkToKey(coord.chunkXPos, coord.chunkZPos);
	}
	
	private static ChunkCoordIntPair um2v(int key)
	{
		return new ChunkCoordIntPair(ChunkHash.keyToX(key), ChunkHash.keyToZ(key));
	}
	
	@Override
	public int size()
	{
		return intset.size();
	}

	@Override
	public boolean isEmpty()
	{
		return intset.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return o instanceof ChunkCoordIntPair && contains((ChunkCoordIntPair)o);
	}
	
	public boolean contains(ChunkCoordIntPair coord)
	{
		return intset.contains(v2um(coord));
	}

	@Override
	@SuppressWarnings("deprecation")
	public Iterator<ChunkCoordIntPair> iterator()
	{
		return Iterators.transform(intset.iterator(), (Integer key) -> um2v(key));
	}

	@Override
	public Object[] toArray()
	{
		return toArray(new ChunkCoordIntPair[size()]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a)
	{
		int size = size();
		ChunkCoordIntPair[] r = a.length >= size() ? (ChunkCoordIntPair[]) a : (ChunkCoordIntPair[]) Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		for(ChunkCoordIntPair coord : this)
			r[i++] = coord;
		return (T[]) r;
	}

	@Override
	public boolean add(ChunkCoordIntPair e)
	{
		return intset.add(v2um(e));
	}

	@Override
	public boolean remove(Object o)
	{
		return o instanceof ChunkCoordIntPair && remove((ChunkCoordIntPair)o);
	}
	
	public boolean remove(ChunkCoordIntPair coord)
	{
		return intset.removeInt(v2um(coord));
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for(Object o : c)
			if(!contains(o))
				return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends ChunkCoordIntPair> c)
	{
		boolean modified = false;
		for(Object o : c)
			modified |= add((ChunkCoordIntPair)o);
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean modified = false;
		for(Iterator<ChunkCoordIntPair> it = iterator(); it.hasNext();)
		{
			ChunkCoordIntPair coord = it.next();
			if(!c.contains(coord))
			{
				it.remove();
				modified = true;
			}
		}
		
		return modified;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean modified = false;
		for(Object o : c)
			modified |= remove(o);
		return modified;
	}

	@Override
	public void clear()
	{
		intset.clear();
	}
}
