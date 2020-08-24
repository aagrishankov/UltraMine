package org.ultramine.server.chunk;

import java.util.Collection;

import net.minecraft.world.chunk.Chunk;
import net.openhft.koloboke.collect.map.IntObjCursor;
import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import org.ultramine.server.chunk.ChunkHash;

public class ChunkMap
{
	private final IntObjMap<Chunk> map = HashIntObjMaps.newMutableMap();
	
	public void put(int x, int z, Chunk chunk)
	{
		put(ChunkHash.chunkToKey(x, z), chunk);
	}
	
	public void put(int key, Chunk chunk)
	{
		map.put(key, chunk);
	}
	
	public Chunk get(int x, int z)
	{
		return map.get(ChunkHash.chunkToKey(x, z));
	}
	
	public Chunk get(int hash)
	{
		return map.get(hash);
	}
	
	public Chunk remove(int x, int z)
	{
		return map.remove(ChunkHash.chunkToKey(x, z));
	}
	
	public Chunk remove(int hash)
	{
		return map.remove(hash);
	}
	
	public boolean contains(int x, int z)
	{
		return map.containsKey(ChunkHash.chunkToKey(x, z));
	}
	
	public boolean contains(int hash)
	{
		return map.containsKey(hash);
	}
	
	public IntObjCursor<Chunk> iterator()
	{
		return map.cursor();
	}
	
	public Collection<Chunk> valueCollection()
	{
		return map.values();
	}
	
	public int size()
	{
		return map.size();
	}
	
	public void clear()
	{
		map.clear();
	}
}
