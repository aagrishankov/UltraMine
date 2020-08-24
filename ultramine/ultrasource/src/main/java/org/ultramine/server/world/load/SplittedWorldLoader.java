package org.ultramine.server.world.load;

import net.minecraft.world.WorldServerMulti;
import org.ultramine.server.world.WorldDescriptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;

public class SplittedWorldLoader extends AbstractWorldLoader
{
	public SplittedWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	public WorldServer doLoad()
	{
		WorldServer mainWorld = server.getMultiWorld().getWorldByID(0);
		ISaveHandler save = getSaveHandler();
		((AnvilSaveHandler)save).setSingleStorage();
		WorldServerMulti world = new WorldServerMulti(
				server,
				save,
				desc.getName(),
				desc.getDimension(),
				makeSettings(save.loadWorldInfo(), desc.getConfig()),
				mainWorld,
				server.theProfiler
		);
		world.isSplitted = true;
		return world;
	}
	
	protected ISaveHandler getSaveHandler()
	{
		return server.getActiveAnvilConverter().getSaveLoader(desc.getName(), true);
	}
}
