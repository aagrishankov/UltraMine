package org.ultramine.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AsyncIOUtils
{
	private static final Logger log = LogManager.getLogger();
	
	public static void writeString(final File file, final String data)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileUtils.writeStringToFile(file, data, Charsets.UTF_8);
				}
				catch(Exception e)
				{
					log.error("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}
	
	public static void writeBytes(final File file, final byte[] data)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					FileUtils.writeByteArrayToFile(file, data);
				}
				catch(Exception e)
				{
					log.error("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}

	public static void safeWriteNBT(final File file, final NBTTagCompound nbt)
	{
		safeWriteNBT(file, nbt, null);
	}
	
	public static void safeWriteNBT(final File file, final NBTTagCompound nbt, Runnable callback)
	{
		GlobalExecutors.writingIO().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					File file1 = new File(file.getParentFile(), file.getName()+".tmp");
					CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file1));

					if (file.exists())
						FileUtils.forceDelete(file);
					FileUtils.moveFile(file1, file);
					if(callback != null)
						callback.run();
				}
				catch(Exception e)
				{
					log.error("Failed to write file: "+file.getAbsolutePath(), e);
				}
			}
		});
	}
}
