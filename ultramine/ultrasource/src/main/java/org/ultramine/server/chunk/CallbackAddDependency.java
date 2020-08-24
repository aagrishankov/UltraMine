package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public class CallbackAddDependency implements IChunkLoadCallback
{
	private final IChunkDependency dependency;
	
	public CallbackAddDependency(IChunkDependency dependency)
	{
		this.dependency = dependency;
	}
	
	@Override
	public void onChunkLoaded(Chunk chunk)
	{
		chunk.addDependency(dependency);
	}
	
	@Override
	public int hashCode()
	{
		return dependency.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof CallbackAddDependency && ((CallbackAddDependency)o).dependency.equals(dependency);
	}
}
