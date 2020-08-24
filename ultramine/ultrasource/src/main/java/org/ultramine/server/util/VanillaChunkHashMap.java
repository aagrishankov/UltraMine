package org.ultramine.server.util;

import org.ultramine.server.chunk.ChunkMap;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.chunk.Chunk;

public class VanillaChunkHashMap extends LongHashMap
{
	private final ChunkMap chunkMap;
	
	public VanillaChunkHashMap(ChunkMap chunkMap)
	{
		super(null);
		this.chunkMap = chunkMap;
	}
	
	private static int v2x(long key)
	{
		return (int) (key & 0xFFFFFFFFL);
	}
	
	private static int v2z(long key)
	{
		return (int) (key >>> 32);
	}

	@Override
	public int getNumHashElements()
	{
		return chunkMap.size();
	}

	@Override
	public Object getValueByKey(long key)
	{
		return chunkMap.get(v2x(key), v2z(key));
	}

	@Override
	public boolean containsItem(long key)
	{
		return chunkMap.contains(v2x(key), v2z(key));
	}

	@Override
	public void add(long key, Object obj)
	{
		chunkMap.put(v2x(key), v2z(key), (Chunk)obj);
	}

	@Override
	public Object remove(long key)
	{
		return chunkMap.remove(v2x(key), v2z(key));
	}
}
