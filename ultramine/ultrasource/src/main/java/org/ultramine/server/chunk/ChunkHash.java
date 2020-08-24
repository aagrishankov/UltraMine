package org.ultramine.server.chunk;

public class ChunkHash
{
	public static int chunkToKey(int x, int z)
	{
		return (x & 0xffff) << 16 | (z & 0xffff);
	}
	
	public static int keyToX(int k)
	{
		return (short)((k >> 16) & 0xffff);
	}
	public static int keyToZ(int k)
	{
		return (short)(k & 0xffff);
	}
	
	public static short chunkCoordToHash(int x, int y, int z)
	{
		return (short)((y << 8) | (z << 4) | x);
	}
	
	public static long worldChunkToKey(int dim, int x, int z)
	{
		return (long)dim << 32 | (long)(x & 0xffff) << 16 | (z & 0xffff);
	}
	
	public static long blockCoordToHash(int x, int y, int z)
	{
		return (long)(x & 0xffffff) | ((long)(y & 0xff) << 24) | ((long)(z & 0xffffff) << 32);
	}
	
	public static int blockKeyToX(long key)
	{
		int x = (int)(key & 0xffffff);
		if((x & 8388608) != 0)
			x |= 0xff << 24;
		return x;
	}
	
	public static int blockKeyToZ(long key)
	{
		int z = (int)((key >> 32) & 0xffffff);
		if((z & 8388608) != 0)
			z |= 0xff << 24;
		return z;
	}
	
	public static int blockKeyToY(long key)
	{
		return (int)((key >> 24) & 0xff);
	}
}
