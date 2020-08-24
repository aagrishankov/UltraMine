package net.minecraft.world;

import org.ultramine.server.chunk.ChunkHash;

public class ChunkCoordIntPair
{
	public final int chunkXPos;
	public final int chunkZPos;
	private static final String __OBFID = "CL_00000133";

	public ChunkCoordIntPair(int p_i1947_1_, int p_i1947_2_)
	{
		this.chunkXPos = p_i1947_1_;
		this.chunkZPos = p_i1947_2_;
	}

	public static long chunkXZ2Int(int p_77272_0_, int p_77272_1_)
	{
		return (long)p_77272_0_ & 4294967295L | ((long)p_77272_1_ & 4294967295L) << 32;
	}

	public int hashCode()
	{
		return ChunkHash.chunkToKey(chunkXPos, chunkZPos);
	}

	public boolean equals(Object p_equals_1_)
	{
		if (this == p_equals_1_)
		{
			return true;
		}
		else if (!(p_equals_1_ instanceof ChunkCoordIntPair))
		{
			return false;
		}
		else
		{
			ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)p_equals_1_;
			return this.chunkXPos == chunkcoordintpair.chunkXPos && this.chunkZPos == chunkcoordintpair.chunkZPos;
		}
	}

	public int getCenterXPos()
	{
		return (this.chunkXPos << 4) + 8;
	}

	public int getCenterZPosition()
	{
		return (this.chunkZPos << 4) + 8;
	}

	public ChunkPosition func_151349_a(int p_151349_1_)
	{
		return new ChunkPosition(this.getCenterXPos(), p_151349_1_, this.getCenterZPosition());
	}

	public String toString()
	{
		return "[" + this.chunkXPos + ", " + this.chunkZPos + "]";
	}
}