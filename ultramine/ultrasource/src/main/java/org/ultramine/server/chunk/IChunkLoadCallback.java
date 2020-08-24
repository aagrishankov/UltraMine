package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public interface IChunkLoadCallback
{
	public static final IChunkLoadCallback EMPTY = new IChunkLoadCallback(){public void onChunkLoaded(Chunk chunk){}};
	
	public void onChunkLoaded(Chunk chunk);
}
