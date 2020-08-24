package org.ultramine.server;

import java.util.ArrayList;
import java.util.List;

public class WorldsConfig
{
	public WorldConfig global = new WorldConfig();
	public List<WorldConfig> worlds = new ArrayList<WorldConfig>();
	
	public static class WorldConfig
	{
		public int dimension;
		public String name;
		public ImportFrom importFrom;
		public Generation generation;
		public MobSpawn mobSpawn;
		public Settings settings;
		public Border[] borders;
		public ChunkLoading chunkLoading;
		public LoadBalancer loadBalancer;
		public Portals portals = new Portals();
		
		public static class ImportFrom
		{
			public String file;
			public String pathInArchive;
		}
		
		public static class Generation
		{
			public String seed;
			public int providerID = 0;
			public String levelType = "DEFAULT";
			public String generatorSettings = "";
			public boolean generateStructures = true;
			public boolean disableModGeneration = false;
			public List<String> modGenerationBlackList;
		}
		
		public static class MobSpawn
		{
			public boolean allowAnimals = true;
			public boolean spawnAnimals = true;
			public boolean spawnMonsters = true;
			public boolean allowNPCs = true;
			public MobSpawnEngine spawnEngine = MobSpawnEngine.OLD;
			public NewEngineSettings newEngineSettings;
			
			public static enum MobSpawnEngine
			{
				OLD, NEW, NONE
			}
			
			public static class NewEngineSettings
			{
				public MonsterSettings monsters;
				public PerTypeMobSpawnSettings animals;
				public PerTypeMobSpawnSettings water;
				public PerTypeMobSpawnSettings ambient;
				
				public static class PerTypeMobSpawnSettings
				{
					public boolean enabled;
					public int minRadius;
					public int maxRadius;
					public int minPlayerDistance;
					public int performInterval;
					public int localCheckRadius;
					public int localLimit;
				}
				
				public static class MonsterSettings extends PerTypeMobSpawnSettings
				{
					public int nightlyLocalLimit;
				}
			}
		}
		
		public static class Settings
		{
			public String difficulty = "1";
			public boolean pvp = true;
			public int maxBuildHeight = 256;
			public WorldTime time = WorldTime.NORMAL;
			public Weather weather = Weather.NORMAL;
			public boolean useIsolatedPlayerData = false;
			public String respawnOnWarp = null;
			public String reconnectOnWarp = null;
			public boolean fastLeafDecay = false;
			
			public enum WorldTime
			{
				NORMAL, DAY, NIGHT, FIXED
			}
			
			public enum Weather
			{
				NORMAL, NONE, RAIN, THUNDER
			}
		}
		
		public static class Border
		{
			public int x;
			public int z;
			public int radius;
			public boolean round;
		}
		
		public static class ChunkLoading
		{
			public int viewDistance  = 10;
			public int chunkActivateRadius = 7;
			public int chunkCacheSize;
			public boolean enableChunkLoaders = true;
			public int maxSendRate = 4;
		}
		
		public static class LoadBalancer
		{
			public Limits limits;
			public static class Limits
			{
				public PerChunkEntityLimits monsters;
				public PerChunkEntityLimits animals;
				public PerChunkEntityLimits water;
				public PerChunkEntityLimits ambient;
				public PerChunkEntityLimits items;
				public PerChunkEntityLimits xpOrbs;
				
				public static class PerChunkEntityLimits
				{
					public int updateRadius;
					public boolean updateByChunkLoader;
					public int lowerLimit;
					public int higherLimit;
				}
			}
		}
		
		public static class Portals
		{
			public int netherLink = Integer.MIN_VALUE;
			public int enderLink = Integer.MIN_VALUE;
		}
	}
}
