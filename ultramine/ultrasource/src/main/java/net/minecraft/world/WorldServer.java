package net.minecraft.world;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.ChestGenHooks;
import static net.minecraftforge.common.ChestGenHooks.BONUS_CHEST;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.openhft.koloboke.collect.map.IntByteCursor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.WorldBorder;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.WorldsConfig.WorldConfig.Settings.WorldTime;
import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.chunk.PendingBlockUpdate;
import org.ultramine.server.event.ServerWorldEventProxy;
import org.ultramine.server.event.WorldUpdateObjectType;
import org.ultramine.server.mobspawn.MobSpawnManager;
import org.ultramine.server.util.BasicTypeParser;

public class WorldServer extends World
{
	private static final Logger logger = LogManager.getLogger();
	private final MinecraftServer mcServer;
	private final EntityTracker theEntityTracker;
	private final PlayerManager thePlayerManager;
	private Set pendingTickListEntriesHashSet;
	private TreeSet pendingTickListEntriesTreeSet;
	public ChunkProviderServer theChunkProviderServer;
	public boolean levelSaving;
	private boolean allPlayersSleeping;
	private int updateEntityTick;
	private final Teleporter worldTeleporter;
	private final SpawnerAnimals animalSpawner = new SpawnerAnimals();
	private WorldServer.ServerBlockEventList[] field_147490_S = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(null), new WorldServer.ServerBlockEventList(null)};
	private int blockEventCacheIndex;
	public static final WeightedRandomChestContent[] bonusChestContent = new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.stick, 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.planks), 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log), 0, 1, 3, 10), new WeightedRandomChestContent(Items.stone_axe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_axe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.stone_pickaxe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.apple, 0, 2, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 2, 3, 3), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log2), 0, 1, 3, 10)};
	private List pendingTickListEntriesThisTick = new ArrayList();
	private IntHashMap entityIdMap;
	private static final String __OBFID = "CL_00001437";

	public List<Teleporter> customTeleporters = new ArrayList<Teleporter>();

	public WorldServer(MinecraftServer p_i45284_1_, ISaveHandler p_i45284_2_, String p_i45284_3_, int p_i45284_4_, WorldSettings p_i45284_5_, Profiler p_i45284_6_)
	{
		super(p_i45284_2_, p_i45284_3_, p_i45284_5_, WorldProvider.getProviderForDimension(p_i45284_4_), p_i45284_6_);
		this.mcServer = p_i45284_1_;
		this.theEntityTracker = new EntityTracker(this);
		this.thePlayerManager = new PlayerManager(this);

		if (this.entityIdMap == null)
		{
			this.entityIdMap = new IntHashMap();
		}

		if (this.pendingTickListEntriesHashSet == null)
		{
			this.pendingTickListEntriesHashSet = new HashSet();
		}

		if (this.pendingTickListEntriesTreeSet == null)
		{
			this.pendingTickListEntriesTreeSet = new TreeSet();
		}

		this.worldTeleporter = new Teleporter(this);
		this.worldScoreboard = new ServerScoreboard(p_i45284_1_);
		ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData)this.mapStorage.loadData(ScoreboardSaveData.class, "scoreboard");

		if (scoreboardsavedata == null)
		{
			scoreboardsavedata = new ScoreboardSaveData();
			this.mapStorage.setData("scoreboard", scoreboardsavedata);
		}

		if (provider.dimensionId == 0) //Forge: We fix the global mapStorage, which causes us to share scoreboards early. So don't associate the save data with the temporary scoreboard
		{
			scoreboardsavedata.func_96499_a(this.worldScoreboard);
		}
		((ServerScoreboard)this.worldScoreboard).func_96547_a(scoreboardsavedata);
		DimensionManager.setWorld(p_i45284_4_, this);
	}

	public void tick()
	{
		super.tick();

		if (this.getWorldInfo().isHardcoreModeEnabled() && this.difficultySetting != EnumDifficulty.HARD)
		{
			this.difficultySetting = EnumDifficulty.HARD;
		}

		this.provider.worldChunkMgr.cleanupCache();

		if (this.areAllPlayersAsleep())
		{
			if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
			{
				long i = this.worldInfo.getWorldTime() + 24000L;
				this.worldInfo.setWorldTime(i - i % 24000L);
			}

			this.wakeAllPlayers();
		}

		this.theProfiler.startSection("mobSpawner");

		if (this.getGameRules().getGameRuleBooleanValue("doMobSpawning"))
		{
			if(isServer && mobSpawner != null)
				mobSpawner.performSpawn(spawnHostileMobs, spawnPeacefulMobs, worldInfo.getWorldTotalTime());
			else
				this.animalSpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
		}

		this.theProfiler.endStartSection("chunkSource");
		this.chunkProvider.unloadQueuedChunks();
		int j = this.calculateSkylightSubtracted(1.0F);

		if (j != this.skylightSubtracted)
		{
			this.skylightSubtracted = j;
		}

		this.worldInfo.incrementTotalWorldTime(this.worldInfo.getWorldTotalTime() + 1L);
		
		WorldTime time = getConfig().settings.time;
		long curTime = worldInfo.getWorldTime() % 24000;
		
		if(time == WorldTime.DAY && curTime > 10000)
			worldInfo.setWorldTime(worldInfo.getWorldTime() - curTime  + 24000 + 1000);
		if(time == WorldTime.NIGHT && (curTime < 14200 || curTime > 21800))
			worldInfo.setWorldTime(worldInfo.getWorldTime() - curTime + 24000 + 14200);
		
		if(time != WorldTime.FIXED && getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
			worldInfo.setWorldTime(worldInfo.getWorldTime() + 1L);

		this.theProfiler.endStartSection("tickPending");
		this.tickUpdates(false);
		this.theProfiler.endStartSection("tickBlocks");
		this.func_147456_g();
		this.theProfiler.endStartSection("chunkMap");
		this.thePlayerManager.updatePlayerInstances();
		this.theProfiler.endStartSection("village");
		this.villageCollectionObj.tick();
		this.villageSiegeObj.tick();
		this.theProfiler.endStartSection("portalForcer");
		this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
		for (Teleporter tele : customTeleporters)
		{
			tele.removeStalePortalLocations(getTotalWorldTime());
		}
		this.theProfiler.endSection();
		this.func_147488_Z();
	}

	public BiomeGenBase.SpawnListEntry spawnRandomCreature(EnumCreatureType p_73057_1_, int p_73057_2_, int p_73057_3_, int p_73057_4_)
	{
		List list = this.getChunkProvider().getPossibleCreatures(p_73057_1_, p_73057_2_, p_73057_3_, p_73057_4_);
		list = ForgeEventFactory.getPotentialSpawns(this, p_73057_1_, p_73057_2_, p_73057_3_, p_73057_4_, list);
		return list != null && !list.isEmpty() ? (BiomeGenBase.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, list) : null;
	}

	public void updateAllPlayersSleepingFlag()
	{
		this.allPlayersSleeping = !this.playerEntities.isEmpty();
		Iterator iterator = this.playerEntities.iterator();

		while (iterator.hasNext())
		{
			EntityPlayer entityplayer = (EntityPlayer)iterator.next();

			if (!entityplayer.isPlayerSleeping())
			{
				this.allPlayersSleeping = false;
				break;
			}
		}
	}

	protected void wakeAllPlayers()
	{
		this.allPlayersSleeping = false;
		Iterator iterator = this.playerEntities.iterator();

		while (iterator.hasNext())
		{
			EntityPlayer entityplayer = (EntityPlayer)iterator.next();

			if (entityplayer.isPlayerSleeping())
			{
				entityplayer.wakeUpPlayer(false, false, true);
			}
		}

		this.resetRainAndThunder();
	}

	private void resetRainAndThunder()
	{
		provider.resetRainAndThunder();
	}

	public boolean areAllPlayersAsleep()
	{
		if (this.allPlayersSleeping && !this.isRemote)
		{
			Iterator iterator = this.playerEntities.iterator();
			EntityPlayer entityplayer;

			do
			{
				if (!iterator.hasNext())
				{
					return true;
				}

				entityplayer = (EntityPlayer)iterator.next();
			}
			while (entityplayer.isPlayerFullyAsleep());

			return false;
		}
		else
		{
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	public void setSpawnLocation()
	{
		if (this.worldInfo.getSpawnY() <= 0)
		{
			this.worldInfo.setSpawnY(64);
		}

		int i = this.worldInfo.getSpawnX();
		int j = this.worldInfo.getSpawnZ();
		int k = 0;

		while (this.getTopBlock(i, j).getMaterial() == Material.air)
		{
			i += this.rand.nextInt(8) - this.rand.nextInt(8);
			j += this.rand.nextInt(8) - this.rand.nextInt(8);
			++k;

			if (k == 10000)
			{
				break;
			}
		}

		this.worldInfo.setSpawnX(i);
		this.worldInfo.setSpawnZ(j);
	}

	protected void func_147456_g()
	{
		super.func_147456_g();
//		int i = 0;
//		int j = 0;

		Random random = ThreadLocalRandom.current();
		for (IntByteCursor iter = activeChunks.cursor(); iter.moveNext();)
		{
			int chunkCoord = iter.key();
			int chunkX = ChunkHash.keyToX(chunkCoord);
			int chunkZ = ChunkHash.keyToZ(chunkCoord);
			int priority = iter.value();
			int blocksPerChunk = priority < 3 ? 3 : priority < 5 ? 2 : 1;
			int k = chunkX << 4;
			int l = chunkZ << 4;
			
			this.theProfiler.startSection("getChunk");
			Chunk chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
			chunk.setActive();
			chunkProfiler.startChunk(chunkCoord);
			this.theProfiler.endStartSection("updatePending");
			this.updatePendingOf(chunk);
			this.func_147467_a(k, l, chunk);
			this.theProfiler.endStartSection("tickChunk");
			chunk.func_150804_b(false);
			this.theProfiler.endStartSection("thunder");
			int i1;
			int j1;
			int k1;
			int l1;

			if (provider.canDoLightning(chunk) && random.nextInt(100000) == 0 && this.isRaining() && this.isThundering())
			{
				this.updateLCG = this.updateLCG * 3 + 1013904223;
				i1 = this.updateLCG >> 2;
				j1 = k + (i1 & 15);
				k1 = l + (i1 >> 8 & 15);
				l1 = this.getPrecipitationHeight(j1, k1);

				if (this.canLightningStrikeAt(j1, l1, k1))
				{
					this.addWeatherEffect(new EntityLightningBolt(this, (double)j1, (double)l1, (double)k1));
				}
			}

			this.theProfiler.endStartSection("iceandsnow");

			if (provider.canDoRainSnowIce(chunk) && random.nextInt(16) == 0)
			{
				getEventProxy().pushState(WorldUpdateObjectType.WEATHER);
				this.updateLCG = this.updateLCG * 3 + 1013904223;
				i1 = this.updateLCG >> 2;
				j1 = i1 & 15;
				k1 = i1 >> 8 & 15;
				l1 = this.getPrecipitationHeight(j1 + k, k1 + l);

				if (this.isBlockFreezableNaturally(j1 + k, l1 - 1, k1 + l))
				{
					this.setBlock(j1 + k, l1 - 1, k1 + l, Blocks.ice);
				}

				if (this.isRaining() && this.func_147478_e(j1 + k, l1, k1 + l, true))
				{
					this.setBlock(j1 + k, l1, k1 + l, Blocks.snow_layer);
				}

				if (this.isRaining())
				{
					BiomeGenBase biomegenbase = this.getBiomeGenForCoords(j1 + k, k1 + l);

					if (biomegenbase.canSpawnLightningBolt())
					{
						this.getBlock(j1 + k, l1 - 1, k1 + l).fillWithRain(this, j1 + k, l1 - 1, k1 + l);
					}
				}
				getEventProxy().popState();
			}

			this.theProfiler.endStartSection("tickBlocks");
			ExtendedBlockStorage[] aextendedblockstorage = chunk.getBlockStorageArray();
			j1 = aextendedblockstorage.length;

			getEventProxy().pushState(WorldUpdateObjectType.BLOCK_RANDOM);
			for (k1 = 0; k1 < j1; ++k1)
			{
				ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[k1];

				if (extendedblockstorage != null && extendedblockstorage.getNeedsRandomTick())
				{
					for (int i3 = 0; i3 < blocksPerChunk; ++i3)
					{
						this.updateLCG = this.updateLCG * 3 + 1013904223;
						int i2 = this.updateLCG >> 2;
						int j2 = i2 & 15;
						int k2 = i2 >> 8 & 15;
						int l2 = i2 >> 16 & 15;
//						++j;
						Block block = extendedblockstorage.getBlockByExtId(j2, l2, k2);

						if (block.getTickRandomly())
						{
//							++i;
							getEventProxy().startBlock(block, j2 + k, l2 + extendedblockstorage.getYLocation(), k2 + l);
							block.updateTick(this, j2 + k, l2 + extendedblockstorage.getYLocation(), k2 + l, this.rand);
						}
					}
				}
			}
			getEventProxy().popState();

			chunkProfiler.endChunk();
			this.theProfiler.endSection();
		}
	}

	public boolean isBlockTickScheduledThisTick(int p_147477_1_, int p_147477_2_, int p_147477_3_, Block p_147477_4_)
	{
		//Это не заглушка. Этот метод может использоваться модами по назначению, все работает правильно.
		return false;
	}

	public void scheduleBlockUpdate(int p_147464_1_, int p_147464_2_, int p_147464_3_, Block p_147464_4_, int p_147464_5_)
	{
		this.scheduleBlockUpdateWithPriority(p_147464_1_, p_147464_2_, p_147464_3_, p_147464_4_, p_147464_5_, 0);
	}

	public void scheduleBlockUpdateWithPriority(int x, int y, int z, Block block, int time, int priority)
	{
		//NextTickListEntry nextticklistentry = new NextTickListEntry(x, y, z, block);
		//Keeping here as a note for future when it may be restored.
		//boolean isForced = getPersistentChunks().containsKey(new ChunkCoordIntPair(nextticklistentry.xCoord >> 4, nextticklistentry.zCoord >> 4));
		//byte b0 = isForced ? 0 : 8;
		byte b0 = 0;

		if (this.scheduledUpdatesAreImmediate && block.getMaterial() != Material.air)
		{
			if (block.func_149698_L())
			{
				b0 = 8;

				if (this.checkChunksExist(x - b0, y - b0, z - b0, x + b0, y + b0, z + b0))
				{
					Block block1 = this.getBlock(x, y, z);

					if (block1.getMaterial() != Material.air && block1 == block)
					{
						block1.updateTick(this, x, y, z, this.rand);
					}
				}

				return;
			}

			time = 1;
		}
		
		Chunk chunk = getChunkIfExists(x >> 4, z >> 4);

		if (chunk != null)
		{
			PendingBlockUpdate p = new PendingBlockUpdate(x&15, y, z&15, block, worldInfo.getWorldTotalTime() + (long)time, priority);
			p.initiator = getEventProxy().getObjectOwner();
			chunk.scheduleBlockUpdate(p, true);
		}
	}

	public void func_147446_b(int p_147446_1_, int p_147446_2_, int p_147446_3_, Block p_147446_4_, int p_147446_5_, int p_147446_6_)
	{
		//Данный метод вызывался только при загрузке чанка. Для совместимости с модами, которые неправильно используют этот метод, пытаемся запланировать обновление.
		Chunk chunk = getChunkIfExists(p_147446_1_ >> 4, p_147446_3_ >> 4);

		if (chunk != null)
		{
			PendingBlockUpdate p = new PendingBlockUpdate(p_147446_1_&15, p_147446_2_, p_147446_3_&15, p_147446_4_, worldInfo.getWorldTotalTime() + (long)p_147446_5_, p_147446_6_);
			chunk.scheduleBlockUpdate(p, true);
		}
	}

	public void updateEntities()
	{
		if (this.playerEntities.isEmpty() && (getPersistentChunks().isEmpty() || !getConfig().chunkLoading.enableChunkLoaders))
		{
			if (this.updateEntityTick++ >= 1200)
			{
				processEntityUnload();
				processTileEntityUnload();
				return;
			}
		}
		else
		{
			this.resetUpdateEntityTick();
		}

		super.updateEntities();
	}

	public void resetUpdateEntityTick()
	{
		this.updateEntityTick = 0;
	}

	public boolean tickUpdates(boolean p_72955_1_)
	{
		//Выполнение отложенных обновлений перенесено в updatePendingOf(Chunk)
		return false;
	}

	@SuppressWarnings("rawtypes")
	public List getPendingBlockUpdates(Chunk p_72920_1_, boolean p_72920_2_)
	{
		//Данный метод вызывался только при сохранении чанка. Выполняем преобразование для совместимости с модами
		Set<PendingBlockUpdate> set = p_72920_1_.getPendingUpdatesForSave();
		if(set == null)
			return null;
		int xadd = p_72920_1_.xPosition << 4;
		int zadd = p_72920_1_.zPosition << 4;
		List<NextTickListEntry> list = new ArrayList<NextTickListEntry>(set.size());
		for(PendingBlockUpdate pbu : set)
		{
			NextTickListEntry ent = new NextTickListEntry(pbu.x + xadd, pbu.y, pbu.z + zadd, pbu.getBlock());
			ent.setScheduledTime(pbu.scheduledTime).setPriority(pbu.priority);
			list.add(ent);
		}
		return list;
	}

	public void updateEntityWithOptionalForce(Entity p_72866_1_, boolean p_72866_2_)
	{
		if (!getConfig().mobSpawn.allowAnimals && (p_72866_1_ instanceof EntityAnimal || p_72866_1_ instanceof EntityWaterMob))
		{
			p_72866_1_.setDead();
		}

		if (!getConfig().mobSpawn.allowNPCs && p_72866_1_ instanceof INpc)
		{
			p_72866_1_.setDead();
		}

		super.updateEntityWithOptionalForce(p_72866_1_, p_72866_2_);
	}

	protected IChunkProvider createChunkProvider()
	{
		IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
		this.theChunkProviderServer = new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
		return this.theChunkProviderServer;
	}

	public List func_147486_a(int p_147486_1_, int p_147486_2_, int p_147486_3_, int p_147486_4_, int p_147486_5_, int p_147486_6_)
	{
		ArrayList arraylist = new ArrayList();

		for(int x = (p_147486_1_ >> 4); x <= (p_147486_4_ >> 4); x++)
		{
			for(int z = (p_147486_3_ >> 4); z <= (p_147486_6_ >> 4); z++)
			{
				Chunk chunk = getChunkFromChunkCoords(x, z);
				if (chunk != null)
				{
					for(Object obj : chunk.chunkTileEntityMap.values())
					{
						TileEntity entity = (TileEntity)obj;
						if (!entity.isInvalid())
						{
							if (entity.xCoord >= p_147486_1_ && entity.yCoord >= p_147486_2_ && entity.zCoord >= p_147486_3_ &&
								entity.xCoord <= p_147486_4_ && entity.yCoord <= p_147486_5_ && entity.zCoord <= p_147486_6_)
							{
								arraylist.add(entity);
							}
						}
					}
				}
			}
		}

		return arraylist;
	}

	public boolean canMineBlock(EntityPlayer p_72962_1_, int p_72962_2_, int p_72962_3_, int p_72962_4_)
	{
		return super.canMineBlock(p_72962_1_, p_72962_2_, p_72962_3_, p_72962_4_);
	}

	public boolean canMineBlockBody(EntityPlayer par1EntityPlayer, int par2, int par3, int par4)
	{
		return !this.mcServer.isBlockProtected(this, par2, par3, par4, par1EntityPlayer);
	}

	protected void initialize(WorldSettings p_72963_1_)
	{
		if (this.entityIdMap == null)
		{
			this.entityIdMap = new IntHashMap();
		}

		if (this.pendingTickListEntriesHashSet == null)
		{
			this.pendingTickListEntriesHashSet = new HashSet();
		}

		if (this.pendingTickListEntriesTreeSet == null)
		{
			this.pendingTickListEntriesTreeSet = new TreeSet();
		}

		setConfig(MinecraftServer.getServer().getMultiWorld().getConfigByID(provider.dimensionId));
		this.createSpawnPosition(p_72963_1_);
		super.initialize(p_72963_1_);
	}

	protected void createSpawnPosition(WorldSettings p_73052_1_)
	{
		if (!this.provider.canRespawnHere())
		{
			this.worldInfo.setSpawnPosition(0, this.provider.getAverageGroundLevel(), 0);
		}
		else
		{
			if (net.minecraftforge.event.ForgeEventFactory.onCreateWorldSpawn(this, p_73052_1_)) return;
			this.findingSpawnPoint = true;
			WorldChunkManager worldchunkmanager = this.provider.worldChunkMgr;
			List list = worldchunkmanager.getBiomesToSpawnIn();
			Random random = new Random(this.getSeed());
			ChunkPosition chunkposition = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
			int i = 0;
			int j = this.provider.getAverageGroundLevel();
			int k = 0;

			if (chunkposition != null)
			{
				i = chunkposition.chunkPosX;
				k = chunkposition.chunkPosZ;
			}
			else
			{
				logger.warn("Unable to find spawn biome");
			}

			int l = 0;

			while (!this.provider.canCoordinateBeSpawn(i, k))
			{
				i += random.nextInt(64) - random.nextInt(64);
				k += random.nextInt(64) - random.nextInt(64);
				++l;

				if (l == 1000)
				{
					break;
				}
			}

			this.worldInfo.setSpawnPosition(i, getHeightValue(i, k), k);
			this.findingSpawnPoint = false;

			if (p_73052_1_.isBonusChestEnabled())
			{
				this.createBonusChest();
			}
		}
	}

	protected void createBonusChest()
	{
		WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest(ChestGenHooks.getItems(BONUS_CHEST, rand), ChestGenHooks.getCount(BONUS_CHEST, rand));

		for (int i = 0; i < 10; ++i)
		{
			int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
			int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
			int l = this.getTopSolidOrLiquidBlock(j, k) + 1;

			if (worldgeneratorbonuschest.generate(this, this.rand, j, l, k))
			{
				break;
			}
		}
	}

	public ChunkCoordinates getEntrancePortalLocation()
	{
		return this.provider.getEntrancePortalLocation();
	}

	public void saveAllChunks(boolean p_73044_1_, IProgressUpdate p_73044_2_) throws MinecraftException
	{
		if (this.chunkProvider.canSave())
		{
			if (p_73044_2_ != null)
			{
				p_73044_2_.displayProgressMessage("Saving level");
			}

			this.saveLevel();

			if (p_73044_2_ != null)
			{
				p_73044_2_.resetProgresAndWorkingMessage("Saving chunks");
			}

			this.chunkProvider.saveChunks(p_73044_1_, p_73044_2_);
			MinecraftForge.EVENT_BUS.postWithProfile(this.theProfiler, new WorldEvent.Save(this));
		}
	}

	public void saveChunkData()
	{
		if (this.chunkProvider.canSave())
		{
			this.chunkProvider.saveExtraData();
		}
	}

	protected void saveLevel() throws MinecraftException
	{
		this.checkSessionLock();
		this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getConfigurationManager().getHostPlayerData());
		this.mapStorage.saveAllData();
		this.perWorldStorage.saveAllData();
	}

	public void onEntityAdded(Entity p_72923_1_)
	{
		super.onEntityAdded(p_72923_1_);
		this.entityIdMap.addKey(p_72923_1_.getEntityId(), p_72923_1_);
		Entity[] aentity = p_72923_1_.getParts();

		if (aentity != null)
		{
			for (int i = 0; i < aentity.length; ++i)
			{
				this.entityIdMap.addKey(aentity[i].getEntityId(), aentity[i]);
			}
		}
	}

	public void onEntityRemoved(Entity p_72847_1_)
	{
		super.onEntityRemoved(p_72847_1_);
		this.entityIdMap.removeObject(p_72847_1_.getEntityId());
		Entity[] aentity = p_72847_1_.getParts();

		if (aentity != null)
		{
			for (int i = 0; i < aentity.length; ++i)
			{
				this.entityIdMap.removeObject(aentity[i].getEntityId());
			}
		}
	}

	public Entity getEntityByID(int p_73045_1_)
	{
		return (Entity)this.entityIdMap.lookup(p_73045_1_);
	}

	public boolean addWeatherEffect(Entity p_72942_1_)
	{
		if (super.addWeatherEffect(p_72942_1_))
		{
			this.mcServer.getConfigurationManager().sendToAllNear(p_72942_1_.posX, p_72942_1_.posY, p_72942_1_.posZ, 512.0D, this.provider.dimensionId, new S2CPacketSpawnGlobalEntity(p_72942_1_));
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setEntityState(Entity p_72960_1_, byte p_72960_2_)
	{
		this.getEntityTracker().func_151248_b(p_72960_1_, new S19PacketEntityStatus(p_72960_1_, p_72960_2_));
	}

	public Explosion newExplosion(Entity p_72885_1_, double p_72885_2_, double p_72885_4_, double p_72885_6_, float p_72885_8_, boolean p_72885_9_, boolean p_72885_10_)
	{
		Explosion explosion = new Explosion(this, p_72885_1_, p_72885_2_, p_72885_4_, p_72885_6_, p_72885_8_);
		explosion.isFlaming = p_72885_9_;
		explosion.isSmoking = p_72885_10_;
		if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
		explosion.doExplosionA();
		explosion.doExplosionB(false);

		if (!p_72885_10_)
		{
			explosion.affectedBlockPositions.clear();
		}

		Iterator iterator = this.playerEntities.iterator();

		while (iterator.hasNext())
		{
			EntityPlayer entityplayer = (EntityPlayer)iterator.next();

			if (entityplayer.getDistanceSq(p_72885_2_, p_72885_4_, p_72885_6_) < 4096.0D)
			{
				((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(p_72885_2_, p_72885_4_, p_72885_6_, p_72885_8_, explosion.affectedBlockPositions, (Vec3)explosion.func_77277_b().get(entityplayer)));
			}
		}

		return explosion;
	}

	public void addBlockEvent(int p_147452_1_, int p_147452_2_, int p_147452_3_, Block p_147452_4_, int p_147452_5_, int p_147452_6_)
	{
		BlockEventData blockeventdata = new BlockEventData(p_147452_1_, p_147452_2_, p_147452_3_, p_147452_4_, p_147452_5_, p_147452_6_);
		blockeventdata.initiator = getEventProxy().getObjectOwner();
		Iterator iterator = this.field_147490_S[this.blockEventCacheIndex].iterator();
		BlockEventData blockeventdata1;

		do
		{
			if (!iterator.hasNext())
			{
				this.field_147490_S[this.blockEventCacheIndex].add(blockeventdata);
				return;
			}

			blockeventdata1 = (BlockEventData)iterator.next();
		}
		while (!blockeventdata1.equals(blockeventdata));
	}

	private void func_147488_Z()
	{
		getEventProxy().pushState(WorldUpdateObjectType.BLOCK_EVENT);
		while (!this.field_147490_S[this.blockEventCacheIndex].isEmpty())
		{
			int i = this.blockEventCacheIndex;
			this.blockEventCacheIndex ^= 1;
			Iterator iterator = this.field_147490_S[i].iterator();

			while (iterator.hasNext())
			{
				BlockEventData blockeventdata = (BlockEventData)iterator.next();
				Block block = getBlockIfExists(blockeventdata.func_151340_a(), blockeventdata.func_151342_b(), blockeventdata.func_151341_c());
				if(block == blockeventdata.getBlock())
					getEventProxy().startBlock(block, blockeventdata.func_151340_a(), blockeventdata.func_151342_b(), blockeventdata.func_151341_c(), blockeventdata.initiator);

				if (this.func_147485_a(blockeventdata))
				{
					this.mcServer.getConfigurationManager().sendToAllNear((double)blockeventdata.func_151340_a(), (double)blockeventdata.func_151342_b(), (double)blockeventdata.func_151341_c(), 64.0D, this.provider.dimensionId, new S24PacketBlockAction(blockeventdata.func_151340_a(), blockeventdata.func_151342_b(), blockeventdata.func_151341_c(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
				}
			}

			this.field_147490_S[i].clear();
		}
		getEventProxy().popState();
	}

	private boolean func_147485_a(BlockEventData p_147485_1_)
	{
		Block block = this.getBlockIfExists(p_147485_1_.func_151340_a(), p_147485_1_.func_151342_b(), p_147485_1_.func_151341_c());
		return block == p_147485_1_.getBlock() ? block.onBlockEventReceived(this, p_147485_1_.func_151340_a(), p_147485_1_.func_151342_b(), p_147485_1_.func_151341_c(), p_147485_1_.getEventID(), p_147485_1_.getEventParameter()) : false;
	}

	public void flush()
	{
		this.saveHandler.flush();
	}

	protected void updateWeather()
	{
		boolean flag = this.isRaining();
		
		switch(getConfig().settings.weather)
		{
		case NONE:
			if(flag)
			{
				worldInfo.setRainTime(12300);
				worldInfo.setThunderTime(12300);
				worldInfo.setRaining(false);
				worldInfo.setThundering(false);
				prevRainingStrength = rainingStrength = 0F;
				prevThunderingStrength = thunderingStrength = 0F;
			}
			break;
		case THUNDER:
			worldInfo.setThunderTime(12300);
			worldInfo.setThundering(true);
		case RAIN:
			worldInfo.setRainTime(12300);
			worldInfo.setRaining(true);
		case NORMAL:
			super.updateWeather();
		}

		if (this.prevRainingStrength != this.rainingStrength)
		{
			this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, this.rainingStrength), this.provider.dimensionId);
		}

		if (this.prevThunderingStrength != this.thunderingStrength)
		{
			this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, this.thunderingStrength), this.provider.dimensionId);
		}

		/*The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
		rather than to all players on the server. This is what causes the client-side rain, as the
		client believes that it has started raining locally, rather than in another dimension.
		*/
		if (flag != this.isRaining())
		{
			if (flag)
			{
				this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(2, 0.0F), provider.dimensionId);
			}
			else
			{
				this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(1, 0.0F), provider.dimensionId);
			}

			this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, this.rainingStrength), provider.dimensionId);
			this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, this.thunderingStrength), provider.dimensionId);
		}
	}

	protected int func_152379_p()
	{
		return config.chunkLoading.chunkActivateRadius;
	}

	public MinecraftServer func_73046_m()
	{
		return this.mcServer;
	}

	public EntityTracker getEntityTracker()
	{
		return this.theEntityTracker;
	}

	public PlayerManager getPlayerManager()
	{
		return this.thePlayerManager;
	}

	public Teleporter getDefaultTeleporter()
	{
		return this.worldTeleporter;
	}

	public void func_147487_a(String p_147487_1_, double p_147487_2_, double p_147487_4_, double p_147487_6_, int p_147487_8_, double p_147487_9_, double p_147487_11_, double p_147487_13_, double p_147487_15_)
	{
		S2APacketParticles s2apacketparticles = new S2APacketParticles(p_147487_1_, (float)p_147487_2_, (float)p_147487_4_, (float)p_147487_6_, (float)p_147487_9_, (float)p_147487_11_, (float)p_147487_13_, (float)p_147487_15_, p_147487_8_);

		for (int j = 0; j < this.playerEntities.size(); ++j)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntities.get(j);
			ChunkCoordinates chunkcoordinates = entityplayermp.getPlayerCoordinates();
			double d7 = p_147487_2_ - (double)chunkcoordinates.posX;
			double d8 = p_147487_4_ - (double)chunkcoordinates.posY;
			double d9 = p_147487_6_ - (double)chunkcoordinates.posZ;
			double d10 = d7 * d7 + d8 * d8 + d9 * d9;

			if (d10 <= 256.0D)
			{
				entityplayermp.playerNetServerHandler.sendPacket(s2apacketparticles);
			}
		}
	}

	public File getChunkSaveLocation()
	{
		return ((AnvilChunkLoader)theChunkProviderServer.currentChunkLoader).chunkSaveLocation;
	}

	static class ServerBlockEventList extends ArrayList
		{
			private static final String __OBFID = "CL_00001439";

			private ServerBlockEventList() {}

			ServerBlockEventList(Object p_i1521_1_)
			{
				this();
			}
		}
	
	/* ======================================== ULTRAMINE START =====================================*/
	
	private static final boolean isServer = FMLCommonHandler.instance().getSide().isServer();
	private WorldConfig config;
	@SideOnly(Side.SERVER)
	private WorldBorder border;
	@SideOnly(Side.SERVER)
	private MobSpawnManager mobSpawner;
	
	@Override
	public void checkSessionLock() throws MinecraftException
	{
		//Removes world lock checking on server
		if(!isServer)
			super.checkSessionLock();
	}
	
	@Override
	public Chunk getChunkIfExists(int cx, int cz)
	{
		return theChunkProviderServer.getChunkIfExists(cx, cz);
	}
	
	public void updatePendingOf(Chunk chunk)
	{
		long time = worldInfo.getWorldTotalTime();
		int x = chunk.xPosition << 4;
		int z = chunk.zPosition << 4;
		
		getEventProxy().pushState(WorldUpdateObjectType.BLOCK_PENDING);
		PendingBlockUpdate p;
		while((p = chunk.pollPending(time)) != null)
		{
			updateBlock(x + p.x, p.y, z + p.z, p.getBlock(), p.initiator);
		}
		getEventProxy().popState();
	}
	
	private void updateBlock(int x, int y, int z, Block block1, GameProfile initiator)
	{
		Block block = this.getBlock(x, y, z);
		getEventProxy().startBlock(block, x, y, z, initiator);

		if (block.getMaterial() != Material.air && Block.isEqualTo(block, block1))
		{
			try
			{
				block.updateTick(this, x, y, z, this.rand);
			}
			catch (Throwable throwable1)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception while ticking a block");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
				int k;

				try
				{
					k = this.getBlockMetadata(x, y, z);
				}
				catch (Throwable throwable)
				{
					k = -1;
				}

				CrashReportCategory.func_147153_a(crashreportcategory, x, y, z, block, k);
				throw new ReportedException(crashreport);
			}
		}
	}
	
	public void setConfig(WorldConfig config)
	{
		this.config = config;
		if(isServer)
			applyConfig();
	}
	
	@SideOnly(Side.SERVER)
	public void applyConfig()
	{
		difficultySetting = BasicTypeParser.parseDifficulty(config.settings.difficulty);
		setAllowedSpawnTypes(config.mobSpawn.spawnMonsters, config.mobSpawn.spawnAnimals);
		getGameRules().setOrCreateGameRule("doDaylightCycle", Boolean.toString(config.settings.time != WorldTime.FIXED));
		getGameRules().setOrCreateGameRule("doMobSpawning", Boolean.toString(config.mobSpawn.spawnEngine != WorldConfig.MobSpawn.MobSpawnEngine.NONE));

		if(config.mobSpawn.spawnEngine == WorldConfig.MobSpawn.MobSpawnEngine.NEW)
		{
			if(mobSpawner == null)
				mobSpawner = new MobSpawnManager(this);
			mobSpawner.configure(config);
		}
		
		this.border = new WorldBorder(config.borders);
		this.eventProxy = new ServerWorldEventProxy(this);
	}
	
	public WorldConfig getConfig()
	{
		return config;
	}
	
	public int getViewDistance()
	{
		return isServer ? config.chunkLoading.viewDistance : mcServer.getConfigurationManager().getViewDistance();
	}
	
	@Override
	protected boolean isChunkLoaderEnabled()
	{
		return config.chunkLoading.enableChunkLoaders;
	}
	
	@SideOnly(Side.SERVER)
	public WorldBorder getBorder()
	{
		return border;
	}
	
	public void saveOtherData()
	{
		try
		{
			saveLevel();
		} catch (MinecraftException ignored) {}
		MinecraftForge.EVENT_BUS.postWithProfile(this.theProfiler, new WorldEvent.Save(this));
	}
}