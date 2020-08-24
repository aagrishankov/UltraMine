package net.minecraft.world.storage;

import java.io.File;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

public interface ISaveHandler
{
	WorldInfo loadWorldInfo();

	void checkSessionLock() throws MinecraftException;

	IChunkLoader getChunkLoader(WorldProvider p_75763_1_);

	void saveWorldInfoWithPlayer(WorldInfo p_75755_1_, NBTTagCompound p_75755_2_);

	void saveWorldInfo(WorldInfo p_75761_1_);

	IPlayerFileData getSaveHandler();

	void flush();

	File getWorldDirectory();

	File getMapFileFromName(String p_75758_1_);

	String getWorldDirectoryName();
}