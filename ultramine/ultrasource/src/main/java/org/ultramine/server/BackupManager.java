package org.ultramine.server;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.UltramineServerConfig.ToolsConf.AutoBackupConf;
import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.util.GlobalExecutors;
import org.ultramine.server.util.ZipUtil;
import org.ultramine.server.world.WorldDescriptor;

import com.google.common.base.Function;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class BackupManager
{
	private static final Logger log = LogManager.getLogger();
	
	private final MinecraftServer server;
	
	private long lastBackupTime;
	private boolean isBackuping;
	private AtomicBoolean backupCompleted = new AtomicBoolean(false);
	private AtomicReference<Runnable> backupApplied = new AtomicReference<Runnable>();
	
	public BackupManager(MinecraftServer server)
	{
		this.server = server;
		this.lastBackupTime = server.startTime;
	}
	
	public void tick()
	{
		if(backupCompleted.get())
		{
			backupCompleted.set(false);
			for(WorldServer world : server.getMultiWorld().getLoadedWorlds())
				world.theChunkProviderServer.resumeSaving();

			isBackuping = false;
		}
		
		Runnable run = backupApplied.get();
		if(run != null)
		{
			backupApplied.set(null);
			run.run();
			isBackuping = false;
		}
		
		AutoBackupConf conf = ConfigurationHandler.getServerConfig().tools.autobackup;
		if(conf.enabled && (System.currentTimeMillis() - lastBackupTime >= conf.interval*60*1000) && !isBackuping)
		{
			lastBackupTime = System.currentTimeMillis();
			File dir = server.getBackupDir();
			List<BackupDescriptor> list = getBackupList();
			
			if(conf.maxBackups != -1)
				while(list.size() >= conf.maxBackups)
					FileUtils.deleteQuietly(new File(dir, list.remove(0).getName()));
			while(conf.maxDirSize != -1 && conf.maxDirSize > FileUtils.sizeOfDirectory(dir) && list.size() != 0)
				FileUtils.deleteQuietly(new File(dir, list.remove(0).getName()));
			
			if(conf.notifyPlayers)
				server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("ultramine.autobackup.start")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
			
			if(conf.worlds == null)
				backupAll();
			else
				backup(conf.worlds);
		}
	}
	
	public List<BackupDescriptor> getBackupList()
	{
		List<BackupDescriptor> list = new ArrayList<BackupDescriptor>();
		for(File file : server.getBackupDir().listFiles())
		{
			if(file.getName().endsWith(".zip"))
				list.add(new BackupDescriptor(file));
		}
		Collections.sort(list);
		return list;
	}

	public void backupWorldsSyncUnsafe() throws IOException
	{
		String filename = backupWorldDirs(server.getMultiWorld().getDirsForBackup());
		log.info("World backup created {}", filename);
	}
	
	private String backupWorldDirs(Collection<String> worlds) throws IOException
	{
		String zipname = String.format("%1$tY.%1$tm.%1$td_%1$tH-%1$tM-%1$tS.zip", System.currentTimeMillis());
		File zip = new File(server.getBackupDir(), zipname);
		File worldsDir = FMLCommonHandler.instance().getSavesDirectory();
		ZipUtil.zipAll(zip, worldsDir, worlds);
		return zip.getName();
	}
	
	public void backupAll()
	{
		backup(server.getMultiWorld().getDirsForBackup());
	}
	
	public void backup(final Collection<String> dirs)
	{
		if(isBackuping)
			throw new IllegalStateException("Already backuping");
		isBackuping = true;
		log.info("Starting backup, saving worlds");
		server.getConfigurationManager().saveAllPlayerData();
		for(WorldServer world : server.getMultiWorld().getLoadedWorlds())
		{
			if(dirs.contains(server.getMultiWorld().getSaveDirName(world)))
			{
				world.theChunkProviderServer.saveChunks(true, null);
				MinecraftForge.EVENT_BUS.postWithProfile(world.theProfiler, new WorldEvent.Save(world));
			}
			world.theChunkProviderServer.preventSaving();
		}
		
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ThreadedFileIOBase.threadedIOInstance.waitForFinish();
				} catch (InterruptedException ignored){}

				RegionFileCache.clearRegionFileReferences();
				
				log.info("Worlds saved, making backup");
				
				try
				{
					String filename = backupWorldDirs(dirs);
					log.info("World backup completed {}", filename);
				}
				catch(Throwable e)
				{
					log.error("Failed to make backup", e);
				}
				
				backupCompleted.set(true);
			}
		});
	}
	
	public void applyBackup(String path) throws CommandException
	{
		applyBackup(path, null, null, true, false);
	}
	
	//Адовы костыли с CommandContext и CommandException. Нужна универсальность без привязки к системе команд
	public void applyBackup(String path, final CommandContext ctx, List<String> moveOnlyList, boolean movePlayersP, final boolean makeTemp) throws CommandException
	{
		if(isBackuping)
			throw new IllegalStateException("Already backuping");
		isBackuping = true;
		final boolean movePlayers = movePlayersP && server.getConfigurationManager().getDataLoader().getDataProvider().isUsingWorldPlayerDir();
		final File zipFile = new File(server.getBackupDir(), path);
		try
		{
			if(!zipFile.getCanonicalPath().startsWith(server.getBackupDir().getCanonicalFile().getParentFile().getParent()))
				throw new CommandException("command.backup.apply.fail.illegalaccess");
		}catch(IOException e){throw new RuntimeException(e);}
		
		if(!zipFile.exists() || zipFile.isDirectory() || !zipFile.getName().endsWith(".zip"))
			throw new CommandException("command.backup.apply.fail.nofile", path);
		
		final Set<String> moveOnly;
		try
		{
			Set<String> available = ZipUtil.getRootFiles(zipFile);
			if(moveOnlyList == null)
			{
				moveOnly = new HashSet<String>(available);
			}
			else
			{
				moveOnly = new HashSet<String>(moveOnlyList);
				moveOnly.retainAll(available);
			}
		}
		catch(IOException e)
		{
			log.error("Failed to apply backup (read zip file)", e);
			throw new CommandException("command.backup.apply.fail.zip.read", path);
		}
		
		if(moveOnly.size() == 0)
			throw new RuntimeException("command.backup.apply.fail.nothing");
		else if(ctx != null)
			ctx.sendMessage("command.backup.apply.started", moveOnly);
		
		final Runnable afterUnpack = new Runnable()
		{
			@Override
			public void run()
			{
				if(makeTemp)
				{
					if(ctx != null)
						ctx.sendMessage("command.backup.apply.success.temp");
					for(File file : server.getWorldsDir().listFiles())
					{
						String name = file.getName();
						if(name.startsWith("unpack_"))
						{
							int dim = server.getMultiWorld().allocTempDim();
							name = "temp_"+dim+"_"+name.substring(7);
							file.renameTo(new File(server.getWorldsDir(), name));
							server.getMultiWorld().makeTempWorld(name, dim);
							if(ctx != null)
								ctx.sendMessage("    - [%s](%s)", dim, name);
						}
					}
				}
				else
				{
					TIntObjectMap<List<EntityPlayerMP>> dimToPlayerMap = new TIntObjectHashMap<List<EntityPlayerMP>>();
					
					List<WorldServer> worlds = new ArrayList<WorldServer>(server.getMultiWorld().getLoadedWorlds());
					for(WorldServer world : worlds)
					{
						if(!moveOnly.contains(server.getMultiWorld().getSaveDirName(world)))
								continue;
						WorldDescriptor desc = server.getMultiWorld().getDescFromWorld(world);
						List<EntityPlayerMP> players = desc.extractPlayers();
						desc.unloadNow(false);
						dimToPlayerMap.put(world.provider.dimensionId, players);
					}
					
					try
					{
						ThreadedFileIOBase.threadedIOInstance.waitForFinish();
					} catch (InterruptedException ignored){}

					RegionFileCache.clearRegionFileReferences();
					
					for(String world : moveOnly)
					{
						try
						{
							File unpacked = new File(server.getWorldsDir(), "unpack_"+world);
							if(!unpacked.exists())
								continue;
							File worldDir = new File(server.getWorldsDir(), world);
							if(movePlayers)
							{
								if(worldDir.exists())
									FileUtils.deleteDirectory(worldDir);
								FileUtils.moveDirectory(unpacked, worldDir);
							}
							else
							{
								if(worldDir.exists())
								{
									for(File file : worldDir.listFiles())
									{
										if(!file.getName().equals("playerdata"))
											FileUtils.forceDelete(file);
									}
								}
								else
								{
									worldDir.mkdir();
								}
								for(File file : unpacked.listFiles())
								{
									if(!file.getName().equals("playerdata"))
										FileUtils.moveToDirectory(file, worldDir, false);
								}
								FileUtils.deleteDirectory(unpacked);
							}
						}
						catch(IOException e)
						{
							log.warn("Failed to delete or rename world directory ("+world+") on backup applying", e);
							if(ctx != null)
								ctx.sendMessage(EnumChatFormatting.RED, "command.backup.apply.fail.rmdir", world);
						}
					}
					
					boolean backOverworld = dimToPlayerMap.containsKey(0);
					if(backOverworld)
						server.getMultiWorld().initDimension(0); //overworld first
					for(TIntObjectIterator<List<EntityPlayerMP>> it = dimToPlayerMap.iterator(); it.hasNext();)
					{
						it.advance();
						int dim = it.key();
						if(dim != 0)
							DimensionManager.initDimension(dim);
					}
					
					ServerDataLoader loader = server.getConfigurationManager().getDataLoader();
					if(movePlayers && backOverworld) //global player data reload
					{
						loader.reloadPlayerCache();
						for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(server.getConfigurationManager().playerEntityList, EntityPlayerMP.class))
							reloadPlayer(player);
					}
					
					for(TIntObjectIterator<List<EntityPlayerMP>> it = dimToPlayerMap.iterator(); it.hasNext();)
					{
						it.advance();
						int dim = it.key();
						WorldServer world = server.getMultiWorld().getWorldByID(dim);
						for(EntityPlayerMP player : it.value())
						{
							if(player.worldObj == null)
								player.setWorld(world);
							if(movePlayers)
							{
								if(server.getMultiWorld().getIsolatedDataDims().contains(dim))
									reloadPlayer(player);
							}
							else
							{
								world.spawnEntityInWorld(player);
								world.getPlayerManager().addPlayer(player);
								player.theItemInWorldManager.setWorld(world);
							}
						}
					}
					
					if(ctx != null)
						ctx.sendMessage("command.backup.apply.success");
				}
			}
		};
		
		GlobalExecutors.cachedIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final List<String> moveOnlyPaths = new ArrayList<String>(moveOnly.size());
					for(String s : moveOnly)
						moveOnlyPaths.add(s + '/');
					ZipUtil.unzip(zipFile, server.getWorldsDir(), new Function<String, String>()
					{
						@Override
						public String apply(String name)
						{
							boolean contains = false;
							for(String s : moveOnlyPaths)
							{
								if(name.startsWith(s))
								{
									contains = true;
									break;
								}
							}
							if(!contains)
								return null;
							if(name.endsWith("/session.lock"))
								return null;
							if(!movePlayers && name.contains("/playerdata/"))
								return null;
							return "unpack_" + name;
						}
					});
					
					backupApplied.set(afterUnpack);
				}
				catch(IOException e)
				{
					log.error("Failed to apply backup (unpack or write files)", e);
					if(ctx != null)
						ctx.sendMessage(EnumChatFormatting.RED, "command.backup.apply.fail.zip.unpack");
				}
			}
		});
	}
	
	private void reloadPlayer(EntityPlayerMP player)
	{
		int curdim = player.dimension;
		WorldServer world = server.getMultiWorld().getWorldByID(curdim);
		player.setWorld(world);
		server.getConfigurationManager().getDataLoader().syncReloadPlayer(player);
		int newdim = player.dimension;
		if(newdim != curdim)
		{
			player.dimension = curdim;
			player.transferToDimension(newdim);
		}
		else
		{
			world.spawnEntityInWorld(player);
			world.getPlayerManager().addPlayer(player);
			player.theItemInWorldManager.setWorld(world);
			player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
			server.getConfigurationManager().updateTimeAndWeatherForPlayer(player, world);
			server.getConfigurationManager().syncPlayerInventory(player);

			for(PotionEffect eff : GenericIterableFactory.newCastingIterable(player.getActivePotionEffects(), PotionEffect.class))
				player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), eff));
		}
	}
	
	public static class BackupDescriptor implements Comparable<BackupDescriptor>
	{
		private final String name;
		private final long time;
		
		private BackupDescriptor(String name, long time)
		{
			this.name = name;
			this.time = time;
		}
		
		private BackupDescriptor(File file)
		{
			this(file.getName(), file.lastModified());
		}

		@Override
		public int compareTo(BackupDescriptor b)
		{
			return Long.compare(time, b.time);
		}
		
		public String getName()
		{
			return name;
		}
	}
}
