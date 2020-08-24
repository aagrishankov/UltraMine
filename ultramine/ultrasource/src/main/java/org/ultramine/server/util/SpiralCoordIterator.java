package org.ultramine.server.util;

import net.minecraft.world.ChunkCoordIntPair;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SpiralCoordIterator implements Iterator<ChunkCoordIntPair>
{
	private final int baseX;
	private final int baseZ;
	private final int limit;
	private int counter;
	private int radius;
	private int offset;
	private int position;

	public SpiralCoordIterator(int baseX, int baseZ, int limit)
	{
		this.baseX = baseX;
		this.baseZ = baseZ;
		this.limit = limit;
	}

	public SpiralCoordIterator(int baseX, int baseZ)
	{
		this(baseX, baseZ, Integer.MAX_VALUE);
	}

	public SpiralCoordIterator()
	{
		this(0, 0);
	}

	public int getLimit()
	{
		return limit;
	}

	public int getCounter()
	{
		return counter;
	}

	@Override
	public boolean hasNext()
	{
		return counter < limit;
	}

	@Override
	public ChunkCoordIntPair next()
	{
		if(!hasNext())
			throw new NoSuchElementException("limit is exceeded");
		counter++;
		int resX;
		int resZ;
		if(radius == 0)
		{
			resX = 0;
			resZ = 0;
			radius = 1;
			offset = -1;
		}
		else
		{
			switch(position)
			{
			case 0:
				resX = -radius;
				resZ = offset;
				break;
			case 1:
				resX = offset;
				resZ = radius;
				break;
			case 2:
				resX = radius;
				resZ = -offset;
				break;
			case 3:
			default:
				resX = -offset;
				resZ = -radius;
				break;
			}

			if(++offset == radius)
			{
				if(++position == 4)
				{
					++radius;
					position = 0;
				}
				offset = -radius;
			}
		}

		return new ChunkCoordIntPair(baseX + resX, baseZ + resZ);
	}
}
