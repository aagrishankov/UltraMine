package org.ultramine.commands.basic;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.WorldConstants;
import org.ultramine.server.WorldsConfig;
import org.ultramine.server.util.SpiralCoordIterator;

import java.util.ArrayList;
import java.util.List;

public class GenWorldCommand
{
	private static final List<WorldGenerator> generators = new ArrayList<>();

	@Command(
			name = "genworld",
			group = "technical",
			permissions = {"command.technical.genworld"},
			syntax = {
					"",
					"[stop]",
					"<radius>",
					"<world> [stop]",
					"<world> <x> <z> <radius>"
			}
	)
	public static void genworld(CommandContext ctx)
	{
		WorldServer world = ctx.contains("world") ? ctx.get("world").asWorld() : ctx.getSenderAsPlayer().getServerForPlayer();

		if(ctx.getAction().equals("stop"))
		{
			stop(ctx, world);
			return;
		}

		checkNotStarted(ctx, world);
		if(!ctx.contains("radius"))
		{
			if(world.getConfig().borders == null || world.getConfig().borders.length == 0)
				ctx.failure("command.genworld.noborder");
			startBordered(ctx, world);
		}
		else
		{
			int radius = ctx.get("radius").asInt(1);
			int x = ctx.contains("x") ? ctx.get("x").asInt() : MathHelper.floor_double(ctx.getSenderAsPlayer().posX);
			int z = ctx.contains("z") ? ctx.get("z").asInt() : MathHelper.floor_double(ctx.getSenderAsPlayer().posZ);
			startRadius(ctx, world, x, z, radius);
		}

		ctx.sendMessage("command.genworld.start");
	}

	private static void startBordered(CommandContext ctx, WorldServer world)
	{
		for(WorldsConfig.WorldConfig.Border border : world.getConfig().borders)
			generators.add(new WorldGenerator(world.provider.dimensionId, border.x, border.z, border.radius));
	}

	private static void startRadius(CommandContext ctx, WorldServer world, int x, int z, int radius)
	{
		generators.add(new WorldGenerator(world.provider.dimensionId, x, z, radius));
	}

	private static void checkNotStarted(CommandContext ctx, WorldServer world)
	{
		int dim = world.provider.dimensionId;
		for(WorldGenerator gen : generators)
			if(gen.dim == dim)
				ctx.failure("command.genworld.already");
	}

	private static void stop(CommandContext ctx, WorldServer world)
	{
		int genCurrent = 0;
		int genTotal = 0;

		int dim = world.provider.dimensionId;
		for(WorldGenerator gen : new ArrayList<>(generators))
		{
			if(gen.dim == dim)
			{
				genCurrent += gen.getCurrentGen();
				genTotal += gen.getTotalGen();
				gen.stop();
			}
		}
		ctx.check(genTotal != 0, "command.genworld.stop.fail.notrun");
		ctx.sendMessage("command.genworld.stop", genCurrent, genTotal);
	}

	public static void tick()
	{
		if(generators.size() != 0)
			generators.get(0).tick();
	}

	public static class WorldGenerator
	{
		private static final int GEN_RADIUS = 4;
		private static final int OVERLAP = WorldConstants.GENCHUNK_PRELOAD_RADIUS;
		private static final int GEN_SIDE = GEN_RADIUS + GEN_RADIUS + 1;
		private static final int BULK_RADIUS = GEN_RADIUS + OVERLAP;

		private final int dim;
		private final SpiralCoordIterator iterator;
		private boolean canGenerateNow = true;

		public WorldGenerator(int dim, int x, int z, int radius)
		{
			this.dim = dim;
			int radiusGen = (radius >> 4) / GEN_SIDE;
			int sideGens = radiusGen + radiusGen + 1;
			iterator = new SpiralCoordIterator((x >> 4) / GEN_SIDE, (z >> 4) / GEN_SIDE, sideGens*sideGens);
		}

		public int getTotalGen()
		{
			return iterator.getLimit() * GEN_SIDE * GEN_SIDE;
		}

		public int getCurrentGen()
		{
			return iterator.getCounter() * GEN_SIDE * GEN_SIDE;
		}

		public void tick()
		{
			WorldServer world = MinecraftServer.getServer().getMultiWorld().getWorldByID(dim);
			if(world == null)
			{
				stop();
				return;
			}

			if(MinecraftServer.getServer().getTickCounter() % 600 == 0)
				MinecraftServer.getServer().getConfigurationManager().sendChatMsg(
						new ChatComponentTranslation("command.genworld.process", getCurrentGen(), getTotalGen()));

			while(canGenerateNow && iterator.hasNext() && world.theChunkProviderServer.unloadQueue.size() < 512)
			{
				ChunkCoordIntPair coord = iterator.next();

				int cx = coord.chunkXPos * GEN_SIDE;
				int cz = coord.chunkZPos * GEN_SIDE;
				if(world.getBorder().isChunkInsideBorder(cx, cz))
				{
					canGenerateNow = false;
					world.theChunkProviderServer.loadAsyncRadiusThenRun(cx, cz, BULK_RADIUS, () -> {
						canGenerateNow = true;
						world.theChunkProviderServer.loadAsyncRadius(cx, cz, BULK_RADIUS, c -> {
							if(c.getBindState().canUnload())
								world.theChunkProviderServer.unloadChunksIfNotNearSpawn(c.xPosition, c.zPosition);
						});
					});
					break;
				}
			}

			if(!iterator.hasNext())
				completed();
		}

		private void completed()
		{
			MinecraftServer.getServer().getConfigurationManager().sendChatMsg(
					new ChatComponentTranslation("command.genworld.complete", getCurrentGen(), getTotalGen()));
			stop();
		}

		public void stop()
		{
			generators.remove(this);
		}
	}
}
