package org.ultramine.server;

import java.util.concurrent.ThreadLocalRandom;

import org.ultramine.server.WorldsConfig.WorldConfig.LoadBalancer.Limits;
import org.ultramine.server.WorldsConfig.WorldConfig.LoadBalancer.Limits.PerChunkEntityLimits;
import org.ultramine.server.chunk.ChunkHash;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.openhft.koloboke.collect.map.IntByteMap;

public class ServerLoadBalancer
{
	private static final boolean isClient = FMLCommonHandler.instance().getSide().isClient();
	private static final PerChunkEntityLimits clientLimits = new PerChunkEntityLimits();
	private static final PerChunkEntityLimits infinityLimits = new PerChunkEntityLimits();
	private final World world;
	private final IntByteMap activeChunkSet;
	
	static
	{
		clientLimits.lowerLimit = 32;
		clientLimits.higherLimit = Integer.MAX_VALUE;
		clientLimits.updateRadius = 7;
		infinityLimits.lowerLimit = Integer.MAX_VALUE;
		infinityLimits.higherLimit = Integer.MAX_VALUE;
		infinityLimits.updateRadius = 99;
		infinityLimits.updateByChunkLoader = true;
	}
	
	public ServerLoadBalancer(World world)
	{
		this.world = world;
		this.activeChunkSet = world.getActiveChunkSet();
	}
	
	public boolean canUpdateEntity(Entity ent)
	{
		if(ent.isEntityPlayerMP() || isClient && world.isRemote && ent.isEntityPlayer())
			return true;
		int cx = MathHelper.floor_double(ent.posX) >> 4;
		int cz = MathHelper.floor_double(ent.posZ) >> 4;

		Chunk chunk = world.getChunkIfExists(cx, cz);
		if(chunk == null)
		{
			if(!ent.forceSpawn)
			{
				world.getEventProxy().startEntity(ent);
				ent.setDead();
			}
			return false;
		}
		
		int count = chunk.getEntityCountOfSameType(ent);
		PerChunkEntityLimits limits = getLimits(ent);
		if(count > limits.higherLimit)
		{
			world.getEventProxy().startEntity(ent);
			ent.setDead();
			return false;
		}

		int prior = activeChunkSet.get(ChunkHash.chunkToKey(cx, cz));
		if(prior == Byte.MAX_VALUE)
		{
			world.getEventProxy().startEntity(ent);
			ent.updateInactive();
			return false;
		}

		if(!ent.addedToChunk)
			return true;

		int lowerLimit = limits.lowerLimit;

		if(prior == WorldConstants.CL_CHUNK_PRIOR)
		{
			if(!limits.updateByChunkLoader)
				lowerLimit = 1;
		}
		else if(prior > limits.updateRadius)
		{
			lowerLimit = 1;
		}

		if(count > lowerLimit)
		{
			return ThreadLocalRandom.current().nextInt(count) < lowerLimit;
		}

		return true;
	}
	
	private PerChunkEntityLimits getLimits(Entity e)
	{
		if(isClient)
			return clientLimits;
		Limits limits = ((WorldServer)e.worldObj).getConfig().loadBalancer.limits;
		
		switch(e.getEntityType())
		{
		case MONSTER:	return limits.monsters;
		case ANIMAL:	return limits.animals;
		case AMBIENT:	return limits.ambient;
		case WATER:		return limits.water;
		case ITEM:		return limits.items;
		case XP_ORB:	return limits.xpOrbs;
		default: 		return infinityLimits;
		}
	}
}
