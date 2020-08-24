package org.ultramine.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UltramineServerConfig
{
	public ListenConf listen = new ListenConf();
	public SettingsConf settings = new SettingsConf();
	public ToolsConf tools = new ToolsConf();
	public Map<String, DatabaseConf> databases = new HashMap<String, DatabaseConf>();
	public VanillaConf vanilla = new VanillaConf();

	public static class ListenConf
	{
		public MinecraftConf minecraft = new MinecraftConf();
		public QueryConf query = new QueryConf();
		public RConConf rcon = new RConConf();

		public static class MinecraftConf
		{
			public String serverIP = "";
			public int port = 25565;
		}

		public static class QueryConf
		{
			public boolean enabled = false;
			public int port = 25565;
		}

		public static class RConConf
		{
			public boolean enabled = false;
			public int port = 25565;
			public String password = "";
			public List<String> whitelist = null;
		}
	}

	public static class SettingsConf
	{
		public AuthorizationConf authorization = new AuthorizationConf();
		public PlayerConf player = new PlayerConf();
		public OtherConf other = new OtherConf();
		public SpawnLocationsConf spawnLocations = new SpawnLocationsConf();
		public TeleportationConf teleportation = new TeleportationConf();
		public MessagesConf messages = new MessagesConf();
		public WatchdogThreadConf	watchdogThread = new WatchdogThreadConf();
		public SQLServerStorageConf inSQLServerStorage = new SQLServerStorageConf();
		public SecurityConf security = new SecurityConf();

		public static class AuthorizationConf
		{
			public boolean onlineMode = true;
		}

		public static class PlayerConf
		{
			public int playerIdleTimeout = 0;
			public int gamemode = 0;
			public int maxPlayers = 20;
			public boolean forceGamemode = false;
			public boolean whiteList = false;
		}

		public static class OtherConf
		{
			public boolean snooperEnabled = true;
			public boolean hardcore = false;
			public String resourcePack = "";
			public boolean enableCommandBlock = false;
			public boolean splitWorldDirs = true;
			public boolean recipeCacheEnabled = true;
		}

		public static class SpawnLocationsConf
		{
			public String firstSpawn = "spawn";
			public String deathSpawn = "spawn";
			public boolean respawnOnBed = true;
		}

		public static class TeleportationConf
		{
			public int cooldown = 60;
			public int delay = 5;
			public boolean interWorldHome = true;
			public boolean interWorldWarp = true;
		}

		public static class MessagesConf
		{
			public boolean announcePlayerAchievements = true;
			public String motd = "A Minecraft Server";
		}

		public static class WatchdogThreadConf
		{
			public int timeout = 120;
			public boolean restart = true;
		}

		public static class SQLServerStorageConf
		{
			public boolean enabled = false;
			public String database = "global";
			public String tablePrefix = "mc_";
		}
		
		public static class SecurityConf
		{
			public boolean allowFlight = false;
			public boolean checkBreakSpeed = true;
		}
	}
	
	public static class ToolsConf
	{
		public AutoBroacastConf autobroadcast = new AutoBroacastConf();
		public AutoDebugInfoConf autoDebugInfo = new AutoDebugInfoConf();
		public AutoBackupConf autobackup = new AutoBackupConf();
		public EconomyConf economy = new EconomyConf();
		
		public static class AutoBroacastConf
		{
			public boolean enabled = false;
			public int intervalSeconds = 600;
			public String[] messages = new String[0];
			public boolean showAllMessages = false;
		}
		
		public static class AutoDebugInfoConf
		{
			public boolean enabled = false;
			public int intervalSeconds = 600;
		}
		
		public static class AutoBackupConf
		{
			public boolean enabled = false;
			public int interval = 60; //minutes
			public int maxBackups = 10;
			public int maxDirSize = 50000; //megabytes
			public List<String> worlds = null;
			public boolean notifyPlayers = true;
		}
		
		public static class WarpProtectionEntry
		{
			public String name;
			public int radius;
			public boolean changeBlocks;
			public boolean useItems;
			public boolean userBlocks;
			public boolean pvp;
			public boolean mobSpawn;
			public boolean mobDamage;
		}
		
		public static class EconomyConf
		{
			public double startBalance = 30;
		}
	}

	public static class DatabaseConf
	{
		public String url; //jdbc:mysql://localhost:3306/databasename
		public String username;
		public String password;
		public int maxConnections;
	}

	public static class VanillaConf
	{
		public Map<String, Object> unresolved = new LinkedHashMap<String, Object>();
	}
}
