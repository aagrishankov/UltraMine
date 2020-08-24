package org.ultramine.server.world.load;

import org.ultramine.server.world.WorldDescriptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class OverworldLoader extends AbstractWorldLoader
{
	public OverworldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	public WorldServer doLoad()
	{
		ISaveFormat format = server.getActiveAnvilConverter();
		ISaveHandler mainSaveHandler = format.getSaveLoader(desc.getName(), true);
		WorldInfo mainWorldInfo = mainSaveHandler.loadWorldInfo();
		WorldSettings mainSettings = makeSettings(mainWorldInfo, desc.getConfig());
		
		return new WorldServer(server, mainSaveHandler, desc.getName(), desc.getDimension(), mainSettings, server.theProfiler);
	}
}
