package org.ultramine.server;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;

import org.ultramine.commands.CommandRegistry;
import org.ultramine.commands.basic.FastWarpCommand;
import org.ultramine.commands.basic.GenWorldCommand;
import org.ultramine.commands.basic.TechCommands;
import org.ultramine.commands.basic.VanillaCommands;
import org.ultramine.commands.syntax.DefaultCompleters;
import org.ultramine.core.economy.service.DefaultHoldingsProvider;
import org.ultramine.core.economy.service.Economy;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.service.InjectService;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.server.chunk.AntiXRayService;
import org.ultramine.server.chunk.ChunkGenerationQueue;
import org.ultramine.server.chunk.ChunkProfiler;
import org.ultramine.server.chunk.alloc.ChunkAllocService;
import org.ultramine.server.chunk.alloc.unsafe.UnsafeChunkAlloc;
import org.ultramine.server.data.Databases;
import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.data.player.PlayerCoreData;
import org.ultramine.server.economy.UMIntegratedHoldingsProvider;
import org.ultramine.server.economy.UMEconomy;
import org.ultramine.server.economy.UMEconomyRegistry;
import org.ultramine.server.event.ForgeModIdMappingEvent;
import org.ultramine.server.internal.SyncServerExecutorImpl;
import org.ultramine.server.internal.UMEventHandler;
import org.ultramine.server.internal.OpBasedPermissions;
import org.ultramine.server.tools.ItemBlocker;
import org.ultramine.server.util.GlobalExecutors;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import org.ultramine.core.permissions.Permissions;

public class UltramineServerModContainer extends DummyModContainer
{
	private static UltramineServerModContainer instance;
	@InjectService private static ServiceManager services;
	@InjectService private static Permissions perms;
	@InjectService private static EconomyRegistry economyRegistry;

	private LoadController controller;
	private ItemBlocker itemBlocker;
	private final RecipeCache recipeCache = new RecipeCache();

	public UltramineServerModContainer()
	{
		super(new ModMetadata());
		instance = this;
	    ModMetadata meta = getMetadata();
		meta.modId		= "UltramineServer";
		meta.name		= "Ultramine Server";
		meta.version	= "@version@";
	}

	public static UltramineServerModContainer getInstance()
	{
		return instance;
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		this.controller = controller;
		bus.register(this);
		return true;
	}

	@Subscribe
	public void modConstruction(FMLConstructionEvent evt)
	{
		NetworkRegistry.INSTANCE.register(this, this.getClass(), null, evt.getASMHarvestedData());
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent e)
	{
		try
		{
			services.register(ChunkAllocService.class, new UnsafeChunkAlloc(), 0);
			services.register(AntiXRayService.class, new AntiXRayService.EmptyImpl(), 0);
			if(e.getSide().isServer())
			{
				// In the case of launch from IDE translation table is not loaded automatically
				InputStream langFile = getClass().getResourceAsStream("/assets/ultramine/lang/en_US.lang");
				if (langFile != null) StringTranslate.inject(langFile);

				ConfigurationHandler.load();
				Databases.init();
				MinecraftServer.getServer().getMultiWorld().preloadConfigs();
				ConfigurationHandler.postWorldDescsLoad();

				services.register(EconomyRegistry.class, new UMEconomyRegistry(), 0);
				services.register(Economy.class, new UMEconomy(), 0);
				services.register(DefaultHoldingsProvider.class, new UMIntegratedHoldingsProvider(), 0);
			}

			OpBasedPermissions vanPerms = new OpBasedPermissions();
			vanPerms.addDefault("command.vanilla.help");
			vanPerms.addDefault("command.vanilla.msg");
			vanPerms.addDefault("command.vanilla.reply");
			services.register(Permissions.class, vanPerms, 0);
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void init(FMLInitializationEvent e)
	{
		try
		{
			UMEventHandler handler = new UMEventHandler();
			MinecraftForge.EVENT_BUS.register(handler);
			FMLCommonHandler.instance().bus().register(handler);
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent e)
	{
		try
		{
			if(e.getSide().isServer())
				ConfigurationHandler.saveServerConfig();
			((SyncServerExecutorImpl) GlobalExecutors.nextTick()).register();
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		try
		{
			ChunkGenerationQueue.instance().register();
			e.getServer().getMultiWorld().register();
			if(e.getSide().isServer())
			{
				itemBlocker = new ItemBlocker();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void serverStarting(FMLServerStartingEvent e)
	{
		try
		{
			e.getServer().getConfigurationManager().getDataLoader().registerPlayerDataExt(PlayerCoreData.class, "core");
			e.registerArgumentHandlers(DefaultCompleters.class);
			e.registerCommands(VanillaCommands.class);
			e.registerCommands(TechCommands.class);
			e.registerCommands(GenWorldCommand.class);

			if(e.getSide().isServer())
			{
				itemBlocker.load();
				e.getServer().getScheduler().start();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void serverStarted(FMLServerStartedEvent e)
	{
		try
		{
			ServerDataLoader loader = MinecraftServer.getServer().getConfigurationManager().getDataLoader();
			CommandRegistry reg = ((CommandHandler)MinecraftServer.getServer().getCommandManager()).getRegistry();
			loader.loadCache();
			loader.addDefaultWarps();
			for(String name : loader.getFastWarps())
				reg.registerCommand(new FastWarpCommand(name));
			if(e.getSide().isServer())
			{
				getRecipeCache().setEnabled(ConfigurationHandler.getServerConfig().settings.other.recipeCacheEnabled);
				FMLCommonHandler.instance().bus().register(getRecipeCache());
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void serverStopped(FMLServerStoppedEvent e)
	{
		try
		{
			MinecraftServer.getServer().getMultiWorld().unregister();
			ChunkGenerationQueue.instance().unregister();
			ChunkProfiler.instance().setEnabled(false);
			((SyncServerExecutorImpl) GlobalExecutors.nextTick()).unregister();

			if(e.getSide().isServer())
			{
				MinecraftServer.getServer().getScheduler().stop();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void remap(FMLModIdMappingEvent e)
	{
		try
		{
			MinecraftForge.EVENT_BUS.post(new ForgeModIdMappingEvent(e));
			recipeCache.clearCache();
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@NetworkCheckHandler
	public boolean networkCheck(Map<String,String> map, Side side)
	{
		return true;
	}

	@Override
	public File getSource()
	{
		return UltraminePlugin.location;
	}

	@Override
	public List<String> getOwnedPackages()
	{
		return ImmutableList.of(
			"org.ultramine.core.service",
			"org.ultramine.server",
			"org.ultramine.commands",
			"org.ultramine.server.util"
		);
	}

	@Override
	public Object getMod()
	{
		return this;
	}

	public RecipeCache getRecipeCache()
	{
		return recipeCache;
	}

	public void reloadToolsCfg()
	{
		getRecipeCache().setEnabled(ConfigurationHandler.getServerConfig().settings.other.recipeCacheEnabled);
		itemBlocker.reload();
	}
}
