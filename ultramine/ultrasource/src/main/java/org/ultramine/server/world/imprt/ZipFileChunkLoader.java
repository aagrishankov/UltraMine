package org.ultramine.server.world.imprt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.ultramine.server.util.ZipUtil;

public class ZipFileChunkLoader extends ImportChunkLoader
{
	private final ZipFile zip;
	private final String path;
	
	public ZipFileChunkLoader(File tempDir, File file, String path) throws ZipException, IOException
	{
		super(tempDir);
		this.zip = new ZipFile(file);
		checkZipFile(zip, path);
		if(!path.isEmpty() && !path.endsWith("/"))
				path += "/";
		this.path = path;
	}
	
	@Override
	protected void unpackFile(String name) throws IOException
	{
		ZipEntry ent = zip.getEntry(path+name);
		if(ent == null)
			return;
		InputStream is = null;
		try
		{
			is = zip.getInputStream(ent);
			FileUtils.copyInputStreamToFile(is, new File(tempDir, name));
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public synchronized void close()
	{
		IOUtils.closeQuietly(zip);
		super.close();
	}
	
	public static void checkZipFile(ZipFile zip, String path)
	{
		Set<String> roots = ZipUtil.getRootFiles(zip);
		if(!path.isEmpty() && !roots.contains(path) || path.isEmpty() && !roots.contains("region"))
			throw new RuntimeException("Path not found in zip hierarchy: " + path);
	}
	
	public static void checkZipFile(File file, String path)
	{
		ZipFile zip = null;
		try
		{
			checkZipFile(zip = new ZipFile(file), path);
		} catch(IOException e) {
			throw new RuntimeException("Failed to open zip archive: "+file.getAbsolutePath(), e);
		} finally {
			if(zip != null)
				try{zip.close();}catch(IOException ignored){}
		}
	}
}
