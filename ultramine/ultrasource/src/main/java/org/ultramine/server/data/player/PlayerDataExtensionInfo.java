package org.ultramine.server.data.player;

import java.lang.reflect.Constructor;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerDataExtensionInfo
{
	private final Class<? extends PlayerDataExtension> clazz;
	private final Constructor<? extends PlayerDataExtension> constructor;
	private final String nbtTagName;
	
	public PlayerDataExtensionInfo(Class<? extends PlayerDataExtension> clazz, String nbtTagName)
	{
		this.clazz = clazz;
		this.nbtTagName = nbtTagName;
		try
		{
			this.constructor = clazz.getConstructor(PlayerData.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException("Bad PlayerDataExtension class " + clazz.getName(), e);
		}
	}
	
	public Class<? extends PlayerDataExtension> getExtClass()
	{
		return clazz;
	}
	
	public String getTagName()
	{
		return nbtTagName;
	}
	
	private PlayerDataExtension makeNew(PlayerData pdata)
	{
		try
		{
			return constructor.newInstance(pdata);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public PlayerDataExtension createFromNBT(PlayerData pdata, NBTTagCompound nbt)
	{
		PlayerDataExtension data = makeNew(pdata);
		if(nbt != null)
			data.readFromNBT(nbt.getCompoundTag(nbtTagName));
		return data;
	}
}
