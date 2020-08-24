package org.ultramine.server.mobspawn;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.ultramine.server.WorldsConfig.WorldConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class MobSpawnerWater extends MobSpawner
{
	public MobSpawnerWater(WorldServer world)
	{
		super(world, EnumCreatureType.waterCreature);
	}

	@Override
	public void configure(WorldConfig config)
	{
		setConstants(config.mobSpawn.newEngineSettings.water);
	}

	@Override
	protected boolean shouldPerform()
	{
		return world.getConfig().mobSpawn.spawnAnimals;
	}

	@Override
	protected void processChunk(Chunk chunk, int x, int z, int topf)
	{
		topf--;
		if(world.getBlockIfExists(x, topf, z).getMaterial() != Material.water)
			return;
		int y = topf - world.rand.nextInt(8);

		for(int i = 0; i < 2; i++)
		{
			if(trySpawnGroupAt(x, y, z, 3))
				break;

			y = topf;
		}
	}
}
