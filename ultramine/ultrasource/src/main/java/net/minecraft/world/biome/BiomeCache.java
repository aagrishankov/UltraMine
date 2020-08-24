package net.minecraft.world.biome;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LongHashMap;

public class BiomeCache
{
	private final WorldChunkManager chunkManager;
	private long lastCleanupTime;
	private LongHashMap cacheMap = new LongHashMap();
	private List cache = new ArrayList();
	private static final String __OBFID = "CL_00000162";

	public BiomeCache(WorldChunkManager p_i1973_1_)
	{
		this.chunkManager = p_i1973_1_;
	}

	public BiomeCache.Block getBiomeCacheBlock(int p_76840_1_, int p_76840_2_)
	{
		p_76840_1_ >>= 4;
		p_76840_2_ >>= 4;
		long k = (long)p_76840_1_ & 4294967295L | ((long)p_76840_2_ & 4294967295L) << 32;
		BiomeCache.Block block = (BiomeCache.Block)this.cacheMap.getValueByKey(k);

		if (block == null)
		{
			block = new BiomeCache.Block(p_76840_1_, p_76840_2_);
			this.cacheMap.add(k, block);
			this.cache.add(block);
		}

		block.lastAccessTime = MinecraftServer.getSystemTimeMillis();
		return block;
	}

	public BiomeGenBase getBiomeGenAt(int p_76837_1_, int p_76837_2_)
	{
		return this.getBiomeCacheBlock(p_76837_1_, p_76837_2_).getBiomeGenAt(p_76837_1_, p_76837_2_);
	}

	public void cleanupCache()
	{
		long i = MinecraftServer.getSystemTimeMillis();
		long j = i - this.lastCleanupTime;

		if (j > 7500L || j < 0L)
		{
			this.lastCleanupTime = i;

			for (int k = 0; k < this.cache.size(); ++k)
			{
				BiomeCache.Block block = (BiomeCache.Block)this.cache.get(k);
				long l = i - block.lastAccessTime;

				if (l > 30000L || l < 0L)
				{
					this.cache.remove(k--);
					long i1 = (long)block.xPosition & 4294967295L | ((long)block.zPosition & 4294967295L) << 32;
					this.cacheMap.remove(i1);
				}
			}
		}
	}

	public BiomeGenBase[] getCachedBiomes(int p_76839_1_, int p_76839_2_)
	{
		return this.getBiomeCacheBlock(p_76839_1_, p_76839_2_).biomes;
	}

	public class Block
	{
		public float[] rainfallValues = new float[256];
		public BiomeGenBase[] biomes = new BiomeGenBase[256];
		public int xPosition;
		public int zPosition;
		public long lastAccessTime;
		private static final String __OBFID = "CL_00000163";

		public Block(int p_i1972_2_, int p_i1972_3_)
		{
			this.xPosition = p_i1972_2_;
			this.zPosition = p_i1972_3_;
			BiomeCache.this.chunkManager.getRainfall(this.rainfallValues, p_i1972_2_ << 4, p_i1972_3_ << 4, 16, 16);
			BiomeCache.this.chunkManager.getBiomeGenAt(this.biomes, p_i1972_2_ << 4, p_i1972_3_ << 4, 16, 16, false);
		}

		public BiomeGenBase getBiomeGenAt(int p_76885_1_, int p_76885_2_)
		{
			return this.biomes[p_76885_1_ & 15 | (p_76885_2_ & 15) << 4];
		}
	}
}