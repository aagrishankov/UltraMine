package net.minecraft.world.gen;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.openhft.koloboke.collect.IntCursor;
import net.openhft.koloboke.collect.map.IntObjCursor;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.WorldConstants;
import org.ultramine.server.chunk.CallbackMultiChunkDependentTask;
import org.ultramine.server.chunk.ChunkBindState;
import org.ultramine.server.chunk.ChunkGC;
import org.ultramine.server.chunk.ChunkGenerationQueue;
import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.chunk.ChunkMap;
import org.ultramine.server.chunk.IChunkLoadCallback;
import org.ultramine.server.internal.UMHooks;
import org.ultramine.server.util.VanillaChunkHashMap;
import org.ultramine.server.util.VanillaChunkHashSet;

public class ChunkProviderServer implements IChunkProvider
{
	private static final Logger logger = LogManager.getLogger();
	public IntSet unloadQueue = HashIntSets.newMutableSet();
	public Set<Long> chunksToUnload = new VanillaChunkHashSet(unloadQueue); //mods compatibility
	private Chunk defaultEmptyChunk;
	public IChunkProvider currentChunkProvider;
	public IChunkLoader currentChunkLoader;
	public boolean loadChunkOnProvideRequest = true;
	public ChunkMap chunkMap = new ChunkMap();
	public LongHashMap loadedChunkHashMap = new VanillaChunkHashMap(chunkMap); //mods compatibility
	public WorldServer worldObj;
	public List loadedChunks = new AbstractList() //mods compatibility
	{
		@Override
		public Object get(int ind)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int size()
		{
			return chunkMap.size();
		}
		
		@Override
		public Iterator iterator()
		{
			return chunkMap.valueCollection().iterator();
		}
		
		@Override
		public Object[] toArray()
		{
			return chunkMap.valueCollection().toArray();
		}
	};
	private static final String __OBFID = "CL_00001436";

	public ChunkProviderServer(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader, IChunkProvider par3IChunkProvider)
	{
		this.defaultEmptyChunk = new EmptyChunk(par1WorldServer, 0, 0);
		this.worldObj = par1WorldServer;
		this.currentChunkLoader = par2IChunkLoader;
		this.currentChunkProvider = par3IChunkProvider;
		
		if(isServer)
			chunkGC = new ChunkGC(this);
	}

	public List func_152380_a()
	{
		return new ArrayList<Chunk>(chunkMap.valueCollection());
	}
	
	public boolean chunkExists(int par1, int par2)
	{
		return this.chunkMap.contains(par1, par2);
	}

	public void unloadChunksIfNotNearSpawn(int par1, int par2)
	{
		Chunk chunk = chunkMap.get(par1, par2);
		if(chunk != null)
		{
			chunk.unbind();
			if(chunk.canUnload())
				unloadQueue.add(ChunkHash.chunkToKey(par1, par2));
		}
	}

	public void unloadAllChunks()
	{
		//Iterator iterator = this.loadedChunks.iterator();

		//while (iterator.hasNext())
		//{
		//	Chunk chunk = (Chunk)iterator.next();
		//	this.unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
		//}
	}

	public Chunk loadChunk(int par1, int par2)
	{
		return loadChunk(par1, par2, null);
	}

	public Chunk loadChunk(int par1, int par2, Runnable runnable)
	{
		this.unloadQueue.removeInt(ChunkHash.chunkToKey(par1, par2));
		Chunk chunk = chunkMap.get(par1, par2);
		AnvilChunkLoader loader = null;

		if (this.currentChunkLoader instanceof AnvilChunkLoader)
		{
			loader = (AnvilChunkLoader) this.currentChunkLoader;
		}

		// We can only use the queue for already generated chunks
		if (chunk == null && loader != null && loader.chunkExists(this.worldObj, par1, par2))
		{
			if(isWorldUnloaded())
				return defaultEmptyChunk;
			if (runnable != null)
			{
				ChunkIOExecutor.queueChunkLoad(this.worldObj, loader, this, par1, par2, runnable);
				return null;
			}
			else
			{
				chunk = ChunkIOExecutor.syncChunkLoad(this.worldObj, loader, this, par1, par2);
				chunk.setBindState(ChunkBindState.LEAK);
				if(debugSyncLoad && worldObj != null && worldObj.func_73046_m() != null && worldObj.func_73046_m().getTickCounter() > 1)
					logger.warn("The chunk ["+worldObj.provider.dimensionId+"]("+par1+", "+par2+") was loaded sync", new Throwable());
			}
		}
		else if (chunk == null)
		{
			chunk = this.originalLoadChunk(par1, par2);
		}

		// If we didn't load the chunk async and have a callback run it now
		if (runnable != null)
		{
			runnable.run();
		}

		return chunk;
	}

	public Chunk originalLoadChunk(int par1, int par2)
	{
		int k = ChunkHash.chunkToKey(par1, par2);
		this.unloadQueue.removeInt(k);
		Chunk chunk = (Chunk)this.chunkMap.get(par1, par2);

		if (chunk == null)
		{
//			chunk = ForgeChunkManager.fetchDormantChunk(k, this.worldObj);
//			if (chunk == null)
			{
				chunk = this.safeLoadChunk(par1, par2);
			}

			if (chunk == null)
			{
				if (this.currentChunkProvider == null)
				{
					chunk = this.defaultEmptyChunk;
				}
				else
				{
					try
					{
						boolean lastIsGenerating = isGenerating;
						isGenerating = true;
						chunk = this.currentChunkProvider.provideChunk(par1, par2);
						isGenerating = lastIsGenerating;
					}
					catch (Throwable throwable)
					{
						CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
						CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
						crashreportcategory.addCrashSection("Location", String.format("%d,%d", new Object[] {Integer.valueOf(par1), Integer.valueOf(par2)}));
						crashreportcategory.addCrashSection("Position hash", Long.valueOf(k));
						crashreportcategory.addCrashSection("Generator", this.currentChunkProvider.makeString());
						throw new ReportedException(crashreport);
					}
				}
			}

			this.chunkMap.put(par1, par2, chunk);
			chunk.onChunkLoad();
			chunk.populateChunk(this, this, par1, par2);
		}

		return chunk;
	}

	public Chunk provideChunk(int par1, int par2)
	{
		Chunk chunk = this.chunkMap.get(par1, par2);
		return chunk == null ? (!this.worldObj.findingSpawnPoint && !this.loadChunkOnProvideRequest ? this.defaultEmptyChunk : this.loadChunk(par1, par2)) : chunk;
	}

	private Chunk safeLoadChunk(int par1, int par2)
	{
		if (this.currentChunkLoader == null)
		{
			return null;
		}
		else
		{
			try
			{
				Chunk chunk = this.currentChunkLoader.loadChunk(this.worldObj, par1, par2);

				if (chunk != null)
				{
					chunk.lastSaveTime = this.worldObj.getTotalWorldTime();

					if (this.currentChunkProvider != null)
					{
						this.currentChunkProvider.recreateStructures(par1, par2);
					}
				}

				return chunk;
			}
			catch (Exception exception)
			{
				logger.error("Couldn\'t load chunk", exception);
				return null;
			}
		}
	}

	private void safeSaveExtraChunkData(Chunk par1Chunk)
	{
		if (this.currentChunkLoader != null)
		{
			try
			{
				this.currentChunkLoader.saveExtraChunkData(this.worldObj, par1Chunk);
			}
			catch (Exception exception)
			{
				logger.error("Couldn\'t save entities", exception);
			}
		}
	}

	private void safeSaveChunk(Chunk par1Chunk)
	{
		if (this.currentChunkLoader != null)
		{
			try
			{
				par1Chunk.lastSaveTime = this.worldObj.getTotalWorldTime();
				this.currentChunkLoader.saveChunk(this.worldObj, par1Chunk);
			}
			catch (IOException ioexception)
			{
				logger.error("Couldn\'t save chunk", ioexception);
			}
			catch (MinecraftException minecraftexception)
			{
				logger.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", minecraftexception);
			}
		}
	}

	public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
	{
		Chunk chunk = this.getChunkIfExists(par2, par3);

		if (chunk != null && !chunk.isTerrainPopulated && worldObj.chunkRoundExists(par2, par3, WorldConstants.GENCHUNK_PRELOAD_RADIUS))
		{
			chunk.func_150809_p();

			if (this.currentChunkProvider != null)
			{
				boolean lastIsGenerating = isGenerating;
				isGenerating = true;
				this.currentChunkProvider.populate(par1IChunkProvider, par2, par3);
				if(!worldObj.getConfig().generation.disableModGeneration)
					GameRegistry.generateWorld(par2, par3, worldObj, currentChunkProvider, par1IChunkProvider);
				chunk.setChunkModified();
				UMHooks.onChunkPopulated(chunk);
				isGenerating = lastIsGenerating;
			}
		}
	}

	public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
	{
		int i = 0;

		for (Chunk chunk : chunkMap.valueCollection())
		{
			if (par1)
			{
				this.safeSaveExtraChunkData(chunk);
			}

			if (chunk.needsSaving(par1))
			{
				this.safeSaveChunk(chunk);
				chunk.isModified = false;
				chunk.postSave();
				++i;

				if (i == 24 && !par1)
				{
					return false;
				}
			}
		}

		return true;
	}

	public void saveExtraData()
	{
		if (this.currentChunkLoader != null)
		{
			this.currentChunkLoader.saveExtraData();
		}
	}

	public boolean unloadQueuedChunks()
	{
		if (!preventSaving)
		{
			if(isServer)
				chunkGC.onTick();

			/*
			for (int i = 0; i < 100; ++i)
			{
				if (!this.chunksToUnload.isEmpty())
				{
					Long olong = (Long)this.chunksToUnload.iterator().next();
					Chunk chunk = (Chunk)this.loadedChunkHashMap.getValueByKey(olong.longValue());
					chunk.onChunkUnload();
					this.safeSaveChunk(chunk);
					this.safeSaveExtraChunkData(chunk);
					this.chunksToUnload.remove(olong);
					this.loadedChunkHashMap.remove(olong.longValue());
					this.loadedChunks.remove(chunk);
					ForgeChunkManager.putDormantChunk(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
					if(loadedChunks.size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId)){
						DimensionManager.unloadWorld(this.worldObj.provider.dimensionId);
						return currentChunkProvider.unloadQueuedChunks();
					}
				}
			}
			*/
			
			Set<ChunkCoordIntPair> persistentChunks = worldObj.getPersistentChunks().keySet();
			int savequeueSize = ((AnvilChunkLoader)currentChunkLoader).getSaveQueueSize();
			
			for(IntCursor it = unloadQueue.cursor(); it.moveNext() && savequeueSize < MAX_SAVE_QUEUE_SIZE;)
			{
				int hash = it.elem();
				Chunk chunk = chunkMap.get(hash);
				if(chunk != null)
				{
					if(chunk.canUnload() && !persistentChunks.contains(chunk.getChunkCoordIntPair()))
					{
						chunk.onChunkUnload();
						if(chunk.shouldSaveOnUnload())
						{
							savequeueSize++;
							safeSaveChunk(chunk);
						}
						else
						{
							MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, new NBTTagCompound())); //CodeChickenLib memory leak fix
						}
						this.safeSaveExtraChunkData(chunk);
						this.chunkMap.remove(hash);
						chunk.release();
					}
				}
				
				it.remove();
			}

			if (this.currentChunkLoader != null)
			{
				this.currentChunkLoader.chunkTick();
			}
		}

		return this.currentChunkProvider.unloadQueuedChunks();
	}

	public boolean canSave()
	{
		return !this.worldObj.levelSaving;
	}

	public String makeString()
	{
		return "ServerChunkCache: " + this.chunkMap.size() + " Drop: " + this.unloadQueue.size();
	}

	public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
	{
		return this.currentChunkProvider.getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
	}

	public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
	{
		return this.currentChunkProvider.func_147416_a(p_147416_1_, p_147416_2_, p_147416_3_, p_147416_4_, p_147416_5_);
	}

	public int getLoadedChunkCount()
	{
		return this.chunkMap.size();
	}

	public void recreateStructures(int par1, int par2) {}
	
	
	/* ======================================== ULTRAMINE START =====================================*/
	
	private static final int MAX_SAVE_QUEUE_SIZE = 64;
	private static final int FULL_SAVE_INTERVAL = 10*60*20; //10 min
	private static final boolean isServer = FMLCommonHandler.instance().getSide().isServer();
	private static final boolean debugSyncLoad = Boolean.parseBoolean(System.getProperty("ultramine.debug.chunksyncload"));

	private final IntSet possibleSaves = HashIntSets.newMutableSet();
	private int lastFullSaveTick;
	private boolean preventSaving;
	private boolean isWorldUnloaded;
	private boolean isGenerating;
	
	@SideOnly(Side.SERVER)
	private ChunkGC chunkGC;
	
	@SideOnly(Side.SERVER)
	public ChunkGC getChunkGC()
	{
		return chunkGC;
	}
	
	public boolean loadAsync(int x, int z, boolean generateOnRequest, IChunkLoadCallback callback)
	{
		Chunk chunk = chunkMap.get(x, z);
		if(chunk != null)
		{
			callback.onChunkLoaded(chunk);
		}
		else if(((AnvilChunkLoader)currentChunkLoader).chunkExists(worldObj, x, z))
		{
			ChunkIOExecutor.queueChunkLoad(this.worldObj, (AnvilChunkLoader)currentChunkLoader, this, x, z, callback);
		}
		else if(generateOnRequest)
		{
			callback.onChunkLoaded(originalLoadChunk(x, z));
		}
		else
		{
			return false;
		}

		return true;
	}

	public void loadAsync(int x, int z, IChunkLoadCallback callback)
	{
		if(!loadAsync(x, z, false, callback))
			ChunkGenerationQueue.instance().queueChunkGeneration(this, x, z, callback);
	}
	
	public void loadAsync(int x, int z)
	{
		loadAsync(x, z, IChunkLoadCallback.EMPTY);
	}
	
	/**
	 * Загружает все чанки в радиусе, callback вызывается для каждого чанка
	 */
	public void loadAsyncRadius(int cx, int cz, int radius, IChunkLoadCallback callback)
	{
		for(int x = cx - radius; x <= cx + radius; x++)
			for(int z = cz - radius; z <= cz + radius; z++)
				loadAsync(x, z, callback);
	}
	
	/**
	 * Загружает все чанки в радиусе, callback вызывается, когда все чанки загружены
	 */
	public void loadAsyncRadiusThenRun(int cx, int cz, int radius, Runnable callback)
	{
		loadAsyncRadius(cx, cz, radius, new CallbackMultiChunkDependentTask((radius*2+1)*(radius*2+1), callback));
	}
	
	/**
	 * Загружает все чанки в радиусе, callback вызывается только для центрального чанка (cx, cz),
	 * когда все чанки загружены
	 */
	public void loadAsyncWithRadius(final int cx, final int cz, int radius, final IChunkLoadCallback callback)
	{
		loadAsyncRadiusThenRun(cx, cz, radius, new Runnable()
		{
			@Override
			public void run()
			{
				callback.onChunkLoaded(chunkMap.get(cx, cz));
			}
		});
	}
	
	public Chunk getChunkIfExists(int cx, int cz)
	{
		return chunkMap.get(cx, cz);
	}
	
	public boolean isChunkGenerated(int cx, int cz)
	{
		return ((AnvilChunkLoader)currentChunkLoader).chunkExists(worldObj, cx, cz);
	}
	
	public void unbindChunk(int cx, int cz)
	{
		Chunk chunk = chunkMap.get(cx, cz);
		if(chunk != null)
			unbindChunk(chunk);
	}
	
	public void unbindChunk(Chunk chunk)
	{
		if(isServer)
		{
			chunk.unbind();
			possibleSaves.add(ChunkHash.chunkToKey(chunk.xPosition, chunk.zPosition));
		}
		else
		{
			unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
		}
	}
	
	public void saveOneChunk(int tick)
	{
		if(preventSaving)
			return;
		
		if(tick - lastFullSaveTick >= FULL_SAVE_INTERVAL)
		{
			for(IntObjCursor<Chunk> it = chunkMap.iterator(); it.moveNext();)
			{
				int key = it.key();
				if(it.value().needsSaving(false) && !unloadQueue.contains(key))
					possibleSaves.add(key);
			}
			
			lastFullSaveTick = tick;
		}
		
		if(!possibleSaves.isEmpty())
		{
			int count = Math.min(10, Math.max(1, possibleSaves.size()/(FULL_SAVE_INTERVAL - tick + lastFullSaveTick)));
			
			for(IntCursor it = possibleSaves.cursor(); it.moveNext();)
			{
				int key = it.elem();
				it.remove();
				Chunk chunk = chunkMap.get(key);
				if(chunk != null && chunk.needsSaving(false) && !unloadQueue.contains(key))
				{
					safeSaveChunk(chunk);
					chunk.isModified = false;
					chunk.postSave();
					if(--count == 0) break;
				}
			}
		}
	}
	
	public void populateChunk(Chunk chunk, int cx, int cz)
	{
		final int radius = WorldConstants.GENCHUNK_PRELOAD_RADIUS;
		for(int x = cx - radius; x <= cx + radius; x++)
			for(int z = cz - radius; z <= cz + radius; z++)
				populate(this, x, z);
	}
	
	public void preventSaving()
	{
		preventSaving = true;
	}
	
	public void resumeSaving()
	{
		preventSaving = false;
	}
	
	public void setWorldUnloaded()
	{
		isWorldUnloaded = true;
	}
	
	public boolean isWorldUnloaded()
	{
		return isWorldUnloaded;
	}
	
	public void unloadAll(boolean save)
	{
		for(Chunk chunk : chunkMap.valueCollection())
		{
			chunk.onChunkUnload();
			if(save && chunk.shouldSaveOnUnload())
				safeSaveChunk(chunk);
			else
				MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, new NBTTagCompound())); //CodeChickenLib memory leak fix
			chunk.release();
		}
		
		chunkMap.clear();
		unloadQueue.clear();
		possibleSaves.clear();
		if(!save)
			((AnvilChunkLoader)currentChunkLoader).unsafeRemoveAll();
	}
	
	public void release()
	{
		for(Chunk chunk : chunkMap.valueCollection())
			chunk.release();
		chunkMap.clear();
		setWorldUnloaded();
	}
	
	public boolean isGenerating()
	{
		return isGenerating;
	}
}