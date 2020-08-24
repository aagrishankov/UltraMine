package org.ultramine.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.util.AsyncIOUtils;
import org.ultramine.server.util.Resources;
import org.ultramine.server.util.YamlConfigProvider;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.ultramine.server.world.WorldDescriptor;

@SideOnly(Side.SERVER)
public class ConfigurationHandler
{
	private static Logger log = LogManager.getLogger();
	
	private static File settingsDir = new File(FMLLaunchHandler.getMinecraftHome(), System.getProperty("org.ultramine.dirs.settings", "settings"));
	private static File storageDir = new File(FMLLaunchHandler.getMinecraftHome(), System.getProperty("org.ultramine.dirs.storage", "storage"));
	private static File worldsDir = new File(FMLLaunchHandler.getMinecraftHome(), System.getProperty("org.ultramine.dirs.worlds", "worlds"));
	private static File vanillaConfigsDir = new File(FMLLaunchHandler.getMinecraftHome(), System.getProperty("org.ultramine.dirs.vanilla", "storage"));
	
	private static File serverConfigFile = new File(getSettingDir(), "server.yml");
	private static File worldsConfigFile = new File(getSettingDir(), "worlds.yml");
	
	private static UltramineServerConfig serverConfig;
	private static WorldsConfig worldsConfig;
	
	static
	{
		try {
			FileUtils.forceMkdir(settingsDir);
			FileUtils.forceMkdir(storageDir);
			FileUtils.forceMkdir(worldsDir);
			FileUtils.forceMkdir(vanillaConfigsDir);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create necessary server directories", e);
		}
	}
	
	public static void load()
	{
		serverConfig = YamlConfigProvider.getOrCreateConfig(serverConfigFile, UltramineServerConfig.class);
		
		if(!worldsConfigFile.exists())
		{
			String def = Resources.getAsString("/org/ultramine/defaults/defaultworlds.yml").replace("{seed}", Long.toString(Math.abs(new Random().nextLong())));
			AsyncIOUtils.writeString(worldsConfigFile, def);
			worldsConfig = YamlConfigProvider.readConfig(def, WorldsConfig.class);
		}
		else
		{
			worldsConfig = YamlConfigProvider.readConfig(worldsConfigFile, WorldsConfig.class);
		}
	}

	static void postWorldDescsLoad()
	{
		// Creating symlink ./world -> ./worlds/world for mods compatibility
		WorldDescriptor desc = MinecraftServer.getServer().getMultiWorld().getDescByID(0);
		if(desc != null)
		{
			try {
				Path vanillaDir = new File(FMLLaunchHandler.getMinecraftHome(), desc.getName()).toPath();
				Path realDir = desc.getDirectory().toPath();
				if(!vanillaDir.equals(realDir))
				{
					if(!Files.exists(realDir))
						Files.createDirectory(realDir);
					if(Files.isSymbolicLink(vanillaDir) && !Files.isSameFile(vanillaDir, realDir))
						Files.delete(vanillaDir);
					if(!Files.exists(vanillaDir))
						Files.createSymbolicLink(vanillaDir, realDir);
				}
			} catch (IOException ignored) {}

		}
	}
	
	public static File getSettingDir()
	{
		return settingsDir;
	}
	
	public static File getStorageDir()
	{
		return storageDir;
	}
	
	public static File getWorldsDir()
	{
		return worldsDir;
	}

	public static File getVanillaConfigsDir()
	{
		return vanillaConfigsDir;
	}
	
	public static UltramineServerConfig getServerConfig()
	{
		return serverConfig;
	}
	
	public static WorldsConfig getWorldsConfig()
	{
		return worldsConfig;
	}
	
	public static void saveServerConfig()
	{
		YamlConfigProvider.saveConfig(serverConfigFile, serverConfig);
	}
}
