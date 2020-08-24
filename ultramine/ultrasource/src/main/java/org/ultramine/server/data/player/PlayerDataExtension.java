package org.ultramine.server.data.player;

import net.minecraft.nbt.NBTTagCompound;

public abstract class PlayerDataExtension
{
	protected final PlayerData data;
	
	public PlayerDataExtension(PlayerData data)
	{
		this.data = data;
	}
	
	public abstract void writeToNBT(NBTTagCompound nbt);
	
	public abstract void readFromNBT(NBTTagCompound nbt);
}
