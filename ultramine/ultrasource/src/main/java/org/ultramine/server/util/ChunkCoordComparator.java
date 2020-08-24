package org.ultramine.server.util;

import org.ultramine.server.WorldConstants;
import org.ultramine.server.chunk.ChunkHash;

public class ChunkCoordComparator implements IntComparator
{
	private static final int VIEW = WorldConstants.MAX_VIEW_DISTANCE;
	private static final int VIEW_WIDTH = VIEW*2+1;
	
	private static final ChunkCoordComparator[] comparators = new ChunkCoordComparator[8];

	static
	{
		try
		{
			for(int i = 0; i < 8; i++)
			{
				comparators[i] = new ChunkCoordComparator(BlockFace.notchToFace(i));
				comparators[i].generateSlope();
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

	public static ChunkCoordComparator get(BlockFace direction, int middleX, int middleZ)
	{
		return new ChunkCoordComparator(comparators[BlockFace.faceToNotch(direction)], middleX, middleZ);
	}

	private int index = 0;
	private final BlockFace direction;
	private final int[] indices;
	private final int middleX;
	private final int middleZ;

	private ChunkCoordComparator(ChunkCoordComparator source, int middleX, int middleZ)
	{
		this.indices = source.indices;
		this.direction = source.direction;
		this.middleX = middleX;
		this.middleZ = middleZ;
	}

	private ChunkCoordComparator(BlockFace direction)
	{
		this.direction = direction;
		this.indices = new int[VIEW_WIDTH * VIEW_WIDTH];
		middleX = 0;
		middleZ = 0;
	}

	private int getRawIndex(int x, int z)
	{
		return indices[x * VIEW_WIDTH + z];
	}

	private void setRawIndex(int x, int z, int value)
	{
		indices[x * VIEW_WIDTH + z] = value;
	}

	private void generate(int dx, int dz)
	{
		dx += VIEW;
		dz += VIEW;
		if(dx >= 0 && dx < VIEW_WIDTH && dz >= 0 && dz < VIEW_WIDTH)
		{
			if(getRawIndex(dx, dz) == 0)
				setRawIndex(dx, dz, this.index++);
		}
	}

	private void generateLayer(final int layer, final double factor)
	{
		int count = (int) (layer * factor) + 1;
		// get modifiers from direction
		MoveMod[] mods = MoveMod.get(direction);
		// Get the chunk to start at
		int startx = this.direction.getModX() * layer;
		int startz = this.direction.getModZ() * layer;
		// Send starter chunk
		this.generate(startx, startz);
		// Peel
		int x1 = startx;
		int z1 = startz;
		int x2 = startx;
		int z2 = startz;
		while(--count > 0)
		{
			// offset the chunks
			x1 += mods[0].direction.getModX();
			z1 += mods[0].direction.getModZ();
			x2 += mods[1].direction.getModX();
			z2 += mods[1].direction.getModZ();
			// mod update
			mods[0].next(x1, z1, layer);
			mods[1].next(x2, z2, layer);
			// got till the end?
			this.generate(x1, z1);
			if(x1 == x2 && z1 == z2)
			{
				return;
			}
			else
			{
				this.generate(x2, z2);
			}
		}
	}

	private void generateSpiral()
	{
		// main chunk
		this.generate(0, 0);
		// Only full layers
		for(int layer = 1; layer <= VIEW; layer++)
		{
			this.generateLayer(layer, 4);
		}
	}

	private void generateSlope()
	{
		// main chunk
		this.generate(0, 0);

		// to this layer full layers are sent, after  half
		final int threshold1 = 2;
		// at this layer less than half are sent
		final int threshold2 = 5;

		for(int layer = 1; layer <= VIEW; layer++)
		{
			if(layer <= threshold1)
			{
				this.generateLayer(layer, 4);
			}
			else if(layer <= threshold2)
			{
				this.generateLayer(layer, 2);
			}
			else
			{
				this.generateLayer(layer, 1.5);
			}
		}

		// end with only full layers
		for(int layer = 1; layer <= VIEW; layer++)
		{
			this.generateLayer(layer, 4);
		}
	}

	private static class MoveMod
	{
		private MoveMod(BlockFace direction, boolean right)
		{
			this.direction = direction;
			this.right = right;
		}

		public BlockFace direction;
		public boolean right;

		public void next(int dx, int dz, int limit)
		{
			if(Math.abs(dx) >= limit && Math.abs(dz) >= limit)
			{
				this.direction = direction.rotate(right ? 2 : -2);
			}
		}

		public static MoveMod[] get(BlockFace direction)
		{
			MoveMod[] mods = new MoveMod[2];
			if(direction == BlockFace.NORTH)
			{
				mods[0] = new MoveMod(BlockFace.WEST, false);
				mods[1] = new MoveMod(BlockFace.EAST, true);
			}
			else if(direction == BlockFace.SOUTH)
			{
				mods[0] = new MoveMod(BlockFace.WEST, true);
				mods[1] = new MoveMod(BlockFace.EAST, false);
			}
			else if(direction == BlockFace.EAST)
			{
				mods[0] = new MoveMod(BlockFace.NORTH, false);
				mods[1] = new MoveMod(BlockFace.SOUTH, true);
			}
			else if(direction == BlockFace.WEST)
			{
				mods[0] = new MoveMod(BlockFace.NORTH, true);
				mods[1] = new MoveMod(BlockFace.SOUTH, false);
			}
			else if(direction == BlockFace.NORTH_EAST)
			{
				mods[0] = new MoveMod(BlockFace.WEST, false);
				mods[1] = new MoveMod(BlockFace.SOUTH, true);
			}
			else if(direction == BlockFace.SOUTH_EAST)
			{
				mods[0] = new MoveMod(BlockFace.WEST, true);
				mods[1] = new MoveMod(BlockFace.NORTH, false);
			}
			else if(direction == BlockFace.SOUTH_WEST)
			{
				mods[0] = new MoveMod(BlockFace.NORTH, true);
				mods[1] = new MoveMod(BlockFace.EAST, false);
			}
			else if(direction == BlockFace.NORTH_WEST)
			{
				mods[0] = new MoveMod(BlockFace.SOUTH, false);
				mods[1] = new MoveMod(BlockFace.EAST, true);
			}
			return mods;
		}
	}

	public int getIndex(int x, int z)
	{
		x -= middleX;
		z -= middleZ;
		if(Math.abs(x) > VIEW || Math.abs(z) > VIEW)
		{
			return Integer.MAX_VALUE;
		}
		return getRawIndex(x + VIEW, z + VIEW);
	}

	@Override
	public int compare(int coord1, int coord2)
	{
		if(coord1 == coord2)
		{
			return 0;
		}
		return getIndex(ChunkHash.keyToX(coord1), ChunkHash.keyToZ(coord1)) - getIndex(ChunkHash.keyToX(coord2), ChunkHash.keyToZ(coord2));
	}
}
