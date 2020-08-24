package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ultramine.server.data.ServerDataLoader;

import com.mojang.authlib.GameProfile;

public class PlayerData
{
	private final ServerDataLoader loader;
	private final Map<Class<? extends PlayerDataExtension>, PlayerDataExtension> data = new HashMap<Class<? extends PlayerDataExtension>, PlayerDataExtension>();
	private PlayerCoreData coreData;
	
	private GameProfile profile;

	public PlayerData(ServerDataLoader loader)
	{
		this.loader = loader;
	}
	
	public void loadExtensions(List<PlayerDataExtension> list)
	{
		for(PlayerDataExtension o : list)
			data.put(o.getClass(), o);
		coreData = get(PlayerCoreData.class);
	}
	
	public GameProfile getProfile()
	{
		return profile;
	}
	
	public void setProfile(GameProfile profile)
	{
		this.profile = profile;
	}

	public <T> T get(Class<T> clazz)
	{
		return clazz.cast(data.get(clazz));
	}

	public PlayerCoreData core()
	{
		return coreData;
	}
	
	public void save()
	{
		loader.savePlayerData(this);
	}
}
