package org.ultramine.server.world.load;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.world.WorldDescriptor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.WorldInfo;

public abstract class AbstractWorldLoader implements IWorldLoader
{
	private static final Logger log = LogManager.getLogger();
	protected final WorldDescriptor desc;
	protected final MinecraftServer server;
	
	protected AbstractWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		this.desc = desc;
		this.server = server;
	}
	
	@Override
	public boolean hasAsyncLoadPhase()
	{
		return false;
	}
	
	@Override
	public void doAsyncLoadPhase()
	{
		
	}
	
	@Override
	public void dispose()
	{
		RegionFileCache.clearRegionFileReferences();
	}
	
	@SideOnly(Side.SERVER)
	protected WorldSettings makeSettings(WorldInfo wi, WorldConfig conf)
	{
		WorldSettings settings;

		if (wi == null)
		{
			WorldType levelType = WorldType.parseWorldType(conf.generation.levelType);
			if(levelType == null)
				throw new RuntimeException("Unknown level type \""+conf.generation.levelType+"\"");
			settings = new WorldSettings(toSeed(conf.generation.seed), server.getGameType(), conf.generation.generateStructures,
					server.isHardcore(), levelType);
			settings.func_82750_a(conf.generation.generatorSettings);
		}
		else
		{
			settings = new WorldSettings(wi);

			if(wi.getSeed() != toSeed(conf.generation.seed))
				logUnequalOption("Random seed", wi.getSeed());
			if(wi.getGameType() != server.getGameType())
				logUnequalOption("Game type", wi.getGameType());
			if(wi.isMapFeaturesEnabled() != conf.generation.generateStructures)
				logUnequalOption("'generateStructures' flag", wi.isMapFeaturesEnabled());
			if(wi.isHardcoreModeEnabled() != server.isHardcore())
				logUnequalOption("'hardcore' flag", wi.isHardcoreModeEnabled());
			if(!wi.getGeneratorOptions().equals(conf.generation.generatorSettings))
				logUnequalOption("Generator settings", wi.getGeneratorOptions());
			WorldType levelType = WorldType.parseWorldType(conf.generation.levelType);
			if(levelType == null || !wi.getTerrainType().getWorldTypeName().equals(levelType.getWorldTypeName()))
				logUnequalOption("Generator settings", wi.getTerrainType().getWorldTypeName());
		}
		
		return settings;
	}
	
	protected static long toSeed(String seedstr)
	{
		try
		{
			return Long.parseLong(seedstr);
		}
		catch (NumberFormatException e)
		{
			return seedstr.hashCode();
		}
	}

	private void logUnequalOption(String optionName, Object value)
	{
		log.warn("{} of world [{}]({}) in level.dat and worlds.yml is not equal. Using value from level.dat: {}",
				optionName, desc.getDimension(), desc.getName(), value);
	}
}
