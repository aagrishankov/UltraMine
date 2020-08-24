package org.ultramine.scheduler;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ultramine.scheduler.pattern.SchedulingPattern;

import com.google.common.collect.Queues;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class Scheduler
{
	private final List<ScheduledTask> tasks = new CopyOnWriteArrayList<ScheduledTask>();
	private final Queue<Runnable> toSyncExec = Queues.newConcurrentLinkedQueue();
	private final SchedulerThread thread = new SchedulerThread();
	
	private volatile boolean isRunning = true;

	public Scheduler()
	{
		
	}
	
	public void start()
	{
		thread.setDaemon(true);
		thread.start();
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public void stop()
	{
		isRunning = false;
		thread.interrupt();
		tasks.clear();
		FMLCommonHandler.instance().bus().unregister(this);
	}
	
	public ScheduledTask scheduleSync(String pattern, Runnable run)
	{
		ScheduledSyncTask task = new ScheduledSyncTask(this, new SchedulingPattern(pattern), run);
		tasks.add(task);
		return task;
	}

	public ScheduledTask scheduleAsync(String pattern, Runnable run)
	{
		ScheduledAsyncTask task = new ScheduledAsyncTask(this, new SchedulingPattern(pattern), run);
		tasks.add(task);
		return task;
	}

	boolean cancelTask(ScheduledTask task)
	{
		return tasks.remove(task);
	}
	
	void addToSyncQueue(Runnable task)
	{
		toSyncExec.add(task);
	}
	
	private void launchTasks(long millis)
	{
		for(ScheduledTask task : tasks)
		{
			if(task.canRun(millis))
				task.launch();
		}
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == Phase.END)
		{
			for(Runnable task; (task = toSyncExec.poll()) != null;)
				task.run();
		}
	}

	private class SchedulerThread extends Thread
	{
		public SchedulerThread()
		{
			super("UM Scheduler thread");
		}
		
		private void safeSleep(long millis) throws InterruptedException
		{
			long done = 0;
			do
			{
				long before = System.currentTimeMillis();
				sleep(millis - done);
				long after = System.currentTimeMillis();
				done += (after - before);
			}
			while(done < millis);
		}

		@Override
		public void run()
		{
			long millis = System.currentTimeMillis();
			long nextMinute = ((millis / 60000) + 1) * 60000;
			while(isRunning)
			{
				long sleepTime = (nextMinute - System.currentTimeMillis());
				if(sleepTime > 0)
				{
					try
					{
						safeSleep(sleepTime);
					}
					catch(InterruptedException e)
					{
						continue;
					}
				}
				millis = System.currentTimeMillis();
				launchTasks(millis);
				nextMinute = ((millis / 60000) + 1) * 60000;
			}
		}
	}
}
