package org.ultramine.server.world.load;

import java.io.File;
import java.io.IOException;

import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.world.WorldDescriptor;
import org.ultramine.server.world.imprt.DirectorySaveHandler;
import org.ultramine.server.world.imprt.ImportSaveHandler;
import org.ultramine.server.world.imprt.ZipFileSaveHandler;

import net.minecraft.server.MinecraftServer;

public class ImportWorldLoader extends SplittedWorldLoader
{
	private ImportSaveHandler saveHandler;
	
	public ImportWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	protected ImportSaveHandler getSaveHandler()
	{
		if(saveHandler != null)
			return saveHandler;
		WorldConfig config = desc.getConfig();
		if(config.importFrom == null)
			throw new RuntimeException("config.importFrom == null");
		
		File file = new File(server.getHomeDirectory(), config.importFrom.file);
		if(!file.exists())
			throw new RuntimeException("File not found: "+file.getAbsolutePath());
		if(file.isDirectory())
			return saveHandler = DirectorySaveHandler.create(desc.getName(), file);
		else
			return saveHandler = ZipFileSaveHandler.create(desc.getName(), file, config.importFrom.pathInArchive);
	}
	
	@Override
	public boolean hasAsyncLoadPhase()
	{
		return getSaveHandler().shouldUnpack();
	}
	
	@Override
	public void doAsyncLoadPhase()
	{
		try
		{
			saveHandler.unpackIfNecessary();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void dispose()
	{
		saveHandler.close();
	}
}
