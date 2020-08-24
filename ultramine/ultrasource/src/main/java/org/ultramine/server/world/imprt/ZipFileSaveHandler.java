package org.ultramine.server.world.imprt;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ultramine.server.util.ZipUtil;

import com.google.common.base.Function;

import net.minecraft.server.MinecraftServer;

public class ZipFileSaveHandler extends ImportSaveHandler
{
	private final File file;
	private final String path;
	
	private ZipFileSaveHandler(boolean tempDirExists, boolean tempDirEmpty, String dirname, File file, String path)
	{
		super(tempDirExists, tempDirEmpty, dirname);
		this.file = file;
		this.path = path;
		ZipFileChunkLoader.checkZipFile(file, path);
	}
	
	public static ZipFileSaveHandler create(String dirname, File file, String path)
	{
		File tempDir = new File(MinecraftServer.getServer().getWorldsDir(), dirname);
		boolean exists = tempDir.exists();
		return new ZipFileSaveHandler(exists, exists && tempDir.list().length == 0, dirname, file, path);
	}

	@Override
	protected ImportChunkLoader createChunkLoader() throws IOException
	{
		return new ZipFileChunkLoader(tempDir, file, path);
	}

	@Override
	protected void unpackExceptRegions() throws IOException
	{
		FileUtils.forceMkdir(tempDir);
		final String pathstart = path+"/";
		final String exclude1 = path+"/region";
		final String exclude2 = path+"/DIM";
		ZipUtil.unzip(file, tempDir, new Function<String, String>()
		{
			@Override
			public String apply(String name)
			{
				if(!name.startsWith(pathstart))
					return null;
				if(name.startsWith(exclude1) || name.startsWith(exclude2))
					return null;
				return name.substring(pathstart.length());
			}
		});
	}
}
