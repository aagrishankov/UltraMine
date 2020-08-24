package org.ultramine.server.world.load;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import org.ultramine.server.world.WorldDescriptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class NotSplittedWorldLoader extends AbstractWorldLoader
{
	public NotSplittedWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	public WorldServer doLoad()
	{
		WorldServer mainWorld = server.getMultiWorld().getWorldByID(0);
		ISaveHandler mainSaveHandler = mainWorld.getSaveHandler();
		String name = desc.getDirectory().getName();
		AnvilSaveHandler save = new AnvilSaveHandler(mainSaveHandler.getWorldDirectory(), name, true);
		save.setSingleStorage();
		return new WorldServerMulti(
				server,
				save,
				name,
				desc.getDimension(),
				makeSettings(save.loadWorldInfo(), desc.getConfig()),
				mainWorld,
				server.theProfiler
		);
	}
}
