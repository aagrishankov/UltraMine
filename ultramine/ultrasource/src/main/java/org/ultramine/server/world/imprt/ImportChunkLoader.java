package org.ultramine.server.world.imprt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

public abstract class ImportChunkLoader extends AnvilChunkLoader
{
	private static final Logger log = LogManager.getLogger();
	protected final IntObjMap<RegionFile> regionCache = HashIntObjMaps.newUpdatableMap();
	protected final File tempDir;
	protected volatile boolean closed;
	
	protected ImportChunkLoader(File tempDir)
	{
		super(tempDir);
		this.tempDir = tempDir;
	}
	
	protected abstract void unpackFile(String name) throws IOException;
	
	private String getRegionFileName(int cx, int cz)
	{
		return "region/r."+(cx >> 5) + "." + (cz >> 5) + ".mca";
	}
	
	private synchronized RegionFile getRegion(int cx, int cz)
	{
		int x = cx >> 5;
		int z = cz >> 5;
		int key = (x & 0xffff) | ((z & 0xffff) << 11);
		RegionFile region = regionCache.get(key);
		if(region == null)
		{
			clearCache(128);
			String name = getRegionFileName(cx, cz);
			File regFile = new File(tempDir, name);
			if(!regFile.exists())
			{
				try {
					unpackFile(name);
				} catch(IOException e) {
					log.error("Error unpacking RegionFile: "+name, e);
				}
			}
			region = new RegionFile(regFile);
			regionCache.put(key, region);
		}
		return region;
	}
	
	private void clearCache(int limit)
	{
		if(regionCache.size() > limit)
		{
			try {
				ThreadedFileIOBase.threadedIOInstance.waitForFinish(this);
			} catch (InterruptedException ignored) {}
			
			for(RegionFile region : regionCache.values())
				try{region.close();}catch(IOException ignored){}
			regionCache.clear();
		}
	}
	
	@Override
	protected boolean isChunkExistsInFile(int cx, int cz)
	{
		return getRegion(cx, cz).chunkExists(cx & 31, cz & 31);
	}

	@Override
	protected DataInputStream getChunkInputStream(int cx, int cz)
	{
		return getRegion(cx, cz).getChunkDataInputStream(cx & 31, cz & 31);
	}

	@Override
	protected DataOutputStream getChunkOutputStream(AnvilChunkLoader.PendingChunk pending)
	{
		return getRegion(pending.chunkCoordinate.chunkXPos, pending.chunkCoordinate.chunkZPos)
				.getChunkDataOutputStream(pending.chunkCoordinate.chunkXPos & 31, pending.chunkCoordinate.chunkZPos & 31);
	}

	@Override
	public void saveChunk(World world, Chunk chunk) throws MinecraftException, IOException
	{
		if(!tempDir.exists() || closed)
			return;
		super.saveChunk(world, chunk);
	}

	@Override
	public boolean writeNextIO()
	{
		if(!tempDir.exists() || closed)
			return false;
		return super.writeNextIO();
	}
	
	public synchronized void close()
	{
		clearCache(0);
		closed = true;
	}
}
