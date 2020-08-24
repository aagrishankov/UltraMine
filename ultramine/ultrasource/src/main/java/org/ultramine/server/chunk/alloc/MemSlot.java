package org.ultramine.server.chunk.alloc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MemSlot
{
	default void setLSB(byte[] arr)
	{
		setLSB(arr, 0);
	}

	void setLSB(byte[] arr, int start);

	default void setMSB(byte[] arr)
	{
		setMSB(arr, 0);
	}

	void setMSB(byte[] arr, int start);

	default void setData(@Nonnull byte[] lsb, @Nullable byte[] msb, @Nonnull byte[] meta, @Nonnull byte[] blockLight, @Nullable byte[] skyLight)
	{
		setLSB(lsb);
		if(msb != null)
			setMSB(msb);
		else
			zerofillMSB();
		setBlockMetadata(meta);
		setBlocklight(blockLight);
		if(skyLight != null)
			setSkylight(skyLight);
		else
			zerofillSkylight();
	}

	default void setBlockMetadata(byte[] arr)
	{
		setBlockMetadata(arr, 0);
	}

	void setBlockMetadata(byte[] arr, int start);

	default void setBlocklight(byte[] arr)
	{
		setBlocklight(arr, 0);
	}

	void setBlocklight(byte[] arr, int start);

	default void setSkylight(byte[] arr)
	{
		setSkylight(arr, 0);
	}

	void setSkylight(byte[] arr, int start);

	default void copyLSB(byte[] arr)
	{
		copyLSB(arr, 0);
	}

	void copyLSB(byte[] arr, int start);

	default void copyMSB(byte[] arr)
	{
		copyMSB(arr, 0);
	}

	void copyMSB(byte[] arr, int start);

	default void copyBlockMetadata(byte[] arr)
	{
		copyBlockMetadata(arr, 0);
	}

	void copyBlockMetadata(byte[] arr, int start);

	default void copyBlocklight(byte[] arr)
	{
		copyBlocklight(arr, 0);
	}

	void copyBlocklight(byte[] arr, int start);

	default void copySkylight(byte[] arr)
	{
		copySkylight(arr, 0);
	}

	void copySkylight(byte[] arr, int start);

	default byte[] copyLSB()
	{
		byte[] arr = new byte[4096];
		copyLSB(arr);
		return arr;
	}

	default byte[] copyMSB()
	{
		byte[] arr = new byte[2048];
		copyMSB(arr);
		return arr;
	}

	default byte[] copyBlockMetadata()
	{
		byte[] arr = new byte[2048];
		copyBlockMetadata(arr);
		return arr;
	}

	default byte[] copyBlocklight()
	{
		byte[] arr = new byte[2048];
		copyBlocklight(arr);
		return arr;
	}

	default byte[] copySkylight()
	{
		byte[] arr = new byte[2048];
		copySkylight(arr);
		return arr;
	}

	void zerofillMSB();

	void zerofillSkylight();

	void zerofillAll();

	int getBlockId(int x, int y, int z);

	void setBlockId(int x, int y, int z, int id);

	int getMeta(int x, int y, int z);

	void setMeta(int x, int y, int z, int meta);

	default void setBlockIdAndMeta(int x, int y, int z, int id, int meta)
	{
		setBlockId(x, y, z, id);
		setMeta(x, y, z, meta);
	}

	default int getBlockIdAndMeta(int x, int y, int z)
	{
		return getBlockId(x, y, z) | (getMeta(x, y, z) << 12);
	}

	int getBlocklight(int x, int y, int z);

	void setBlocklight(int x, int y, int z, int val);

	int getSkylight(int x, int y, int z);

	void setSkylight(int x, int y, int z, int val);

	@Nonnull
	ChunkAllocService getAlloc();

	void copyFrom(@Nonnull MemSlot src);

	@Nonnull
	default MemSlot copy()
	{
		MemSlot other = getAlloc().allocateSlot();
		other.copyFrom(this);
		return other;
	}

	void release();
}
