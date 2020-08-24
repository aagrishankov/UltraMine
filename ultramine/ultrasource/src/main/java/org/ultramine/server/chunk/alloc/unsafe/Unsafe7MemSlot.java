package org.ultramine.server.chunk.alloc.unsafe;

import sun.misc.Unsafe;

//LSB
//MSB#META
//BLOCK#SKY
public final class Unsafe7MemSlot extends AbstractUnsafeMemSlot
{
	private static final long BYTE_ARRAY_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
	private static final int OFFSET_LSB			= 0;
	private static final int OFFSET_MSB			= 4096;
	private static final int OFFSET_META		= 4096+2048;
	private static final int OFFSET_BLOCK_LIGHT = 4096+2048+2048;
	private static final int OFFSET_SKY_LIGHT	= 4096+2048+2048+2048;

	Unsafe7MemSlot(UnsafeChunkAlloc alloc, long pointer)
	{
		super(alloc, pointer);
	}

	//raw set

	@Override
	public void setLSB(byte[] arr)
	{
		if(arr == null || arr.length != 4096) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer, 4096);
	}

	@Override
	public void setLSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer, 4096);
	}

	@Override
	public void setMSB(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_MSB, 2048);
	}

	@Override
	public void setMSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_MSB, 2048);
	}

	@Override
	public void setBlockMetadata(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_META, 2048);
	}

	@Override
	public void setBlockMetadata(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_META, 2048);
	}

	@Override
	public void setBlocklight(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_BLOCK_LIGHT, 2048);
	}

	@Override
	public void setBlocklight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_BLOCK_LIGHT, 2048);
	}

	@Override
	public void setSkylight(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_SKY_LIGHT, 2048);
	}

	@Override
	public void setSkylight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_SKY_LIGHT, 2048);
	}

	//raw copy

	@Override
	public void copyLSB(byte[] arr)
	{
		if(arr == null || arr.length != 4096) throw new IllegalArgumentException();
		U.copyMemory(null, pointer, arr, BYTE_ARRAY_OFFSET, 4096);
	}

	@Override
	public void copyLSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
		U.copyMemory(null, pointer, arr, BYTE_ARRAY_OFFSET + start, 4096);
	}

	@Override
	public void copyMSB(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_MSB, arr, BYTE_ARRAY_OFFSET, 2048);
	}

	@Override
	public void copyMSB(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_MSB, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	@Override
	public void copyBlockMetadata(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_META, arr, BYTE_ARRAY_OFFSET, 2048);
	}

	@Override
	public void copyBlockMetadata(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_META, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	@Override
	public void copyBlocklight(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_BLOCK_LIGHT, arr, BYTE_ARRAY_OFFSET, 2048);
	}

	@Override
	public void copyBlocklight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_BLOCK_LIGHT, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	@Override
	public void copySkylight(byte[] arr)
	{
		if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_SKY_LIGHT, arr, BYTE_ARRAY_OFFSET, 2048);
	}

	@Override
	public void copySkylight(byte[] arr, int start)
	{
		if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
		U.copyMemory(null, pointer + OFFSET_SKY_LIGHT, arr, BYTE_ARRAY_OFFSET + start, 2048);
	}

	//clear

	@Override
	public void zerofillMSB()
	{
		U.setMemory(pointer + OFFSET_MSB, 2048, (byte)0);
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
		return (getByte(y << 8 | z << 4 | x) & 255) | (get4bits(OFFSET_MSB, x, y, z) << 8);
	}

	@Override
	public void setBlockId(int x, int y, int z, int id)
	{
		setByte(y << 8 | z << 4 | x, (byte)(id & 0xFF));
		set4bits(OFFSET_MSB, x, y, z, (id & 3840) >> 8);
	}

	@Override
	public int getMeta(int x, int y, int z)
	{
		return get4bits(OFFSET_META, x, y, z);
	}

	@Override
	public void setMeta(int x, int y, int z, int meta)
	{
		set4bits(OFFSET_META, x, y, z, meta);
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
