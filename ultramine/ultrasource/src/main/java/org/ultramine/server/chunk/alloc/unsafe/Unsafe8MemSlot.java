package org.ultramine.server.chunk.alloc.unsafe;

import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Unsafe8MemSlot extends AbstractUnsafeMemSlot
{
	private static final long BYTE_ARRAY_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
	private static final int OFFSET_BLOCK_LIGHT = 4096+2048+2048;
	private static final int OFFSET_SKY_LIGHT	= 4096+2048+2048+2048;

	Unsafe8MemSlot(UnsafeChunkAlloc alloc, long pointer)
	{
		super(alloc, pointer);
	}

	private void setSingleLSB(int ind, byte data)
	{
		setChar(ind, (char)((getChar(ind) & 0xFF00) | (data & 0xFF)));
	}

	private byte getSingleLSB(int ind)
	{
		return (byte)(getChar(ind) & 0xFF);
	}

	private void setSingleMSB(int ind, byte data)
	{
		setChar(ind, (char)((getChar(ind) & 0xF0FF) | ((data & 0xF) << 8)));
	}

	private byte getSingleMSB(int ind)
	{
		return (byte)((getChar(ind) >> 8) & 0xF);
	}

	private void setSingleMeta(int ind, byte data)
	{
		setChar(ind, (char)((getChar(ind) & 0x0FFF) | ((data & 0xF) << 12)));
	}

	private byte getSingleMeta(int ind)
	{
		return (byte)((getChar(ind) >> 12) & 0xF);
	}

	@Override
	public void setLSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
		for(int i = 0; i < 4096; i++)
			setSingleLSB(i << 1, arr[start + i]);
	}

	@Override
	public void setMSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		for(int i = 0; i < 2048; i++)
		{
			byte data = arr[start + i];
			int ind = (i << 1);
			setSingleMSB(ind << 1,			data);
			setSingleMSB((ind + 1) << 1,	(byte)(data >> 4));
		}
	}

	@Override
	public void setData(@Nonnull byte[] lsb, @Nullable byte[] msb, @Nonnull byte[] meta, @Nonnull byte[] blockLight, @Nullable byte[] skyLight)
	{
		for(int i = 0; i < 4096; i++)
			setChar(i << 1, (char)(get4bits(meta, i) << 12 | get4bits(msb, i) << 8 | (lsb[i] & 0xFF)));
		setBlocklight(blockLight);
		if(skyLight != null)
			setSkylight(skyLight);
		else
			zerofillSkylight();
	}

	private static int get4bits(@Nullable byte[] arr, int i)
	{
		if(arr == null)
			return 0;
		int data = arr[i >> 1];
		return ((i & 1) == 0) ? (data & 0xF) : (data >> 4 & 0xF);
	}

	@Override
	public void setBlockMetadata(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		for(int i = 0; i < 2048; i++)
		{
			byte data = arr[start + i];
			int ind = (i << 1);
			setSingleMeta(ind << 1,			data);
			setSingleMeta((ind + 1) << 1,	(byte)(data >> 4));
		}
	}

	@Override
	public void setBlocklight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_BLOCK_LIGHT, 2048);
	}

	@Override
	public void setSkylight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_SKY_LIGHT, 2048);
	}

	@Override
	public void copyLSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
		for(int i = 0; i < 4096; i++)
			arr[start + i] = getSingleLSB(i << 1);
	}

	@Override
	public void copyMSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		for(int i = 0; i < 2048; i++)
			arr[start + i] = (byte)((getSingleMSB(i << 2)) | (getSingleMSB(((i << 1) + 1) << 1) << 4));
	}

	@Override
	public void copyBlockMetadata(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		for(int i = 0; i < 2048; i++)
			arr[start + i] = (byte)((getSingleMeta(i << 2)) | (getSingleMeta(((i << 1) + 1) << 1) << 4));
	}

	@Override
	public void copyBlocklight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_BLOCK_LIGHT, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	@Override
	public void copySkylight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_SKY_LIGHT, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	@Override
	public void zerofillMSB()
	{
		for(int i = 0; i < 4096; i++)
			setSingleMSB(i << 1, (byte)0);
	}

	@Override
	public void zerofillSkylight()
	{
		U.setMemory(pointer + OFFSET_SKY_LIGHT, 2048, (byte)0);
	}

	@Override
	public void zerofillAll()
	{
		U.setMemory(pointer, SLOT_SIZE, (byte)0);
	}

	//

	@Override
	public int getBlockId(int x, int y, int z)
	{
		return getChar((y << 8 | z << 4 | x) << 1) & 0xFFF;
	}

	@Override
	public void setBlockId(int x, int y, int z, int id)
	{
		int ind = (y << 8 | z << 4 | x) << 1;
		setChar(ind, (char)((getChar(ind) & 0xF000) | id));
	}

	@Override
	public int getMeta(int x, int y, int z)
	{
		return getChar((y << 8 | z << 4 | x) << 1) >> 12;
	}

	@Override
	public void setMeta(int x, int y, int z, int meta)
	{
		setSingleMeta((y << 8 | z << 4 | x) << 1, (byte)meta);
	}

	@Override
	public void setBlockIdAndMeta(int x, int y, int z, int id, int meta)
	{
		setChar((y << 8 | z << 4 | x) << 1, (char)((meta << 12) | id));
	}

	@Override
	public int getBlockIdAndMeta(int x, int y, int z)
	{
		return getChar((y << 8 | z << 4 | x) << 1);
	}

	@Override
	public int getBlocklight(int x, int y, int z)
	{
		return get4bits(OFFSET_BLOCK_LIGHT, x, y, z);
	}

	@Override
	public void setBlocklight(int x, int y, int z, int val)
	{
		set4bits(OFFSET_BLOCK_LIGHT, x, y, z, val);
	}

	@Override
	public int getSkylight(int x, int y, int z)
	{
		return get4bits(OFFSET_SKY_LIGHT, x, y, z);
	}

	@Override
	public void setSkylight(int x, int y, int z, int val)
	{
		set4bits(OFFSET_SKY_LIGHT, x, y, z, val);
	}
}
