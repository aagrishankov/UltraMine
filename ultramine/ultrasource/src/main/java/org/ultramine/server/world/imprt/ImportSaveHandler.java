package org.ultramine.server.world.imprt;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.WorldInfo;

public abstract class ImportSaveHandler extends AnvilSaveHandler
{
	private static final Logger log = LogManager.getLogger();
	protected final File tempDir;
	
	protected ImportChunkLoader loader;
	
	protected ImportSaveHandler(boolean tempDirExists, boolean tempDirEmpty, String dirname)
	{
		super(MinecraftServer.getServer().getWorldsDir(), dirname, false);
		tempDir = getWorldDirectory();
		if(tempDir.exists())
		{
			if(!tempDirExists)
				deleteTempDir();
			else if(tempDirEmpty)
				cleanTempDir();
		}
	}
	
	protected void deleteTempDir()
	{
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch(IOException e) {
			log.error("Failed to delete directory: " + tempDir.getAbsolutePath(), e);
		}
	}
	
	protected void cleanTempDir()
	{
		try {
			FileUtils.cleanDirectory(tempDir);
		} catch(IOException e) {
			log.error("Failed to delete directory: " + tempDir.getAbsolutePath(), e);
		}
	}
	
	@Override
	public IChunkLoader getChunkLoader(WorldProvider provider)
	{
		if(loader != null)
			throw new IllegalStateException("Already loaded");
		
		try
		{
			unpackIfNecessary();
			
			return loader = createChunkLoader();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected abstract ImportChunkLoader createChunkLoader() throws IOException;
	
	public boolean shouldUnpack()
	{
		return !tempDir.exists() || tempDir.list().length == 0;
	}
	
	public void unpackIfNecessary() throws IOException
	{
		if(shouldUnpack())
			unpackExceptRegions();
	}
	
	protected abstract void unpackExceptRegions() throws IOException;
	
	public void close()
	{
		if(loader != null)
			loader.close();
		loader = null;
	}
	
	@Override
	public void flush()
	{
		close();
	}
	
	@Override protected void setSessionLock(){}
	@Override public void checkSessionLock() throws MinecraftException{}
	@Override public void saveWorldInfoWithPlayer(WorldInfo p_75755_1_, NBTTagCompound p_75755_2_){}
	@Override public void saveWorldInfo(WorldInfo p_75761_1_){}
	@Override public void writePlayerData(EntityPlayer p_75753_1_){}
}
