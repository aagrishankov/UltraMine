package org.ultramine.server.mobspawn;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import org.ultramine.server.WorldsConfig.WorldConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class MobSpawnerAnimals extends MobSpawner
{
	public MobSpawnerAnimals(WorldServer world)
	{
		super(world, EnumCreatureType.creature);
	}

	@Override
	public void configure(WorldConfig config)
	{
		setConstants(config.mobSpawn.newEngineSettings.animals);
	}

	@Override
	protected boolean shouldPerform()
	{
		return world.getConfig().mobSpawn.spawnAnimals && (int)(world.getWorldInfo().getWorldTime() % 24000) < 12000; //day
	}

	@Override
	protected void processChunk(Chunk chunk, int x, int z, int topf)
	{
		if(!world.getBlock(x, topf-1, z).isSideSolid(world, x, topf-1, z, ForgeDirection.UP))
			--topf;
		trySpawnGroupAt(x, topf, z, 3);
	}
}
