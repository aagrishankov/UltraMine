package org.ultramine.server.chunk;

import com.google.common.collect.Queues;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ChunkGenerationQueue
{
	private static final ChunkGenerationQueue INSTANCE = new ChunkGenerationQueue();
	private final Queue<QueuedChunk> queue = Queues.newArrayDeque();
	private final IntObjMap<QueuedChunk> map = HashIntObjMaps.newMutableMap();

	public static ChunkGenerationQueue instance()
	{
		return INSTANCE;
	}

	public void queueChunkGeneration(ChunkProviderServer provider, int cx, int cz, IChunkLoadCallback callback)
	{
		int key = ChunkHash.chunkToKey(cx, cz);
		QueuedChunk chunk = map.get(key);
		if(chunk != null)
		{
			chunk.callbacks.add(callback);
		}
		else
		{
			chunk = new QueuedChunk(provider, cx, cz, callback);
			map.put(key, chunk);
			queue.add(chunk);
		}
	}

	public boolean generateOneChunk()
	{
		for(QueuedChunk chunk; (chunk = queue.poll()) != null;)
		{
			map.remove(ChunkHash.chunkToKey(chunk.cx, chunk.cz));
			if(chunk.provider.isWorldUnloaded())
				continue;
			if(chunk.provider.loadAsync(chunk.cx, chunk.cz, false, chunk))
				continue;
			chunk.onChunkLoaded(chunk.provider.originalLoadChunk(chunk.cx, chunk.cz));
			return true;
		}

		return false;
	}

	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
	}

	public void unregister()
	{
		FMLCommonHandler.instance().bus().unregister(this);
		queue.clear();
		map.clear();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
			generateOneChunk();
	}

	private static class QueuedChunk implements IChunkLoadCallback
	{
		public final ChunkProviderServer provider;
		public final int cx;
		public final int cz;
		public final List<IChunkLoadCallback> callbacks = new ArrayList<>(1);

		public QueuedChunk(ChunkProviderServer provider, int cx, int cz, IChunkLoadCallback callback)
		{
			this.provider = provider;
			this.cx = cx;
			this.cz = cz;
			this.callbacks.add(callback);
		}

		@Override
		public void onChunkLoaded(Chunk chunk)
		{
			for(IChunkLoadCallback cb : callbacks)
				cb.onChunkLoaded(chunk);
		}
	}
}
