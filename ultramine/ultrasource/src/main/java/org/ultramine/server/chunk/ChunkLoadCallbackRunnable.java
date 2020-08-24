package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public class ChunkLoadCallbackRunnable implements IChunkLoadCallback
{
	private final Runnable run;
	
	public ChunkLoadCallbackRunnable(Runnable run)
	{
		this.run = run;
	}

	@Override
	public void onChunkLoaded(Chunk chunk)
	{
		run.run();
	}
	
	public int hashCode()
	{
		return run.hashCode();
	}
	
	public boolean equals(Object o)
	{
		return o instanceof ChunkLoadCallbackRunnable && ((ChunkLoadCallbackRunnable)o).run == run;
	}
}
