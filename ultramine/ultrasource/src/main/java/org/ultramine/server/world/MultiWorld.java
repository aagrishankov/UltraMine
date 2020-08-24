package org.ultramine.server.world;

import gnu.trove.TCollections;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.ConfigUtil;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiWorld
{
	private static final Logger log = LogManager.getLogger();
	
	private final MinecraftServer server;
	private final TIntIntMap provTranslt = new TIntIntHashMap();
	private final TIntObjectMap<WorldDescriptor> dimToWorldMap = new TIntObjectHashMap<WorldDescriptor>();
	private final Map<String, WorldDescriptor> nameToWorldMap = new HashMap<String, WorldDescriptor>();
	private TIntSet isolatedDataDims;
	private boolean serverLoaded;
	
	public MultiWorld(MinecraftServer server)
	{
		this.server = server;
	}
	
	public void registerProviderTranslation(int src, int dst)
	{
		provTranslt.put(src, dst);
	}
	
	void sendDimensionToAll(int dim, int pid)
	{
		if(!serverLoaded)
			return;
		FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
		channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channel.writeAndFlush(new ForgeMessage.DimensionRegisterMessage(dim, provTranslt.containsKey(pid) ? provTranslt.get(pid) : pid));
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(FMLNetworkEvent.ServerConnectionFromClientEvent event)
	{
		FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
		channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
		channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(event.manager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
		for (int dim : DimensionManager.getStaticDimensionIDs())
		{
			WorldDescriptor desc = getDescByID(dim);
			if(desc != null && desc.isSendDimToPlayers())
			{
				int pid = DimensionManager.getProviderType(dim);
				channel.writeAndFlush(new ForgeMessage.DimensionRegisterMessage(dim, provTranslt.containsKey(pid) ? provTranslt.get(pid) : pid));
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWorldUnload(WorldEvent.Unload e)
	{
		if(e.world.isRemote)
			return;
		WorldDescriptor desc = getDescByID(e.world.provider.dimensionId);
		if(desc != null)
			desc.onUnload();
	}
	
	@SideOnly(Side.SERVER)
	public void preloadConfigs()
	{
		TIntSet isolatedDataDimsSet = new TIntHashSet();
		checkDuplicates(ConfigurationHandler.getWorldsConfig().worlds);
		for(WorldConfig config : ConfigurationHandler.getWorldsConfig().worlds)
		{
			WorldDescriptor desc = getOrCreateDescriptor(config.dimension);
			if(config.name != null)
				desc.setName(config.name);
			
			desc.setConfig(config);
			if(config.settings.useIsolatedPlayerData)
				isolatedDataDimsSet.add(config.dimension);
		}
		isolatedDataDims = TCollections.unmodifiableSet(isolatedDataDimsSet);
	}
	
	@SideOnly(Side.SERVER)
	public void handleServerWorldsInit()
	{
		DimensionManager.registerProviderType(-10, org.ultramine.server.world.WorldProviderEmpty.class, false);
		registerProviderTranslation(-10, 0);
		DimensionManager.unregisterDimension(-1);
		DimensionManager.unregisterDimension(0);
		DimensionManager.unregisterDimension(1);
		
		//Dims added by mods only (already registered)
		for(int dim : DimensionManager.getStaticDimensionIDs())
		{
			WorldDescriptor desc = getOrCreateDescriptor(dim);
			desc.setState(WorldState.AVAILABLE);
			if(desc.getConfig() == null)
				desc.setConfig(cloneGlobalConfig());
		}
		
		//
		
		//register worlds.yml world (may redefine)
		for(WorldConfig config : ConfigurationHandler.getWorldsConfig().worlds)
		{
			WorldDescriptor desc = getOrCreateDescriptor(config.dimension);
			if(desc.getState() == WorldState.UNREGISTERED)
				desc.register();
		}
		
		WorldDescriptor overDesc = getDescByID(0);
		if(overDesc == null)
			throw new RuntimeException("WorldDescriptor for OverWorld (dimension = 0) not found!");
		overDesc.weakLoadNow();
		
		for(WorldDescriptor desc : nameToWorldMap.values())
			if(desc.getDimension() != 0)
				desc.weakLoadNow();
		
		serverLoaded = true;
	}
	
	@SideOnly(Side.SERVER)
	public void reloadServerWorlds()
	{
		TIntSet isolatedDataDimsSet = new TIntHashSet(isolatedDataDims);
		checkDuplicates(ConfigurationHandler.getWorldsConfig().worlds);
		for(WorldConfig config : ConfigurationHandler.getWorldsConfig().worlds)
		{
			WorldDescriptor desc = getOrCreateDescriptor(config.dimension);
			if(config.name != null)
				desc.setName(config.name);
			
			desc.setConfig(config);
			if(desc.getState() == WorldState.UNREGISTERED)
				desc.register();
			if(config.settings.useIsolatedPlayerData)
				isolatedDataDimsSet.add(config.dimension);
			else
				isolatedDataDimsSet.remove(config.dimension);
		}
		isolatedDataDims = TCollections.unmodifiableSet(isolatedDataDimsSet);
	}
	
	@SideOnly(Side.SERVER)
	public void initDimension(int dim)
	{
		WorldDescriptor desc = getDescByID(dim);
		if(desc == null && DimensionManager.isDimensionRegistered(dim))
		{
			desc = getOrCreateDescriptor(dim);
			desc.setState(WorldState.AVAILABLE);
			desc.setConfig(cloneGlobalConfig());
			desc.setSendDimToPlayers(false);
		}
		if(desc != null)
			desc.weakLoadNow();
	}
	
	@SideOnly(Side.CLIENT)
	public void handleClientWorldsInit()
	{
		for(WorldServer world : server.worldServers)
			onClientInitDimension(world);
	}
	
	@SideOnly(Side.CLIENT)
	public void onClientInitDimension(WorldServer world)
	{
		WorldConfig conf = getDefaultClientConfig(world.provider.dimensionId);
		world.setConfig(conf);
		WorldDescriptor desc = getOrCreateDescriptor(world.provider.dimensionId);
		desc.setConfig(conf);
		desc.setState(WorldState.LOADED);
		desc.setWorld(world);
	}
	
	@SideOnly(Side.CLIENT)
	private WorldConfig getDefaultClientConfig(int dim)
	{
		WorldConfig conf = new WorldConfig();
		conf.generation = new WorldConfig.Generation();
		conf.mobSpawn = new WorldConfig.MobSpawn();
		conf.settings = new WorldConfig.Settings();
		conf.chunkLoading = new WorldConfig.ChunkLoading();
		conf.portals = new WorldConfig.Portals();
		
		conf.portals.netherLink = dim == -1 ? 0 : -1;
		conf.portals.enderLink = dim == 1 ? 0 : 1;
		
		return conf;
	}

	@SideOnly(Side.SERVER)
	static WorldConfig cloneGlobalConfig()
	{
		return ConfigUtil.deepClone(ConfigurationHandler.getWorldsConfig().global);
	}
	
	private void checkDuplicates(List<WorldConfig> list)
	{
		TIntObjectMap<WorldConfig> idMap = new TIntObjectHashMap<WorldConfig>();
		Map<String, WorldConfig> nameMap = new HashMap<String, WorldConfig>();
		
		for(WorldConfig config : list)
		{
			{
				WorldConfig prev = idMap.put(config.dimension, config);
				if(prev != null)
					throw new RuntimeException("Duplicate dimension ID in worlds.yml! Dimension: " + config.dimension + ", Names: "+prev.name + " " + config.name);
			}
			
			if(config.name != null)
			{
				WorldConfig prev = nameMap.put(config.name, config);
				if(prev != null)
					throw new RuntimeException("Duplicate world names in worlds.yml! Name: " + prev.name + ", Dimensions: "+prev.dimension + " " + config.dimension);
			}
		}
	}
	
	private WorldDescriptor getOrCreateDescriptor(int dim, String name)
	{
		WorldDescriptor desc = dimToWorldMap.get(dim);
		if(desc == null)
		{
			desc = new WorldDescriptor(server, this, !server.isSinglePlayer() && ConfigurationHandler.getServerConfig().settings.other.splitWorldDirs, dim, name);
			dimToWorldMap.put(dim, desc);
			nameToWorldMap.put(desc.getName(), desc);
		}
		
		return desc;
	}
	
	private WorldDescriptor getOrCreateDescriptor(int dim)
	{
		return getOrCreateDescriptor(dim, "world_unnamed" + dim);
	}
	
	void transitDescName(WorldDescriptor desc, String oldName, String newName)
	{
		if(desc.getState().isLoaded())
			throw new RuntimeException("Can not change name of loaded world");
		nameToWorldMap.remove(oldName);
		nameToWorldMap.put(newName, desc);
	}

	/** @return A random string from "000000" to "zzzzzz" */
	@SideOnly(Side.SERVER)
	private static String genRandomMark()
	{
		String rnd = Long.toString(System.nanoTime() % 0x81bf0fffL, Character.MAX_RADIX);
		if(rnd.length() != 6)
			return new StringBuilder("000000").replace(6-rnd.length(), 6, rnd).toString();
		return rnd;
	}
	
	@SideOnly(Side.SERVER)
	public static String getTempWorldName(int dim)
	{
		return "temp_"+genRandomMark()+"_"+dim;
	}
	
	@SideOnly(Side.SERVER)
	public int allocTempDim()
	{
		int dim = -2;
		while(getDescByID(dim) != null || DimensionManager.isDimensionRegistered(dim))
			dim--;
		return dim;
	}
	
	@SideOnly(Side.SERVER)
	public WorldDescriptor makeTempWorld()
	{
		int dim = allocTempDim();
		return makeTempWorld(getTempWorldName(dim), dim);
	}
	
	@SideOnly(Side.SERVER)
	public WorldDescriptor makeTempWorld(String name)
	{
		return makeTempWorld(name, allocTempDim());
	}
	
	@SideOnly(Side.SERVER)
	public WorldDescriptor makeTempWorld(String name, int dim)
	{
		if(getDescByID(dim) != null || DimensionManager.isDimensionRegistered(dim))
			throw new RuntimeException("WorldDescriptor for dimension "+dim+" already registered (on making temp world)");
		
		WorldDescriptor desc = getOrCreateDescriptor(dim, name);
		desc.setConfig(cloneGlobalConfig());
		desc.setTemp(true);

		return desc;
	}
	
	public WorldDescriptor getDescByID(int dim)
	{
		return dimToWorldMap.get(dim);
	}
	
	public WorldDescriptor getDescByName(String name)
	{
		return nameToWorldMap.get(name);
	}
	
	public WorldDescriptor getDescByNameOrID(String id)
	{
		return BasicTypeParser.isInt(id) ? dimToWorldMap.get(Integer.parseInt(id)) : nameToWorldMap.get(id);
	}
	
	public WorldDescriptor getDescFromWorld(WorldServer world)
	{
		return getOrCreateDescriptor(world.provider.dimensionId);
	}
	
	public WorldServer getWorldByID(int dim)
	{
		WorldDescriptor desc = getDescByID(dim);
		return desc == null ? null : desc.getOrLoadWorld();
	}
	
	public WorldServer getWorldByName(String name)
	{
		WorldDescriptor desc = getDescByName(name);
		return desc == null ? null : desc.getOrLoadWorld();
	}
	
	public WorldServer getWorldByNameOrID(String id)
	{
		WorldDescriptor desc = getDescByNameOrID(id);
		return desc == null ? null : desc.getOrLoadWorld();
	}
	
	public Collection<String> getAllNames()
	{
		return nameToWorldMap.keySet();
	}
	
	public Collection<WorldDescriptor> getAllDescs()
	{
		return nameToWorldMap.values();
	}
	
	public Collection<WorldServer> getLoadedWorlds()
	{
		List<WorldServer> worlds = new ArrayList<WorldServer>();
		for(WorldDescriptor desc : nameToWorldMap.values())
		{
			if(desc.getState().isLoaded())
				worlds.add(desc.getWorld());
		}
		return worlds;
	}
	
	public String getNameByID(int id)
	{
		WorldDescriptor desc = getDescByID(id);
		return desc == null ? null : desc.getName();
	}
	
	public WorldConfig getConfigByID(int dim)
	{
		WorldDescriptor desc = getDescByID(dim);
		if(desc == null)
			return server.isSinglePlayer() ? getDefaultClientConfig(dim) : cloneGlobalConfig();
		return desc.getConfig();
	}
	
	public Collection<String> getDirsForBackup()
	{
		if(!ConfigurationHandler.getServerConfig().settings.other.splitWorldDirs)
			return Arrays.asList(getDescByID(0).getName());

		List<String> dirs = new ArrayList<String>();
		for(WorldDescriptor desc : nameToWorldMap.values())
		{
			if(!desc.isTemp() && desc.getDirectory().isDirectory())
				dirs.add(desc.getName());
		}
		
		return dirs;
	}
	
	@SideOnly(Side.SERVER)
	public Collection<String> resolveSaveDirs(Collection<String> names)
	{
		if(!ConfigurationHandler.getServerConfig().settings.other.splitWorldDirs)
			return Arrays.asList(getDescByID(0).getName());

		List<String> dirs = new ArrayList<String>();
		for(String name : names)
		{
			WorldDescriptor desc = getDescByNameOrID(name);
			if(desc != null)
				dirs.add(desc.getName());
		}
		
		return dirs;
	}
	
	public String getSaveDirName(WorldServer world)
	{
		if(world instanceof WorldServerMulti)
			return getDescByID(0).getName();
		
		return getDescByID(world.provider.dimensionId).getName();
	}
	
	public TIntSet getIsolatedDataDims()
	{
		return isolatedDataDims;
	}
	
	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void unregister()
	{
		FMLCommonHandler.instance().bus().unregister(this);
		MinecraftForge.EVENT_BUS.unregister(this);
		dimToWorldMap.clear();
		nameToWorldMap.clear();
	}
	
	void dropDesc(WorldDescriptor desc)
	{
		dimToWorldMap.remove(desc.getDimension());
		nameToWorldMap.remove(desc.getName());
	}
}
