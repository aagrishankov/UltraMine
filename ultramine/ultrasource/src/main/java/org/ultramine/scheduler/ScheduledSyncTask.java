package org.ultramine.scheduler;

import org.ultramine.scheduler.pattern.SchedulingPattern;

public class ScheduledSyncTask extends ScheduledTask
{
	ScheduledSyncTask(Scheduler sceduler, SchedulingPattern pattern, Runnable task)
	{
		super(sceduler, pattern, task);
	}

	@Override
	void launch()
	{
		sceduler.addToSyncQueue(task);
	}
}