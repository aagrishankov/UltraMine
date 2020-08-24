package org.ultramine.server.mobspawn;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.ultramine.server.WorldsConfig.WorldConfig;

import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class MobSpawnerAmbient extends MobSpawner
{
	public MobSpawnerAmbient(WorldServer world)
	{
		super(world, EnumCreatureType.ambient);
	}

	@Override
	public void configure(WorldConfig config)
	{
		setConstants(config.mobSpawn.newEngineSettings.ambient);
	}

	@Override
	protected boolean shouldPerform()
	{
		return world.getConfig().mobSpawn.spawnAnimals;
	}

	@Override
	protected void processChunk(Chunk chunk, int x, int z, int topf)
	{
		throw new UnsupportedOperationException();
	}

	protected void rebuildList()
	{
		for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(world.playerEntities, EntityPlayerMP.class))
		{
			if(player.posY > 48)
				continue;

			for(int i = 0; i < 12; i++)
			{
				int x = (int)player.posX + world.rand.nextInt(16) - 8 + (world.rand.nextBoolean() ? 8 : -8);
				int y = (int)player.posY + world.rand.nextInt(16) - 8;
				int z = (int)player.posZ + world.rand.nextInt(16) - 8 + (world.rand.nextBoolean() ? 8 : -8);

				if(world.countEntitiesByType(type, x >> 4, z >> 4, set.localCheckRadius) < getLocalLimit() && world.getBlockLightValue(x, y, z) < 7)
				{
					if(trySpawnGroupAt(x, y, z, 4 + world.rand.nextInt(4)))
						break;
				}
			}
		}
	}
}
