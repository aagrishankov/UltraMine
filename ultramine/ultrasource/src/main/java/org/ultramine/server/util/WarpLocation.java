package org.ultramine.server.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

public class WarpLocation
{
	public int dimension;
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;
	public double randomRadius;
	
	public WarpLocation(){}
	public WarpLocation(int dimension, double x, double y, double z, float yaw, float pitch, double randomRadius)
	{
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.randomRadius = randomRadius;
	}
	
	public WarpLocation(int dimension, double x, double y, double z, float yaw, float pitch)
	{
		this(dimension, x, y, z, yaw, pitch, 0);
	}
	
	public WarpLocation(int dimension, double x, double y, double z)
	{
		this(dimension, x, y, z, 0, 0);
	}
	
	public WarpLocation randomize()
	{
		if(randomRadius == 0)
			return this;
		
		WorldServer world = MinecraftServer.getServer().getMultiWorld().getWorldByID(dimension);
		if(world == null)
			return this;
		double newX = x + randomRadius*world.rand.nextDouble()*(world.rand.nextBoolean() ? -1 : 1);
		double newZ = z + randomRadius*world.rand.nextDouble()*(world.rand.nextBoolean() ? -1 : 1);
		double newY = y;
		int intX = MathHelper.floor_double(newX);
		int intZ = MathHelper.floor_double(newZ);
		if(world.chunkExists(intX >> 4, intZ >> 4))
		{
			while(world.getBlock(intX, MathHelper.floor_double(newY), intZ) != Blocks.air)
				newY++;
			while(world.getBlock(intX, MathHelper.floor_double(newY)-1, intZ) == Blocks.air && newY > 0)
				newY--;
			if(newY == 0)
				newY = 255;
		}
		else
		{
			return this;
		}
		
		return new WarpLocation(dimension, newX, newY, newZ, yaw, pitch, 0);
	}
	
	public boolean equals(WarpLocation loc)
	{
		if(this == loc) return true;
		return
				Math.abs(x - loc.x) < 0.1 &&
				Math.abs(y - loc.y) < 0.1 &&
				Math.abs(z - loc.z) < 0.1;
	}
	
	public NBTTagCompound toNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setDouble("x", x);
		nbt.setDouble("y", y);
		nbt.setDouble("z", z);
		if(dimension != 0)
			nbt.setInteger("d", dimension);
		if(yaw != 0F)
			nbt.setFloat("w", yaw);
		if(pitch != 0F)
			nbt.setFloat("p", pitch);
		if(randomRadius != 0d)
			nbt.setDouble("r", randomRadius);
		return nbt;
	}
	
	public static WarpLocation getFromNBT(NBTTagCompound nbt)
	{
		double x = nbt.getDouble("x");
		double y = nbt.getDouble("y");
		double z = nbt.getDouble("z");
		int dimension = nbt.hasKey("d") ? nbt.getInteger("d") : 0;
		float yaw = nbt.hasKey("w") ? nbt.getFloat("w") : 0F;
		float pitch = nbt.hasKey("p") ? nbt.getFloat("p") : 0F;
		int randomRadius = nbt.hasKey("r") ? nbt.getInteger("r") : 0;
		
		return new WarpLocation(dimension, x, y, z, yaw, pitch, randomRadius);
	}
	
	public static WarpLocation getFromPlayer(EntityPlayer player)
	{
		return new WarpLocation(player.dimension,
				(double)Math.round(player.posX*100)/100.0,
				(double)Math.round(player.posY*100)/100.0,
				(double)Math.round(player.posZ*100)/100.0,
				(float)Math.round(player.rotationYaw*100)/100.0F,
				(float)Math.round(player.rotationPitch*100)/100.0F);
	}
}
