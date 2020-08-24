package net.minecraftforge.common.chunkio;

import org.ultramine.server.chunk.ChunkLoadCallbackRunnable;
import org.ultramine.server.chunk.IChunkLoadCallback;

import net.minecraftforge.common.util.AsynchronousExecutor;

public class ChunkIOExecutor {
	static final int BASE_THREADS = 1;
	static final int PLAYERS_PER_THREAD = 50;

	private static final AsynchronousExecutor<QueuedChunk, net.minecraft.world.chunk.Chunk, IChunkLoadCallback, RuntimeException> instance = new AsynchronousExecutor<QueuedChunk, net.minecraft.world.chunk.Chunk, IChunkLoadCallback, RuntimeException>(new ChunkIOProvider(), BASE_THREADS);

	public static net.minecraft.world.chunk.Chunk syncChunkLoad(net.minecraft.world.World world, net.minecraft.world.chunk.storage.AnvilChunkLoader loader, net.minecraft.world.gen.ChunkProviderServer provider, int x, int z) {
		return instance.getSkipQueue(new QueuedChunk(x, z, loader, world, provider));
	}

	public static void queueChunkLoad(net.minecraft.world.World world, net.minecraft.world.chunk.storage.AnvilChunkLoader loader, net.minecraft.world.gen.ChunkProviderServer provider, int x, int z, Runnable runnable) {
		queueChunkLoad(world, loader, provider, x, z, new ChunkLoadCallbackRunnable(runnable));
	}
	
	public static void queueChunkLoad(net.minecraft.world.World world, net.minecraft.world.chunk.storage.AnvilChunkLoader loader, net.minecraft.world.gen.ChunkProviderServer provider, int x, int z, IChunkLoadCallback callback) {
		instance.add(new QueuedChunk(x, z, loader, world, provider), callback);
	}

	// Abuses the fact that hashCode and equals for QueuedChunk only use world and coords
	public static void dropQueuedChunkLoad(net.minecraft.world.World world, int x, int z, Runnable runnable) {
		instance.drop(new QueuedChunk(x, z, null, world, null), new ChunkLoadCallbackRunnable(runnable));
	}

	public static void adjustPoolSize(int players) {
		int size = Math.max(BASE_THREADS, (int) Math.ceil(players / PLAYERS_PER_THREAD));
		instance.setActiveThreads(size);
	}

	public static void tick() {
		instance.finishActive();
	}
}