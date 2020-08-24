package org.ultramine.server.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.Teleporter;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.util.GlobalExecutors;
import org.ultramine.server.util.WarpLocation;
import org.ultramine.server.world.load.IWorldLoader;
import org.ultramine.server.world.load.ImportWorldLoader;
import org.ultramine.server.world.load.OverworldLoader;
import org.ultramine.server.world.load.SplittedWorldLoader;
import org.ultramine.server.world.load.NotSplittedWorldLoader;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldDescriptor
{
	private static final Logger log = LogManager.getLogger();
	
	private final MinecraftServer server;
	private final MultiWorld mw;
	private final boolean splitWorldDirs;
	private final int dimension;
	
	private String name;
	private File directory;
	private WorldConfig config;
	private WorldState state = WorldState.UNREGISTERED;
	private final AtomicBoolean transitState = new AtomicBoolean();
	private boolean temp;
	private boolean sendDimToPlayers = true;
	private IWorldLoader worldLoader;
	private WorldServer world;
	
	public WorldDescriptor(MinecraftServer server, MultiWorld mw, boolean splitWorldDirs, int dimension, String name)
	{
		this.server = server;
		this.mw = mw;
		this.splitWorldDirs = splitWorldDirs;
		this.dimension = dimension;
		this.name = name;
	}
	
	public int getDimension()
	{
		return dimension;
	}
	
	public String getName()
	{
		return name;
	}
	
	void setName(String name)
	{
		if(!this.name.equals(name))
		{
			this.mw.transitDescName(this, this.name, name);
			this.name = name;
			if(dimension == 0 || splitWorldDirs)
				this.directory = new File(server.getWorldsDir(), this.name);
		}
	}

	private void inferDirectory()
	{
		if(dimension == 0 || splitWorldDirs)
		{
			this.directory = new File(server.getWorldsDir(), name);
		}
		else
		{
			String dirName = WorldProvider.getProviderForDimension(dimension).getSaveFolder();
			if(dirName == null || dirName.startsWith("../"))
				dirName = "DIM" + dimension;
			this.directory = new File(mw.getDescByID(0).getDirectory(), dirName);
		}
	}
	
	public File getDirectory()
	{
		if(this.directory == null)
			inferDirectory();
		return this.directory;
	}
	
	public WorldConfig getConfig()
	{
		return config;
	}
	
	void setConfig(WorldConfig config)
	{
		this.config = config;
		if(state.isLoaded())
			applyConfig();
	}
	
	public WorldState getState()
	{
		return state;
	}
	
	void setState(WorldState state)
	{
		this.state = state;
	}
	
	public boolean isTemp()
	{
		return temp;
	}
	
	public void setTemp(boolean temp)
	{
		this.temp = temp;
	}

	public boolean isSendDimToPlayers()
	{
		return sendDimToPlayers;
	}

	public void setSendDimToPlayers(boolean sendDimToPlayers)
	{
		this.sendDimToPlayers = sendDimToPlayers;
	}

	public WorldServer getWorld()
	{
		return world;
	}
	
	void setWorld(WorldServer world)
	{
		this.world = world;
		this.directory = world.getSaveHandler().getWorldDirectory();
	}
	
	public WorldServer getOrLoadWorld()
	{
		if(state != WorldState.LOADED)
			weakLoadNow();
		return world;
	}
	
	public void register()
	{
		if(state != WorldState.UNREGISTERED)
			throw new IllegalStateException("Dimension "+dimension+" already registered");
		if(config == null)
			throw new IllegalStateException("Can not register dimension "+dimension+": world config == null!");
		if(DimensionManager.isDimensionRegistered(dimension))
			DimensionManager.unregisterDimension(dimension);
		DimensionManager.registerDimension(dimension, config.generation.providerID);
		setState(WorldState.AVAILABLE);
		mw.sendDimensionToAll(dimension, config.generation.providerID);
	}
	
	@SideOnly(Side.SERVER)
	public void forceLoadNow()
	{
		if(state == WorldState.UNREGISTERED)
			register();
		loadNow();
	}
	
	@SideOnly(Side.SERVER)
	public void weakLoadNow()
	{
		if(state == WorldState.HELD || state == WorldState.UNREGISTERED)
			return;
		
		loadNow();
	}
	
	@SideOnly(Side.SERVER)
	private void loadNow()
	{
		if(state.isLoaded())
			throw new RuntimeException("Dimension ["+dimension+"] is already loaded");
		
		worldLoader = createLoader();
		if(worldLoader.hasAsyncLoadPhase())
			worldLoader.doAsyncLoadPhase();
		WorldServer world = worldLoader.doLoad();
		
		setWorld(world);
		initWorld();
	}
	
	private IWorldLoader createLoader()
	{
		if(dimension == 0)
			return new OverworldLoader(this, server);
		if(config.importFrom != null)
		{
			File file = new File(server.getHomeDirectory(), config.importFrom.file);
			if(!file.exists())
				throw new RuntimeException("File not found: "+file.getAbsolutePath());
			return new ImportWorldLoader(this, server);
		}
		
		if(!splitWorldDirs)
			return new NotSplittedWorldLoader(this, server);
		return new SplittedWorldLoader(this, server);
	}
	
	@SideOnly(Side.SERVER)
	private void initWorld()
	{
		world.addWorldAccess(new WorldManager(server, world));
		world.getWorldInfo().setGameType(server.getGameType());
		applyConfig();
		setState(WorldState.LOADED);
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
	}
	
	@SideOnly(Side.SERVER)
	private void applyConfig()
	{
		world.setConfig(config);
	}
	
	void onUnload()
	{
		world.theChunkProviderServer.setWorldUnloaded();
		world = null;
		if(getState().isLoaded())
			setState(WorldState.AVAILABLE);
	}
	
	public List<EntityPlayerMP> extractPlayers()
	{
		if(!state.isLoaded())
			return Collections.emptyList();
		
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>(world.playerEntities);
		for(EntityPlayerMP player : players)
		{
			world.removePlayerEntityDangerously(player);
			player.isDead = false;
			world.getEntityTracker().removePlayerFromTrackers(player);
			world.getPlayerManager().removePlayer(player);
			player.setWorld(null);
			player.theItemInWorldManager.setWorld(null);
		}
		world.playerEntities.clear();
		
		return players;
	}
	
	private void movePlayersOut()
	{
		WarpLocation spawn = server.getConfigurationManager().getDataLoader().getWarp("spawn");
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>(world.playerEntities);
		for(EntityPlayerMP player : players)
		{
			if(player.dimension == spawn.dimension)
			{
				player.playerNetServerHandler.kickPlayerFromServer("The world has been unloaded");
				world.removePlayerEntityDangerously(player);
				player.isDead = false;
				world.getEntityTracker().removePlayerFromTrackers(player);
				world.getPlayerManager().removePlayer(player);
			}
			else
			{
				Teleporter.tpNow(player, spawn);
			}
		}
	}
	
	@SideOnly(Side.SERVER)
	@SuppressWarnings("unchecked")
	private void destroyWorld(boolean save)
	{
		if(!getState().isLoaded())
			return;
		if(!world.playerEntities.isEmpty())
			movePlayersOut();
		
		WorldServer world = this.world;
		if(world.provider.dimensionId == 0)
			for(ScorePlayerTeam team : new ArrayList<ScorePlayerTeam>(world.getScoreboard().getTeams()))
				world.getScoreboard().removeTeam(team);
		
		world.theChunkProviderServer.setWorldUnloaded();
		world.theChunkProviderServer.unloadAll(save);
		world.processTileEntityUnload();
		if(save)
			world.saveOtherData();
		
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));
		DimensionManager.setWorld(world.provider.dimensionId, null);
		world.theChunkProviderServer.release();
		for(Object o : world.loadedTileEntityList)
			((TileEntity)o).setWorldObj(null);
		world.loadedTileEntityList.clear();
	}
	
	private void dispose()
	{
		if(worldLoader != null)
			worldLoader.dispose();
		worldLoader = null;
	}
	
	private void clearWorldDir()
	{
		if(!getDirectory().exists())
			return;
		
		try
		{
			FileUtils.cleanDirectory(getDirectory());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@SideOnly(Side.SERVER)
	public void unloadNow(boolean save)
	{
		if(!getState().isLoaded())
			return;
		
		checkTransition();
		destroyWorld(save);
		dispose();
	}
	
	@SideOnly(Side.SERVER)
	public void holdNow(boolean save)
	{
		if(getState() == WorldState.UNREGISTERED || getState() == WorldState.HELD)
			return;
		checkTransition();
		if(getState().isLoaded())
			unloadNow(save);
		setState(WorldState.HELD);
	}
	
	@SideOnly(Side.SERVER)
	public void deleteNow()
	{
		checkTransition();
		if(state.isLoaded())
			destroyWorld(false);
		
		dispose();
		clearWorldDir();
	}
	
	@SideOnly(Side.SERVER)
	public void unregister()
	{
		if(state == WorldState.UNREGISTERED)
			return;
		checkTransition();
		if(state.isLoaded())
		{
			destroyWorld(true);
			dispose();
		}
		DimensionManager.unregisterDimension(dimension);
		setState(WorldState.UNREGISTERED);
	}
	
	@SideOnly(Side.SERVER)
	public void drop()
	{
		unregister();
		mw.dropDesc(this);
	}

	@SideOnly(Side.SERVER)
	private static CompletableFuture<Void> execLater(Runnable toRun)
	{
		return CompletableFuture.runAsync(toRun, GlobalExecutors.nextTick());
	}
	
	@SideOnly(Side.SERVER)
	private void checkTransition()
	{
		if(transitState.get())
			throw new IllegalStateException("World ["+dimension+"] is in transitional state");
	}
	
	@SideOnly(Side.SERVER)
	private void startTransition()
	{
		if(!transitState.compareAndSet(false, true))
			throw new IllegalStateException("World ["+dimension+"] is in transitional state");
	}

	@SideOnly(Side.SERVER)
	private CompletableFuture<Void> endTransition(CompletableFuture<Void> last)
	{
		return last.whenComplete((v, e) -> {
			transitState.compareAndSet(true, false);
			if(e != null) {
				log.error("Error in world ["+dimension+"] state transition. Aborting", e);
				propagate(e);
			}
		});
	}
	
	private static RuntimeException propagate(Throwable t)
	{
		if(t instanceof CompletionException)
			throw (CompletionException) t;
		throw new CompletionException(t);
	}
	
	@SideOnly(Side.SERVER)
	private CompletableFuture<Void> loadLater()
	{
		if(state.isLoaded())
			throw new RuntimeException("Dimension ["+dimension+"] is already loaded");
		
		worldLoader = createLoader();
		if(!worldLoader.hasAsyncLoadPhase())
		{
			WorldServer world = worldLoader.doLoad();
			setWorld(world);
			initWorld();
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			return CompletableFuture.runAsync(() -> worldLoader.doAsyncLoadPhase(), GlobalExecutors.cachedIO()).thenRunAsync(() -> {
				WorldServer world = worldLoader.doLoad();
				
				setWorld(world);
				initWorld();
			}, GlobalExecutors.nextTick());
		}
	}
	
	@SideOnly(Side.SERVER)
	private CompletableFuture<Void> forceLoadLater0()
	{
		if(state == WorldState.UNREGISTERED)
			register();
		return loadLater();
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> forceLoadLater()
	{
		startTransition();
		return endTransition(forceLoadLater0());
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> weakLoadLater()
	{
		if(state == WorldState.HELD || state == WorldState.UNREGISTERED)
			return CompletableFuture.completedFuture(null);
		
		startTransition();
		return endTransition(loadLater());
	}
	
	@SideOnly(Side.SERVER)
	private CompletableFuture<Void> downgradeLater(final boolean save, WorldState targetState)
	{
		if(getState().ordinal() >= targetState.ordinal())
			return CompletableFuture.completedFuture(null);
		
		if(!getState().isLoaded())
		{
			setState(targetState);
			return CompletableFuture.completedFuture(null);
		}
		
		return execLater(() -> {
			if(getState().isLoaded())
				destroyWorld(save);
			setState(targetState);
		}).thenRunAsync(() -> dispose(), GlobalExecutors.cachedIO());
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> unloadLater(final boolean save)
	{
		if(!getState().isLoaded())
			return CompletableFuture.completedFuture(null);
		
		startTransition();
		return endTransition(downgradeLater(save, WorldState.AVAILABLE));
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> holdLater(boolean save)
	{
		if(getState() == WorldState.UNREGISTERED || getState() == WorldState.HELD)
			return CompletableFuture.completedFuture(null);
		
		startTransition();
		return endTransition(downgradeLater(save, WorldState.HELD));
	}
	
	@SideOnly(Side.SERVER)
	private CompletableFuture<Void> deleteLater0()
	{
		if(state.isLoaded())
			return downgradeLater(false, WorldState.HELD).thenRunAsync(() -> clearWorldDir(), GlobalExecutors.cachedIO());
		else
			return CompletableFuture.runAsync(() -> clearWorldDir(), GlobalExecutors.cachedIO());
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> deleteLater()
	{
		startTransition();
		return endTransition(deleteLater0());
	}
	
	@SideOnly(Side.SERVER)
	public CompletableFuture<Void> wipeLater()
	{
		startTransition();
		return endTransition(deleteLater0().thenComposeAsync(v -> forceLoadLater0(), GlobalExecutors.nextTick()));
	}
}
