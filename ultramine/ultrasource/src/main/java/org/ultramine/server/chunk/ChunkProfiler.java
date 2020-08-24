package org.ultramine.server.chunk;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ChunkProfiler
{
	private static final ChunkProfiler INSTANCE = new ChunkProfiler();
	
	private boolean isChunkDebugEnabled = false;
	private TLongObjectMap<ChunkData> chunkTimeMap = new TLongObjectHashMap<ChunkData>(512);
	
	public static ChunkProfiler instance()
	{
		return INSTANCE;
	}
	
	private void endChunk(long key, long startTime)
	{
		if(isChunkDebugEnabled)
		{
			ChunkData data = chunkTimeMap.get(key);
			if(data == null)
			{
				data = new ChunkData(key);
				chunkTimeMap.put(key, data);
			}
			
			data.current += (System.nanoTime() - startTime);
		}
	}
	
	public void tick(int tick)
	{
		if(chunkTimeMap.size() != 0)
		{
			if(tick % 600 == 0)
				chunkTimeMap.clear();
			else
				chunkTimeMap.forEachValue(updateFunc);
		}
	}
	
	public void setEnabled(boolean enabled)
	{
		this.isChunkDebugEnabled = enabled;
		chunkTimeMap.clear();
	}
	
	public boolean isEnabled()
	{
		return isChunkDebugEnabled;
	}
	
	public ChunkData[] getAverageTop()
	{
		ChunkData[] arr = chunkTimeMap.values(new ChunkData[chunkTimeMap.size()]);
		Arrays.sort(arr, averageComparator);
		ArrayUtils.reverse(arr);
		return arr;
	}
	
	public ChunkData[] getPeakTop()
	{
		ChunkData[] arr = chunkTimeMap.values(new ChunkData[chunkTimeMap.size()]);
		Arrays.sort(arr, peakComparator);
		ArrayUtils.reverse(arr);
		return arr;
	}
	
	public WorldChunkProfiler getForWorld(int dim)
	{
		return new WorldChunkProfiler(dim);
	}
	
	public static class ChunkData
	{
		private final long key;
		private long current;
		private long average;
		private long peak;
		
		ChunkData(long key)
		{
			this.key = key;
		}
		
		public int getDimension()
		{
			return (int)(key >> 32);
		}
		
		public int getChunkX()
		{
			return ChunkHash.keyToX((int)(key & 0xFFFFFFFF));
		}
		
		public int getChunkZ()
		{
			return ChunkHash.keyToZ((int)(key & 0xFFFFFFFF));
		}
		
		public long getCurrent()
		{
			return current;
		}
		
		public long getAverage()
		{
			return average;
		}
		
		public long getPeak()
		{
			return peak;
		}
		
		public String toString()
		{
			return "["+getDimension()+"]("+getChunkX()+", "+getChunkZ()+") -> all:"+current+" average:"+average+" peak:"+peak+";";
		}
	}
	
	private static final TObjectProcedure<ChunkData> updateFunc = new TObjectProcedure<ChunkData>()
	{
		@Override
		public boolean execute(ChunkData data)
		{
			data.average = (long)(data.average*0.95 + data.current*0.05);
			if(data.current > data.peak)
				data.peak = data.current;
			data.current = 0;
			return true;
		}
	};
	
	private static final Comparator<ChunkData> averageComparator = new Comparator<ChunkData>()
	{
		@Override
		public int compare(ChunkData c1, ChunkData c2)
		{
			return Long.compare(c1.average, c2.average);
		}
	};
	
	private static final Comparator<ChunkData> peakComparator = new Comparator<ChunkData>()
	{
		@Override
		public int compare(ChunkData c1, ChunkData c2)
		{
			return Long.compare(c1.peak, c2.peak);
		}
	};
	
	public class WorldChunkProfiler
	{
		private final int dim;
		
		private long curChunk;
		private long curChunkStart;
		
		private WorldChunkProfiler(int dim)
		{
			this.dim = dim;
		}
		
		public void startChunk(Entity entity)
		{
			if(isChunkDebugEnabled)
				startChunk(MathHelper.floor_double(entity.posX) >> 4, MathHelper.floor_double(entity.posZ) >> 4);
		}
		
		public void startChunk(int cx, int cz)
		{
			if(isChunkDebugEnabled)
			{
				curChunk = ChunkHash.worldChunkToKey(dim, cx, cz);
				curChunkStart = System.nanoTime();
			}
		}
		
		public void startChunk(int key)
		{
			if(isChunkDebugEnabled)
			{
				curChunk = (long)dim << 32 | key & 0xFFFFFFFFL;
				curChunkStart = System.nanoTime();
			}
		}
		
		public void endChunk()
		{
			ChunkProfiler.this.endChunk(curChunk, curChunkStart);
		}
	}
}
