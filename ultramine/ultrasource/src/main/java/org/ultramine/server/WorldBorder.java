package org.ultramine.server;

import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;

import org.ultramine.server.WorldsConfig.WorldConfig.Border;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class WorldBorder
{
	private final OneBorder[] borders;
	
	public WorldBorder(Border[] brds)
	{
		if(brds == null)
		{
			borders = new OneBorder[0];
		}
		else
		{
			borders = new OneBorder[brds.length];
			for(int i = 0; i < brds.length; i++)
			{
				Border brd = brds[i];
				borders[i] = new OneBorder(brd.x, brd.z, brd.radius, brd.round);
			}
		}
	}
	
	public boolean isInsideBorder(int x, int z)
	{
		if(borders.length == 0)
			return true;
		for(OneBorder brd : borders)
			if(brd.isInsideBorder(x, z))
				return true;
		
		return false;
	}
	
	public boolean isChunkInsideBorder(int cx, int cz)
	{
		return isInsideBorder(cx >> 4, cz >> 4);
	}
	
	public ChunkPosition correctPosition(int x, int z)
	{
		OneBorder found = null;
		int min = Integer.MAX_VALUE;
		for(OneBorder brd : borders)
		{
			int nmin = Math.min(Math.abs(brd.minX - x), Math.abs(brd.maxX - x)) * Math.min(Math.abs(brd.minZ - z), Math.abs(brd.maxZ - z));
			if(nmin < min)
			{
				min = nmin;
				found = brd;
			}
		}
		
		return found.correctPosition(x, z);
	}
	
	@SideOnly(Side.SERVER)
	private static class OneBorder
	{
		private final int x;
		private final int z;
		private final int radius;
		private final boolean round;
		
		private final int maxX;
		private final int minX;
		private final int maxZ;
		private final int minZ;
		private final int radiusSquared;
		private final int definiteSquare;
		
		public OneBorder(int x, int z, int radius, boolean round)
		{
			this.x = x;
			this.z = z;
			this.radius = radius;
			this.round = round;
			
			this.maxX = x + radius;
			this.minX = x - radius;
			this.maxZ = z + radius;
			this.minZ = z - radius;
			this.radiusSquared = radius * radius;
			this.definiteSquare = (int)Math.sqrt(.5 * this.radiusSquared);
		}
		
		public boolean isInsideBorder(int xLoc, int zLoc)
		{
			if (!round)
			{
				return !(xLoc < minX || xLoc > maxX || zLoc < minZ || zLoc > maxZ);
			}
			else
			{
				int X = Math.abs(x - xLoc);
				int Z = Math.abs(z - zLoc);

				if (X < definiteSquare && Z < definiteSquare)
					return true;	// Definitely inside
				else if (X >= radius || Z >= radius)
					return false;	// Definitely outside
				else if (X * X + Z * Z < radiusSquared)
					return true;	// After further calculation, inside
				else
					return false;	// Apparently outside, then
			}
		}
		
		public ChunkPosition correctPosition(int xLoc, int zLoc)
		{
			if (!round)
			{
				if (xLoc <= minX)
					xLoc = minX + 1;
				else if (xLoc >= maxX)
					xLoc = maxX - 1;
				if (zLoc <= minZ)
					zLoc = minZ + 1;
				else if (zLoc >= maxZ)
					zLoc = maxZ - 1;
			}
			else
			{
				double dX = xLoc - x;
				double dZ = zLoc - z;
				double dU = Math.sqrt(dX * dX + dZ * dZ); //distance of the untransformed point from the center
				double dT = Math.sqrt((dX * dX + dZ * dZ) / radiusSquared); //distance of the transformed point from the center
				double f = (1 / dT - 2 / dU); //"correction" factor for the distances
				xLoc = x + MathHelper.floor_double(dX * f);
				zLoc = z + MathHelper.floor_double(dZ * f);
			}
			
			return new ChunkPosition(xLoc, 0, zLoc);
		}
	}
}
