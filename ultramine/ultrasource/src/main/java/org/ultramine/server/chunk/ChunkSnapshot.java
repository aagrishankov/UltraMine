package org.ultramine.server.chunk;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

public class ChunkSnapshot
{
	private final int x;
	private final int z;
	private final boolean worldHasNoSky;
	private final ExtendedBlockStorage[] ebsArr;
	private final byte[] biomeArray;

	private ChunkSnapshot(int x, int z, boolean worldHasNoSky, ExtendedBlockStorage[] ebsArr, byte[] biomeArray)
	{
		this.x = x;
		this.z = z;
		this.worldHasNoSky = worldHasNoSky;
		this.ebsArr = ebsArr;
		this.biomeArray = biomeArray;
	}

	public static ChunkSnapshot of(Chunk chunk)
	{
		ExtendedBlockStorage[] ebsOld = chunk.getBlockStorageArray();
		ExtendedBlockStorage[] ebsNew = new ExtendedBlockStorage[ebsOld.length];
		for(int i = 0; i < ebsOld.length; i++)
			ebsNew[i] = ebsOld[i] == null ? null : ebsOld[i].copy();
		byte[] biomeArray = chunk.getBiomeArray();
		return new ChunkSnapshot(chunk.xPosition, chunk.zPosition, chunk.worldObj.provider.hasNoSky, ebsNew, Arrays.copyOf(biomeArray, biomeArray.length));
	}

	public int getX()
	{
		return x;
	}

	public int getZ()
	{
		return z;
	}

	public boolean isWorldHasNoSky()
	{
		return worldHasNoSky;
	}

	public ExtendedBlockStorage[] getEbsArr()
	{
		return ebsArr;
	}

	public byte[] getBiomeArray()
	{
		return biomeArray;
	}

	public void release()
	{
		for(ExtendedBlockStorage ebs : ebsArr)
			if(ebs != null)
				ebs.release();
	}

	public ChunkSnapshot copy()
	{
		ExtendedBlockStorage[] ebsOld = ebsArr;
		ExtendedBlockStorage[] ebsNew = new ExtendedBlockStorage[ebsOld.length];
		for(int i = 0; i < ebsOld.length; i++)
			ebsNew[i] = ebsOld[i] == null ? null : ebsOld[i].copy();
		byte[] biomeArray = this.biomeArray;
		return new ChunkSnapshot(getX(), getZ(), isWorldHasNoSky(), ebsNew, Arrays.copyOf(biomeArray, biomeArray.length));
	}

	private static void rangeCheck(int x, int z)
	{
		if((x & 0xFFFFFFF0) != 0 || (z & 0xFFFFFFF0) != 0)
			throw new IllegalArgumentException();
	}

	public Block getBlock(int x, int y, int z)
	{
		rangeCheck(x, z);
		ExtendedBlockStorage ebs = ebsArr[y >> 4];
		if(ebs != null)
			return ebs.getBlockByExtId(x, y & 15, z);
		return Blocks.air;
	}

	public int getBlockId(int x, int y, int z)
	{
		rangeCheck(x, z);
		ExtendedBlockStorage ebs = ebsArr[y >> 4];
		if(ebs != null)
			return ebs.getSlot().getBlockId(x, y & 15, z);
		return 0;
	}

	public int getBlockMeta(int x, int y, int z)
	{
		rangeCheck(x, z);
		ExtendedBlockStorage ebs = ebsArr[y >> 4];
		if(ebs != null)
			return ebs.getExtBlockMetadata(x, y & 15, z);
		return 0;
	}

	public int getBlockIdAndMeta(int x, int y, int z)
	{
		rangeCheck(x, z);
		ExtendedBlockStorage ebs = ebsArr[y >> 4];
		if(ebs != null)
			return ebs.getSlot().getBlockIdAndMeta(x, y & 15, z);
		return 0;
	}

	public void setBlock(int x, int y, int z, int blockId, int meta)
	{
		rangeCheck(x, z);
		ExtendedBlockStorage ebs = ebsArr[y >> 4];
		if(ebs == null)
			ebs = ebsArr[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, true);
		ebs.getSlot().setBlockIdAndMeta(x, y & 15, z, blockId, meta);
		if(ebs.isEmpty())
			ebs.incBlockRefCount();
	}

	public void setBlock(int x, int y, int z, Block block, int meta)
	{
		setBlock(x, y, z, Block.getIdFromBlock(block), meta);
	}

	public int getTopFilledSegment()
	{
		for (int i = ebsArr.length - 1; i >= 0; --i)
			if(ebsArr[i] != null)
				return ebsArr[i].getYLocation();

		return 0;
	}
}
