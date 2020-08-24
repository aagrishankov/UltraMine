package org.ultramine.server.data;

import java.util.List;
import java.util.Map;

import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.WarpLocation;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Все запросы на запись выполняются синхронно. На чтение могут быть выполнены
 * как синхронно, так и асинхронно.
 */
public interface IDataProvider
{
	void init();
	
	boolean isUsingWorldPlayerDir();

	NBTTagCompound loadPlayer(GameProfile player);

	NBTTagCompound loadPlayer(int dim, GameProfile player);

	void savePlayer(GameProfile player, NBTTagCompound nbt);

	void savePlayer(int dim, GameProfile player, NBTTagCompound nbt);

	PlayerData loadPlayerData(GameProfile player);

	List<PlayerData> loadAllPlayerData();

	void savePlayerData(PlayerData data);

	Map<String, WarpLocation> loadWarps();

	void saveWarp(String name, WarpLocation warp);

	void removeWarp(String name);
	
	List<String> loadFastWarps();
	
	void saveFastWarp(String name);
	
	void removeFastWarp(String name);
}
