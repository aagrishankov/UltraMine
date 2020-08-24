package org.ultramine.server.mobspawn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.WorldsConfig.WorldConfig.MobSpawn.NewEngineSettings.PerTypeMobSpawnSettings;
import org.ultramine.server.chunk.ChunkHash;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.openhft.koloboke.collect.map.IntByteCursor;

@SideOnly(Side.SERVER)
public abstract class MobSpawner
{
	private static final Logger log = LogManager.getLogger();
	protected final WorldServer world;
	protected final EnumCreatureType type;
	protected PerTypeMobSpawnSettings set;

	private final TIntList chunks = new TIntArrayList();
	private int listIndex;
	private int perTickLimit;
	private long nextPerformTick;
	
	public MobSpawner(WorldServer world, EnumCreatureType type)
	{
		this.world = world;
		this.type = type;
	}

	protected void setConstants(PerTypeMobSpawnSettings set)
	{
		this.set = set;
	}

	public abstract void configure(WorldConfig config);

	protected int getLocalLimit()
	{
		return set.localLimit;
	}

	protected void rebuildList()
	{
		listIndex = 0;
		chunks.clear();
		for(IntByteCursor it = world.getActiveChunkSet().cursor(); it.moveNext();)
		{
			int prior = it.value();
			if(prior >= set.minRadius && prior <= set.maxRadius)
				chunks.add(it.key());
		}
		chunks.shuffle(world.rand);
		perTickLimit = Math.min(40, chunks.size() / set.performInterval + 1);
	}

	protected abstract boolean shouldPerform();

	public void performSpawn(long currentTick)
	{
		if(set != null && set.enabled && shouldPerform())
		{
			if(listIndex == chunks.size())
			{
				if(currentTick >= nextPerformTick)
				{
					rebuildList();
					nextPerformTick = currentTick + set.performInterval;
				}
				else
				{
					return;
				}
			}

			for(int i = 0, s = chunks.size(); listIndex < s && i < perTickLimit; listIndex++, i++)
			{
				int key = chunks.get(listIndex);
				int cx = ChunkHash.keyToX(key);
				int cz = ChunkHash.keyToZ(key);
				
				if(world.countEntitiesByType(type, cx, cz, set.localCheckRadius) < getLocalLimit())
				{
					Chunk chunk = world.getChunkIfExists(cx, cz);
					int randX = world.rand.nextInt(16);
					int randZ = world.rand.nextInt(16);
					int topf = chunk.getHeightValue(randX, randZ);
					if(topf > 1)
						processChunk(chunk, (cx << 4) + randX, (cz << 4) + randZ, chunk.getHeightValue(randX, randZ));
				}
			}
		}
	}

	protected abstract void processChunk(Chunk chunk, int x, int z, int topf);

	/**
	 * @return true, если больше не следует предпринимать попыток спавнить мобов в данном чанке
	 */
	protected boolean trySpawnGroupAt(int x, int y, int z, int groupSize)
	{
		if(!isApplicableForSpawn(type, x, y, z))
			return false;
		SpawnListEntry spawn = world.spawnRandomCreature(type, x, y, z);
		if(spawn == null)
			return true;
		IEntityLivingData data = null;
		int spawned = 0;
		label:
		while(true)
		{
			if(spawn == null)
				spawn = world.spawnRandomCreature(type, x, y, z);
			EntityLiving entity = spawn == null ? null : createEntityByType(spawn);
			if(entity == null)
				return true;

			entity.setLocationAndAngles(x + 0.5, y, z + 0.5, world.rand.nextFloat() * 360.0F, 0.0F);

			Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, x + 0.5F, y, z + 0.5F);
			if(canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entity.getCanSpawnHere()))
			{
				world.spawnEntityInWorld(entity);
				data = creatureSpecificInit(data, entity, x, y, z);
				spawned++;
				if(spawned == groupSize || spawned >= ForgeEventFactory.getMaxSpawnPackSize(entity))
					return true;
	
				for(int i = 0; i < 4; i++)
				{
					x += world.rand.nextInt(3) - 1;
					z += world.rand.nextInt(3) - 1;
					if(isApplicableForSpawn(type, x, y, z))
						continue label;
				}
			}

			break;
		}
		
		return spawned != 0;
	}

	protected boolean isApplicableForSpawn(EnumCreatureType type, int x, int y, int z)
	{
		if(set.minPlayerDistance > 0 && world.getClosestPlayer(x, y, z, set.minPlayerDistance) != null)
			return false;
		Block block = world.getBlockIfExists(x, y, z);
		return (!block.isNormalCube() && (block.getMaterial() == type.getCreatureMaterial() || block == Blocks.snow_layer && type == EnumCreatureType.monster)) &&
				(type == EnumCreatureType.ambient || SpawnerAnimals.canCreatureTypeSpawnAtLocation(type, world, x, y, z));
	}

	@SuppressWarnings("unchecked")
	protected EntityLiving createEntityByType(SpawnListEntry type)
	{
        try
        {
            return (EntityLiving)type.entityClass.getConstructor(World.class).newInstance(world);
        }
        catch (Exception e)
        {
        	log.error("Failed to create entity instance", e);
            return null;
        }
	}

	protected IEntityLivingData creatureSpecificInit(IEntityLivingData data, EntityLiving entity, int x, int y, int z)
	{
		if (!ForgeEventFactory.doSpecialSpawn(entity, world, x + 0.5F, y, z + 0.5F))
		{
			return entity.onSpawnWithEgg(data);
		}

		return data;
	}
}
