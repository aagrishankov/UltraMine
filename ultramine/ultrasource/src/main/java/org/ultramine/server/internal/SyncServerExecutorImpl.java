package org.ultramine.server.internal;

import com.google.common.collect.Queues;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.ultramine.server.util.SyncServerExecutor;

import java.util.Queue;

public class SyncServerExecutorImpl extends SyncServerExecutor
{
	private final Queue<Runnable> queue = Queues.newConcurrentLinkedQueue();

	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
	}

	public void unregister()
	{
		FMLCommonHandler.instance().bus().unregister(this);
		queue.clear();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
		{
			for(Runnable toRun; (toRun = queue.poll()) != null;)
				toRun.run();
		}
	}

	public boolean processOneTask()
	{
		Runnable toRun = queue.poll();
		if(toRun != null)
			toRun.run();
		return toRun != null;
	}

	@Override
	public void execute(Runnable toRun)
	{
		queue.add(toRun);
	}
}
