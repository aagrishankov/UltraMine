package org.ultramine.server.util;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Arrays;

public class MinecraftUtil
{
	//from ItemBoat
	public static MovingObjectPosition getMovingObjectPosition(EntityPlayer player)
	{
		float var4 = 1.0F;
        float var5 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * var4;
        float var6 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * var4;
        double var7 = player.prevPosX + (player.posX - player.prevPosX) * (double)var4;
        double var9 = player.prevPosY + (player.posY - player.prevPosY) * (double)var4 + 1.62D - (double)player.yOffset;
        double var11 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)var4;
        Vec3 var13 = Vec3.createVectorHelper(var7, var9, var11);
        float var14 = MathHelper.cos(-var6 * 0.017453292F - (float)Math.PI);
        float var15 = MathHelper.sin(-var6 * 0.017453292F - (float)Math.PI);
        float var16 = -MathHelper.cos(-var5 * 0.017453292F);
        float var17 = MathHelper.sin(-var5 * 0.017453292F);
        float var18 = var15 * var16;
        float var20 = var14 * var16;
        double var21 = 5.0D;
        Vec3 var23 = var13.addVector((double)var18 * var21, (double)var17 * var21, (double)var20 * var21);
        return player.worldObj.rayTraceBlocks(var13, var23, true);
	}
	
	public static int countXPCostForLevel(int level)
	{
		if(level < 17)
		{
			return 17*level;
		}
		else if(level < 30)
		{
			int cost = 17*level;
			for(int i = 0; i < level - 15; i++)
				cost += i*3;
			return cost + level/18;
		}
		else
		{
			int cost = 826;
			for(int i = 0; i < level - 30; i++)
				cost += 62 + i*7;
			return cost;
		}
	}

	public static boolean canLeavesStay(World world, int bx, int by, int bz, int searchDistance)
	{
		return new LeavesPathFinder(world, bx, by, bz, searchDistance).canLeavesStay();
	}

	private static class LeavesPathFinder
	{
		private static final byte SIZE = 16;
		private static final int SIZE_HALF = SIZE / 2;
		private static final byte[] AREA = new byte[SIZE * SIZE * SIZE];
		private static final int EMPTY = 0;
		private static final int WOOD = -1;
		private static final int LEAVES = Byte.MAX_VALUE;
		private static final int WALL = -2;

		private final World world;
		private final int bx;
		private final int by;
		private final int bz;
		private final int distance;

		public LeavesPathFinder(World world, int bx, int by, int bz, int distance)
		{
			this.world = world;
			this.bx = bx;
			this.by = by;
			this.bz = bz;
			this.distance = distance;
		}

		public boolean canLeavesStay()
		{
			if(distance > 7)
				throw new IllegalArgumentException("distance should be less then 8, given: " + distance);
			if(!world.checkChunksExist(bx - distance - 1, by - distance - 1, bz - distance - 1, bx + distance + 1, by + distance + 1, bz + distance + 1))
				return true;

			Arrays.fill(AREA, (byte)EMPTY);
			setVal(0, 0, 0, LEAVES);
			return recursivePathFind(0, 0, 0, 0);
		}

		private boolean recursivePathFind(int x, int y, int z, int depth)
		{
			int current = getVal(x, y, z);

			if(current == EMPTY)
			{
				Block block = world.getBlock(bx + x, by + y, bz + z);
				current = block.canSustainLeaves(world, bx + x, by + y, bz + z) ? WOOD : block.isLeaves(world, bx + x, by + y, bz + z) ? LEAVES : WALL;
				setVal(x, y, z, current);
			}

			if(current == WOOD)
				return true;
			else if(current <= depth)
				return false;

			setVal(x, y, z, depth);

			int newDepth = depth + 1;
			if(newDepth > distance)
				return false;

			if(recursivePathFind(x+1, y, z, newDepth)) return true;
			if(recursivePathFind(x, y+1, z, newDepth)) return true;
			if(recursivePathFind(x, y, z+1, newDepth)) return true;
			if(recursivePathFind(x-1, y, z, newDepth)) return true;
			if(recursivePathFind(x, y-1, z, newDepth)) return true;
			if(recursivePathFind(x, y, z-1, newDepth)) return true;
			return false;
		}

		private static int getVal(int x, int y, int z)
		{
			return AREA[((x + SIZE_HALF) << 8) | ((y + SIZE_HALF) << 4) | (z + SIZE_HALF)];
		}

		private static void setVal(int x, int y, int z, int newVal)
		{
			AREA[((x + SIZE_HALF) << 8) | ((y + SIZE_HALF) << 4) | (z + SIZE_HALF)] = (byte)newVal;
		}
	}
}
