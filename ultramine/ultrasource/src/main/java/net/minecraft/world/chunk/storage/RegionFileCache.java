package net.minecraft.world.chunk.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.ultramine.server.internal.LambdaHolder;
import org.ultramine.server.util.CachedEntry;
import org.ultramine.server.util.CollectionUtil;

public class RegionFileCache
{
	private static final Map<File, CachedEntry<RegionFile>> regionsByFilenameUM = new HashMap<>();
	private static final Map regionsByFilename = Maps.transformValues(regionsByFilenameUM, LambdaHolder.cachedEntryGetValueGuavaFunc());
	private static final String __OBFID = "CL_00000383";

	public static synchronized RegionFile createOrLoadRegionFile(File p_76550_0_, int p_76550_1_, int p_76550_2_)
	{
		File file2 = new File(p_76550_0_, "region");
		File file3 = new File(file2, "r." + (p_76550_1_ >> 5) + "." + (p_76550_2_ >> 5) + ".mca");
		RegionFile regionfile = (RegionFile)regionsByFilename.get(file3);

		if (regionfile != null)
		{
			return regionfile;
		}
		else
		{
			if (!file2.exists())
			{
				file2.mkdirs();
			}

			if (regionsByFilename.size() >= 256)
			{
				for(CachedEntry<RegionFile> entry : CollectionUtil.retainNewestEntries(regionsByFilenameUM.values(), 128))
				{
					try
					{
						entry.getValueAndUpdateTime().close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			RegionFile regionfile1 = new RegionFile(file3);
			regionsByFilenameUM.put(file3, CachedEntry.of(regionfile1));
			return regionfile1;
		}
	}

	public static void clearRegionFileReferences()
	{
		if(!Thread.currentThread().getName().equals("File IO Thread"))
		{
			try
			{
				ThreadedFileIOBase.threadedIOInstance.waitForFinish();
			} catch (InterruptedException ignored){}
		}

		synchronized(RegionFileCache.class)
		{
		Iterator iterator = regionsByFilename.values().iterator();

		while (iterator.hasNext())
		{
			RegionFile regionfile = (RegionFile)iterator.next();

			try
			{
				if (regionfile != null)
				{
					regionfile.close();
				}
			}
			catch (IOException ioexception)
			{
				ioexception.printStackTrace();
			}
		}

		regionsByFilename.clear();
		}
	}

	public static DataInputStream getChunkInputStream(File p_76549_0_, int p_76549_1_, int p_76549_2_)
	{
		RegionFile regionfile = createOrLoadRegionFile(p_76549_0_, p_76549_1_, p_76549_2_);
		return regionfile.getChunkDataInputStream(p_76549_1_ & 31, p_76549_2_ & 31);
	}

	public static DataOutputStream getChunkOutputStream(File p_76552_0_, int p_76552_1_, int p_76552_2_)
	{
		RegionFile regionfile = createOrLoadRegionFile(p_76552_0_, p_76552_1_, p_76552_2_);
		return regionfile.getChunkDataOutputStream(p_76552_1_ & 31, p_76552_2_ & 31);
	}
}