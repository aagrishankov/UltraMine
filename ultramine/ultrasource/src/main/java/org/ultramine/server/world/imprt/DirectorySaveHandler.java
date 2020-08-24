package org.ultramine.server.world.imprt;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.minecraft.server.MinecraftServer;

public class DirectorySaveHandler extends ImportSaveHandler
{
	private File fromDir;

	protected DirectorySaveHandler(boolean tempDirExists, boolean tempDirEmpty, String dirname, File fromDir)
	{
		super(tempDirExists, tempDirEmpty, dirname);
		this.fromDir = fromDir;
	}
	
	public static DirectorySaveHandler create(String dirname, File dir)
	{
		File tempDir = new File(MinecraftServer.getServer().getWorldsDir(), dirname);
		boolean exists = tempDir.exists();
		return new DirectorySaveHandler(exists, exists && tempDir.list().length == 0, dirname, dir);
	}

	@Override
	protected ImportChunkLoader createChunkLoader() throws IOException
	{
		return new DirectoryChunkLoader(tempDir, fromDir);
	}

	@Override
	protected void unpackExceptRegions() throws IOException
	{
		FileUtils.forceMkdir(tempDir);
		if(!fromDir.isDirectory())
			throw new IOException(fromDir.getAbsolutePath() + " is not a directory!");
		for(File file : fromDir.listFiles())
		{
			String name = file.getName();
			if(!name.equals("region") && !name.startsWith("DIM"))
			{
				if(file.isFile())
					FileUtils.copyFileToDirectory(file, tempDir);
				else
					FileUtils.copyDirectoryToDirectory(file, tempDir);
			}
		}
	}
}
