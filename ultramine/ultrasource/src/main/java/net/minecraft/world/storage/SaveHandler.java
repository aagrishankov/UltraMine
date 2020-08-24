package net.minecraft.world.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.StartupQuery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.util.GlobalExecutors;

public class SaveHandler implements ISaveHandler, IPlayerFileData
{
	private static final Logger logger = LogManager.getLogger();
	private final File worldDirectory;
	private final File playersDirectory;
	private final File mapDataDir;
	private final long initializationTime = MinecraftServer.getSystemTimeMillis();
	private final String saveDirectoryName;
	private static final String __OBFID = "CL_00000585";

	public SaveHandler(File p_i2146_1_, String p_i2146_2_, boolean p_i2146_3_)
	{
		this.worldDirectory = new File(p_i2146_1_, p_i2146_2_);
		this.worldDirectory.mkdirs();
		this.playersDirectory = new File(this.worldDirectory, "playerdata");
		this.mapDataDir = new File(this.worldDirectory, "data");
		this.mapDataDir.mkdirs();
		this.saveDirectoryName = p_i2146_2_;

		if (p_i2146_3_)
		{
			this.playersDirectory.mkdirs();
		}

		this.setSessionLock();
	}

	protected void setSessionLock()
	{
		try
		{
			File file1 = new File(this.worldDirectory, "session.lock");
			DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));

			try
			{
				dataoutputstream.writeLong(this.initializationTime);
			}
			finally
			{
				dataoutputstream.close();
			}
		}
		catch (IOException ioexception)
		{
			ioexception.printStackTrace();
			throw new RuntimeException("Failed to check session lock, aborting");
		}
	}

	public File getWorldDirectory()
	{
		return this.worldDirectory;
	}

	public void checkSessionLock() throws MinecraftException
	{
		try
		{
			File file1 = new File(this.worldDirectory, "session.lock");
			DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));

			try
			{
				if (datainputstream.readLong() != this.initializationTime)
				{
					throw new MinecraftException("The save is being accessed from another location, aborting");
				}
			}
			finally
			{
				datainputstream.close();
			}
		}
		catch (IOException ioexception)
		{
			throw new MinecraftException("Failed to check session lock, aborting");
		}
	}

	public IChunkLoader getChunkLoader(WorldProvider p_75763_1_)
	{
		throw new RuntimeException("Old Chunk Storage is no longer supported.");
	}

	public WorldInfo loadWorldInfo()
	{
		File file1 = new File(this.worldDirectory, "level.dat");
		NBTTagCompound nbttagcompound;
		NBTTagCompound nbttagcompound1;

		WorldInfo worldInfo = null;

		if (file1.exists())
		{
			try
			{
				nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
				nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
				worldInfo = new WorldInfo(nbttagcompound1);
				FMLCommonHandler.instance().handleWorldDataLoad(this, worldInfo, nbttagcompound);
				return worldInfo;
			}
			catch (StartupQuery.AbortedException e)
			{
				throw e;
			}
			catch (Exception exception1)
			{
				exception1.printStackTrace();
			}
		}

		FMLCommonHandler.instance().confirmBackupLevelDatUse(this);
		file1 = new File(this.worldDirectory, "level.dat_old");

		if (file1.exists())
		{
			try
			{
				nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
				nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
				worldInfo = new WorldInfo(nbttagcompound1);
				FMLCommonHandler.instance().handleWorldDataLoad(this, worldInfo, nbttagcompound);
				return worldInfo;
			}
			catch (StartupQuery.AbortedException e)
			{
				throw e;
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}

		return null;
	}

	public void saveWorldInfoWithPlayer(WorldInfo p_75755_1_, NBTTagCompound p_75755_2_)
	{
		NBTTagCompound nbttagcompound1 = p_75755_1_.cloneNBTCompound(p_75755_2_);
		final NBTTagCompound nbttagcompound2 = new NBTTagCompound();
		nbttagcompound2.setTag("Data", nbttagcompound1);

		FMLCommonHandler.instance().handleWorldDataSave(this, p_75755_1_, nbttagcompound2);

		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					File file1 = new File(worldDirectory, "level.dat_new");
					File file2 = new File(worldDirectory, "level.dat_old");
					File file3 = new File(worldDirectory, "level.dat");
					CompressedStreamTools.writeCompressed(nbttagcompound2, new FileOutputStream(file1));

					if (file2.exists())
					{
						file2.delete();
					}

					file3.renameTo(file2);

					if (file3.exists())
					{
						file3.delete();
					}

					file1.renameTo(file3);

					if (file1.exists())
					{
						file1.delete();
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}
		});
	}

	public void saveWorldInfo(WorldInfo p_75761_1_)
	{
		NBTTagCompound nbttagcompound = p_75761_1_.getNBTTagCompound();
		NBTTagCompound nbttagcompound1 = new NBTTagCompound();
		nbttagcompound1.setTag("Data", nbttagcompound);

		FMLCommonHandler.instance().handleWorldDataSave(this, p_75761_1_, nbttagcompound1);

		try
		{
			File file1 = new File(this.worldDirectory, "level.dat_new");
			File file2 = new File(this.worldDirectory, "level.dat_old");
			File file3 = new File(this.worldDirectory, "level.dat");
			CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file1));

			if (file2.exists())
			{
				file2.delete();
			}

			file3.renameTo(file2);

			if (file3.exists())
			{
				file3.delete();
			}

			file1.renameTo(file3);

			if (file1.exists())
			{
				file1.delete();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public void writePlayerData(EntityPlayer p_75753_1_)
	{
		try
		{
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			p_75753_1_.writeToNBT(nbttagcompound);
			File file1 = new File(this.playersDirectory, p_75753_1_.getUniqueID().toString() + ".dat.tmp");
			File file2 = new File(this.playersDirectory, p_75753_1_.getUniqueID().toString() + ".dat");
			CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file1));

			if (file2.exists())
			{
				file2.delete();
			}

			file1.renameTo(file2);
			net.minecraftforge.event.ForgeEventFactory.firePlayerSavingEvent(p_75753_1_, this.playersDirectory, p_75753_1_.getUniqueID().toString());
		}
		catch (Exception exception)
		{
			logger.warn("Failed to save player data for " + p_75753_1_.getCommandSenderName());
		}
	}

	public NBTTagCompound readPlayerData(EntityPlayer p_75752_1_)
	{
		NBTTagCompound nbttagcompound = null;

		try
		{
			File file1 = new File(this.playersDirectory, p_75752_1_.getUniqueID().toString() + ".dat");

			if (file1.exists() && file1.isFile())
			{
				nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
			}
		}
		catch (Exception exception)
		{
			logger.warn("Failed to load player data for " + p_75752_1_.getCommandSenderName());
		}

		if (nbttagcompound != null)
		{
			p_75752_1_.readFromNBT(nbttagcompound);
		}

		net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(p_75752_1_, playersDirectory, p_75752_1_.getUniqueID().toString());
		return nbttagcompound;
	}

	public IPlayerFileData getSaveHandler()
	{
		return this;
	}

	public String[] getAvailablePlayerDat()
	{
		String[] astring = this.playersDirectory.list();

		for (int i = 0; i < astring.length; ++i)
		{
			if (astring[i].endsWith(".dat"))
			{
				astring[i] = astring[i].substring(0, astring[i].length() - 4);
			}
		}

		return astring;
	}

	public void flush() {}

	public File getMapFileFromName(String p_75758_1_)
	{
		return new File(this.mapDataDir, p_75758_1_ + ".dat");
	}

	public String getWorldDirectoryName()
	{
		return this.saveDirectoryName;
	}

	public NBTTagCompound getPlayerNBT(EntityPlayerMP player)
	{
		try
		{
			File file1 = new File(this.playersDirectory, player.getUniqueID().toString() + ".dat");

			if (file1.exists() && file1.isFile())
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(file1));
			}
		}
		catch (Exception exception)
		{
			logger.warn("Failed to load player data for " + player.getCommandSenderName());
		}
		return null;
	}
	
	public File getPlayerSaveDir()
	{
		return playersDirectory;
	}
}
