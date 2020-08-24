package org.ultramine.server.mobspawn;

import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.WorldsConfig.WorldConfig.MobSpawn.NewEngineSettings.MonsterSettings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

@SideOnly(Side.SERVER)
public class MobSpawnerMonsters extends MobSpawner
{
	private MonsterSettings set;

	public MobSpawnerMonsters(WorldServer world)
	{
		super(world, EnumCreatureType.monster);
	}

	@Override
	public void configure(WorldConfig config)
	{
		setConstants(set = config.mobSpawn.newEngineSettings.monsters);
	}

	@Override
	protected boolean shouldPerform()
	{
		return world.getConfig().mobSpawn.spawnMonsters;
	}

	@Override
	protected int getLocalLimit()
	{
		int worldTime = (int)(world.getWorldInfo().getWorldTime() % 24000);
		boolean isDay = worldTime < 14200 || worldTime > 21800;
		return isDay ? super.getLocalLimit() : set.nightlyLocalLimit;
	}

	@Override
	protected void processChunk(Chunk chunk, int x, int z, int topf)
	{
		if(!world.getBlock(x, topf-1, z).isSideSolid(world, x, topf-1, z, ForgeDirection.UP))
			--topf;
		int worldTime = (int)(world.getWorldInfo().getWorldTime() % 24000);
		boolean isDay = (worldTime < 14200 || worldTime > 21800) && world.provider.isSurfaceWorld();
		
		int op;
		int y;
		if(isDay)
		{
			if(topf <= 3)
				return;
			op = 0;
			y = world.rand.nextInt(topf - 2);
		}
		else if(!world.provider.hasNoSky && world.rand.nextInt(3) == 0)
		{
			op = 1;
			y = topf;
		}
		else
		{
			op = 2;
			y = world.rand.nextInt(topf + 1);
		}
		
		for(int i = 0; i < 12; i++)
		{
			if(trySpawnGroupAt(x, y, z, 1))
				break;
			
			if(op == 0) y = world.rand.nextInt(topf - 2);
			else if(op == 1) break;
			else if(op == 2) y = world.rand.nextInt(topf + 1);
		}
	}
}
