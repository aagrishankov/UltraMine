package net.minecraft.world.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerFileData
{
	void writePlayerData(EntityPlayer p_75753_1_);

	NBTTagCompound readPlayerData(EntityPlayer p_75752_1_);

	String[] getAvailablePlayerDat();
}