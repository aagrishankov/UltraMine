package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public interface IChunkDependency
{
	boolean isDependent(Chunk chunk);
}
