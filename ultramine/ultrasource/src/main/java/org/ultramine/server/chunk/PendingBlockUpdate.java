package org.ultramine.server.chunk;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;

public class PendingBlockUpdate implements Comparable<PendingBlockUpdate>
{
	private static long nextTickEntryID;
	
	private final Block block;
	public final byte x;
	public final short y;
	public final byte z;
	
	public final long scheduledTime;
	public final int priority;
	
	private final long id;
	private final short hash;

	public GameProfile initiator;
	
	public PendingBlockUpdate(int x, int y, int z, Block block, long time, int priority)
	{
		this.id = nextTickEntryID++;
		this.x = (byte)x;
		this.y = (short)y;
		this.z = (byte)z;
		this.block = block;
		this.scheduledTime = time;
		this.priority = priority;
		
		hash = ChunkHash.chunkCoordToHash(x, y, z);
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof PendingBlockUpdate))
		{
			return false;
		}
		
		PendingBlockUpdate p = (PendingBlockUpdate)o;
		return this.hash == p.hash && Block.isEqualTo(this.block, p.block);
	}

	public short getChunkCoordHash()
	{
		return hash;
	}
	
	public int hashCode()
	{
		return hash;
	}
	
	public int compareTo(PendingBlockUpdate p)
	{
		return scheduledTime < p.scheduledTime ? -1 : (scheduledTime > p.scheduledTime ? 1 : (priority != p.priority ? priority - p.priority : (id < p.id ? -1 : (id > p.id ? 1 : 0))));
	}
	
	public String toString()
	{
		return Block.getIdFromBlock(block) + ": (" + x + ", " + y + ", " + z + "), " + scheduledTime + ", " + priority + ", " + id;
	}
	
	public Block getBlock()
	{
		return block;
	}
}
