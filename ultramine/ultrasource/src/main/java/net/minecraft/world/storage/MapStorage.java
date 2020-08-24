package net.minecraft.world.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.world.gen.structure.MapGenStructureData;
import org.ultramine.server.internal.UMHooks;
import org.ultramine.server.util.AsyncIOUtils;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.WorldSavedData;

public class MapStorage
{
	private ISaveHandler saveHandler;
	private Map loadedDataMap = new HashMap();
	private List loadedDataList = new ArrayList();
	private Map idCounts = new HashMap();
	private static final String __OBFID = "CL_00000604";

	public MapStorage(ISaveHandler p_i2162_1_)
	{
		this.saveHandler = p_i2162_1_;
		this.loadIdCounts();
	}

	public WorldSavedData loadData(Class p_75742_1_, String p_75742_2_)
	{
		WorldSavedData worldsaveddata = (WorldSavedData)this.loadedDataMap.get(p_75742_2_);

		if (worldsaveddata != null)
		{
			return worldsaveddata;
		}
		else
		{
			if (this.saveHandler != null)
			{
				boolean prevValue = p_75742_1_ == MapGenStructureData.class && NBTTagCompound.setUseKolobokeMap(true);
				try
				{
					File file1 = this.saveHandler.getMapFileFromName(p_75742_2_);

					if (file1 != null && file1.exists())
					{
						try
						{
							worldsaveddata = (WorldSavedData)p_75742_1_.getConstructor(new Class[] {String.class}).newInstance(new Object[] {p_75742_2_});
						}
						catch (Exception exception)
						{
							throw new RuntimeException("Failed to instantiate " + p_75742_1_.toString(), exception);
						}

						FileInputStream fileinputstream = new FileInputStream(file1);
						NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
						fileinputstream.close();
						worldsaveddata.readFromNBT(nbttagcompound.getCompoundTag("data"));
					}
				}
				catch (Exception exception1)
				{
					exception1.printStackTrace();
				}
				finally
				{
					if(p_75742_1_ == MapGenStructureData.class)
						NBTTagCompound.setUseKolobokeMap(prevValue);
				}
			}

			if (worldsaveddata != null)
			{
				this.loadedDataMap.put(p_75742_2_, worldsaveddata);
				this.loadedDataList.add(worldsaveddata);
			}

			return worldsaveddata;
		}
	}

	public void setData(String p_75745_1_, WorldSavedData p_75745_2_)
	{
		if (p_75745_2_ == null)
		{
			throw new RuntimeException("Can\'t set null data");
		}
		else
		{
			if (this.loadedDataMap.containsKey(p_75745_1_))
			{
				this.loadedDataList.remove(this.loadedDataMap.remove(p_75745_1_));
			}

			this.loadedDataMap.put(p_75745_1_, p_75745_2_);
			this.loadedDataList.add(p_75745_2_);
		}
	}

	public void saveAllData()
	{
		for (int i = 0; i < this.loadedDataList.size(); ++i)
		{
			WorldSavedData worldsaveddata = (WorldSavedData)this.loadedDataList.get(i);

			if (worldsaveddata.isDirty())
			{
				this.saveData(worldsaveddata);
				worldsaveddata.setDirty(false);
			}
		}
	}

	private void saveData(WorldSavedData p_75747_1_)
	{
		if (this.saveHandler != null)
		{
			boolean prevValue = NBTTagCompound.setUseKolobokeMap(true);
			try
			{
				File file1 = this.saveHandler.getMapFileFromName(p_75747_1_.mapName);

				if (file1 != null)
				{
					if(p_75747_1_ instanceof MapGenStructureData)
					{
						UMHooks.writeMapGenStructureData((MapGenStructureData) p_75747_1_, file1);
						return;
					}

					NBTTagCompound nbttagcompound = new NBTTagCompound();
					p_75747_1_.writeToNBT(nbttagcompound);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setTag("data", nbttagcompound);
					AsyncIOUtils.safeWriteNBT(file1, nbttagcompound1);
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				NBTTagCompound.setUseKolobokeMap(prevValue);
			}
		}
	}

	private void loadIdCounts()
	{
		try
		{
			this.idCounts.clear();

			if (this.saveHandler == null)
			{
				return;
			}

			File file1 = this.saveHandler.getMapFileFromName("idcounts");

			if (file1 != null && file1.exists())
			{
				DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));
				NBTTagCompound nbttagcompound = CompressedStreamTools.read(datainputstream);
				datainputstream.close();
				Iterator iterator = nbttagcompound.func_150296_c().iterator();

				while (iterator.hasNext())
				{
					String s = (String)iterator.next();
					NBTBase nbtbase = nbttagcompound.getTag(s);

					if (nbtbase instanceof NBTTagShort)
					{
						NBTTagShort nbttagshort = (NBTTagShort)nbtbase;
						short short1 = nbttagshort.func_150289_e();
						this.idCounts.put(s, Short.valueOf(short1));
					}
				}
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public int getUniqueDataId(String p_75743_1_)
	{
		Short oshort = (Short)this.idCounts.get(p_75743_1_);

		if (oshort == null)
		{
			oshort = Short.valueOf((short)0);
		}
		else
		{
			oshort = Short.valueOf((short)(oshort.shortValue() + 1));
		}

		this.idCounts.put(p_75743_1_, oshort);

		if (this.saveHandler == null)
		{
			return oshort.shortValue();
		}
		else
		{
			try
			{
				File file1 = this.saveHandler.getMapFileFromName("idcounts");

				if (file1 != null)
				{
					NBTTagCompound nbttagcompound = new NBTTagCompound();
					Iterator iterator = this.idCounts.keySet().iterator();

					while (iterator.hasNext())
					{
						String s1 = (String)iterator.next();
						short short1 = ((Short)this.idCounts.get(s1)).shortValue();
						nbttagcompound.setShort(s1, short1);
					}

					DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));
					CompressedStreamTools.write(nbttagcompound, dataoutputstream);
					dataoutputstream.close();
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}

			return oshort.shortValue();
		}
	}
}