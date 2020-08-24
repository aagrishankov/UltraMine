package org.ultramine.server.chunk.alloc.unsafe;

import org.ultramine.server.chunk.alloc.ChunkAllocService;
import org.ultramine.server.chunk.alloc.MemSlot;
import org.ultramine.server.util.UnsafeUtil;
import sun.misc.Unsafe;

abstract class AbstractUnsafeMemSlot implements MemSlot
{
	static final int SLOT_SIZE = 4096*3;
	protected static final Unsafe U = UnsafeUtil.getUnsafe();

	protected final UnsafeChunkAlloc alloc;
	protected final long pointer;
	private boolean isReleased;

	AbstractUnsafeMemSlot(UnsafeChunkAlloc alloc, long pointer)
	{
		this.alloc = alloc;
		this.pointer = pointer;
	}

	@Override
	public ChunkAllocService getAlloc()
	{
		return alloc;
	}

	public final long getPointer()
	{
		return pointer;
	}

	@Override
	public void copyFrom(MemSlot src)
	{
		if(getClass() != src.getClass())
			throw new IllegalStateException();
		if(isReleased)
			throw new IllegalStateException("Destination slot already released");
		if(((AbstractUnsafeMemSlot)src).isReleased)
			throw new IllegalStateException("Source slot already released");
		U.copyMemory(((AbstractUnsafeMemSlot)src).getPointer(), pointer, SLOT_SIZE);
	}

	@Override
	public void release()
	{
		if(isReleased)
			throw new IllegalStateException("Slot already released");
		alloc.releaseSlot(pointer);
		isReleased = true;
	}

	protected final void setByte(int ind, byte data)
	{
		U.putByte(pointer + ind, data);
	}

	protected final byte getByte(int ind)
	{
		return U.getByte(pointer + ind);
	}

	protected final void setChar(int ind, char data)
	{
		U.putChar(pointer + ind, data);
	}

	protected final char getChar(int ind)
	{
		return U.getChar(pointer + ind);
	}

	protected final int get4bits(int start, int x, int y, int z)
	{
		int ind = y << 8 | z << 4 | x;
		byte data = getByte(start + (ind >> 1));
		return (ind & 1) == 0 ? data & 15 : data >> 4 & 15;
	}

	protected final void set4bits(int start, int x, int y, int z, int data)
	{
		int ind = y << 8 | z << 4 | x;
		int off = start + (ind >> 1);
		if ((ind & 1) == 0)
			setByte(off, (byte)(getByte(off) & 240 | data & 15));
		else
			setByte(off, (byte)(getByte(off) & 15 | (data & 15) << 4));
	}

	@Override
	protected void finalize()
	{
		try
		{
			if(!isReleased)
				release();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
}
