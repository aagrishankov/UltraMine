package org.ultramine.server.world;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderEmpty implements IChunkProvider
{
	private World world;
	
	public ChunkProviderEmpty(World world)
	{
		this.world = world;
	}
	
	@Override
	public boolean chunkExists(int var1, int var2)
	{
		return true;
	}

	@Override
	public Chunk provideChunk(int par1, int par2)
	{
		Chunk chunk = new Chunk(this.world, par1, par2);
		
		byte[] arr = chunk.getBiomeArray();

		for (int i = 0; i < arr.length; ++i)
		{
			arr[i] = (byte)BiomeGenBase.forest.biomeID;
		}
		
		chunk.generateSkylightMap();
		
		return chunk;
	}

	@Override
	public Chunk loadChunk(int var1, int var2)
	{
		return this.provideChunk(var1, var2);
	}

	@Override
	public void populate(IChunkProvider var1, int x, int z)
	{
		int bx = x << 4;
		int bz = z << 4;
		
		if((x > -2 && x < 2) && (z > -2 && z < 2))
		{
			for(int i = 0; i < 16; i++)
			{
				for(int j = 0; j < 16; j++)
				{
					world.setBlock(bx + i, 64, bz + j, Blocks.grass);
				}
			}
		}
	}

	@Override
	public boolean saveChunks(boolean var1, IProgressUpdate var2)
	{
		return true;
	}

	@Override
	public boolean unloadQueuedChunks()
	{
		return false;
	}

	@Override
	public boolean canSave()
	{
		return true;
	}

	@Override
	public String makeString()
	{
		return "EmptyWorldSource";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getPossibleCreatures(EnumCreatureType var1, int var2, int var3, int var4)
	{
		return null;
	}

	@Override
	public ChunkPosition func_147416_a(World var1, String var2, int var3, int var4, int var5)
	{
		return null;
	}

	@Override
	public int getLoadedChunkCount()
	{
		return 0;
	}

	@Override
	public void recreateStructures(int var1, int var2)
	{
		
	}

	@Override
	public void saveExtraData()
	{
		
	}

}
