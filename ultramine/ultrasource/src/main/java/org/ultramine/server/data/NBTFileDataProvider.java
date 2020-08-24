package org.ultramine.server.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;
import org.ultramine.server.util.AsyncIOUtils;
import org.ultramine.server.util.WarpLocation;
import org.ultramine.server.util.YamlConfigProvider;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.storage.SaveHandler;

public class NBTFileDataProvider implements IDataProvider
{
	private static final Logger log = LogManager.getLogger();

	private final ServerConfigurationManager mgr;
	private File umPlayerDir;
	private List<String> fastWarps = Collections.emptyList();
	private final Map<File, CachedPlayerStruct> savingPlayersCache = new ConcurrentHashMap<>();
	private long cachedPlayerCounter;

	public NBTFileDataProvider(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
	}
	
	@Override
	public void init()
	{
		if(umPlayerDir == null)
		{
			umPlayerDir = new File(((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), "ultramine");
			umPlayerDir.mkdir();
		}
	}

	@Override
	public boolean isUsingWorldPlayerDir()
	{
		return true;
	}

	@Override
	public NBTTagCompound loadPlayer(GameProfile player)
	{
		return loadPlayer((SaveHandler)mgr.getPlayerNBTLoader(), player);
	}

	@Override
	public NBTTagCompound loadPlayer(int dim, GameProfile player)
	{
		return loadPlayer((SaveHandler)mgr.getServerInstance().getMultiWorld().getWorldByID(dim).getSaveHandler(), player);
	}

	@Override
	public void savePlayer(GameProfile player, NBTTagCompound nbt)
	{
		savePlayer((SaveHandler)mgr.getPlayerNBTLoader(), player, nbt);
	}

	@Override
	public void savePlayer(int dim, GameProfile player, NBTTagCompound nbt)
	{
		savePlayer((SaveHandler)mgr.getServerInstance().getMultiWorld().getWorldByID(dim).getSaveHandler(), player, nbt);
	}

	@Override
	public PlayerData loadPlayerData(GameProfile player)
	{
		return readPlayerData(getPlayerDataNBT(player.getId().toString()));
	}
	
	public List<PlayerData> loadAllPlayerData()
	{
		List<PlayerData> list = new ArrayList<PlayerData>();
		for(File file : umPlayerDir.listFiles())
		{
			if(file.getName().endsWith(".dat"))
			{
				try
				{
					list.add(readPlayerData(CompressedStreamTools.readCompressed(new FileInputStream(file))));
				}
				catch(IOException e)
				{
					log.warn("Failed to load ultramine player data from " + file.getName(), e);
				}
			}
		}
		
		return list;
	}
	
	@Override
	public void savePlayerData(PlayerData data)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", data.getProfile().getId().toString());
		nbt.setString("name", data.getProfile().getName());
		for(PlayerDataExtensionInfo info : mgr.getDataLoader().getDataExtProviders())
		{
			NBTTagCompound extnbt = new NBTTagCompound();
			data.get(info.getExtClass()).writeToNBT(extnbt);
			nbt.setTag(info.getTagName(), extnbt);
		}
		
		AsyncIOUtils.safeWriteNBT(new File(umPlayerDir, data.getProfile().getId().toString() + ".dat"), nbt);
	}
	
	@Override
	public Map<String, WarpLocation> loadWarps()
	{
		File file = mgr.getServerInstance().getStorageFile("warps.yml");
		if(file.exists())
		{
			YamlWarpList warps = YamlConfigProvider.getOrCreateConfig(file, YamlWarpList.class);
			fastWarps = warps.fastWarps;
			return warps.warps;
		}
		
		return Collections.emptyMap();
	}
	
	@Override
	public void saveWarp(String name, WarpLocation warp)
	{
		writeWarpList();
	}
	
	@Override
	public void removeWarp(String name)
	{
		writeWarpList();
	}
	
	@Override
	public List<String> loadFastWarps()
	{
		return fastWarps;
	}

	@Override
	public void saveFastWarp(String name)
	{
		writeWarpList();
	}

	@Override
	public void removeFastWarp(String name)
	{
		writeWarpList();
	}

	public NBTTagCompound loadPlayer(SaveHandler sh, GameProfile player)
	{
		File file = getPlayerNbtFile(sh, player);
		CachedPlayerStruct data = savingPlayersCache.get(file);
		if(data != null)
			return data.nbt;

		if(file.exists())
		{
			try
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
			catch(IOException e)
			{
				log.warn("Failed to load player data for " + player.getName(), e);
			}
		}

		return null;
	}

	public void savePlayer(SaveHandler sh, GameProfile player, NBTTagCompound nbt)
	{
		File file = getPlayerNbtFile(sh, player);
		long nextId = cachedPlayerCounter++;
		savingPlayersCache.put(file, new CachedPlayerStruct(nbt, nextId));
		AsyncIOUtils.safeWriteNBT(file, nbt, () ->
			savingPlayersCache.computeIfPresent(file, (file1, data) -> data.id == nextId ? null : data)
		);
	}

	private static File getPlayerNbtFile(SaveHandler sh, GameProfile player)
	{
		return new File(sh.getPlayerSaveDir(), player.getId().toString() + ".dat");
	}

	private NBTTagCompound getPlayerDataNBT(String username)
	{
		try
		{
			File file = new File(umPlayerDir, username + ".dat");

			if (file.exists())
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
		}
		catch (IOException e)
		{
			log.warn("Failed to load ultramine player data for " + username, e);
		}

		return null;
	}
	
	private PlayerData readPlayerData(NBTTagCompound nbt)
	{
		PlayerData pdata = new PlayerData(mgr.getDataLoader());
		if(nbt != null && nbt.hasKey("id") && nbt.hasKey("name"))
			pdata.setProfile(new GameProfile(UUID.fromString(nbt.getString("id")), nbt.getString("name")));

		List<PlayerDataExtensionInfo> infos = mgr.getDataLoader().getDataExtProviders();
		List<PlayerDataExtension> data = new ArrayList<PlayerDataExtension>(infos.size());
		
		for(PlayerDataExtensionInfo info : infos)
		{
			data.add(info.createFromNBT(pdata, nbt));
		}
		
		pdata.loadExtensions(data);
		return pdata;
	}
	
	private void writeWarpList()
	{
		File file = mgr.getServerInstance().getStorageFile("warps.yml");
		YamlWarpList warps = new YamlWarpList();
		warps.warps = mgr.getDataLoader().getWarps();
		warps.fastWarps = mgr.getDataLoader().getFastWarps();
		YamlConfigProvider.saveConfig(file, warps);
	}
	
	private static class YamlWarpList
	{
		public Map<String, WarpLocation> warps;
		public List<String> fastWarps;
	}

	private static class CachedPlayerStruct
	{
		public NBTTagCompound nbt;
		public long id;

		public CachedPlayerStruct(NBTTagCompound nbt, long id)
		{
			this.nbt = nbt;
			this.id = id;
		}
	}
}
