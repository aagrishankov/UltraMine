package net.minecraft.entity.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAITasks
{
	private static final Logger logger = LogManager.getLogger();
	public List taskEntries = new ArrayList();
	private List executingTaskEntries = new ArrayList();
	private final Profiler theProfiler;
	private int tickCount;
	private int tickRate = 3;
	private static final String __OBFID = "CL_00001588";

	public EntityAITasks(Profiler p_i1628_1_)
	{
		this.theProfiler = p_i1628_1_;
	}

	public void addTask(int p_75776_1_, EntityAIBase p_75776_2_)
	{
		this.taskEntries.add(new EntityAITasks.EntityAITaskEntry(p_75776_1_, p_75776_2_));
	}

	public void removeTask(EntityAIBase p_85156_1_)
	{
		Iterator iterator = this.taskEntries.iterator();

		while (iterator.hasNext())
		{
			EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
			EntityAIBase entityaibase1 = entityaitaskentry.action;

			if (entityaibase1 == p_85156_1_)
			{
				if (this.executingTaskEntries.contains(entityaitaskentry))
				{
					entityaibase1.resetTask();
					this.executingTaskEntries.remove(entityaitaskentry);
				}

				iterator.remove();
			}
		}
	}

	public void onUpdateTasks()
	{
		ArrayList arraylist = new ArrayList();
		Iterator iterator;
		EntityAITasks.EntityAITaskEntry entityaitaskentry;

		if (this.tickCount++ % this.tickRate == 0)
		{
			iterator = this.taskEntries.iterator();

			while (iterator.hasNext())
			{
				entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
				boolean flag = this.executingTaskEntries.contains(entityaitaskentry);

				if (flag)
				{
					if (this.canUse(entityaitaskentry) && this.canContinue(entityaitaskentry))
					{
						continue;
					}

					entityaitaskentry.action.resetTask();
					this.executingTaskEntries.remove(entityaitaskentry);
				}

				if (this.canUse(entityaitaskentry) && entityaitaskentry.action.shouldExecute())
				{
					arraylist.add(entityaitaskentry);
					this.executingTaskEntries.add(entityaitaskentry);
				}
			}
		}
		else
		{
			iterator = this.executingTaskEntries.iterator();

			while (iterator.hasNext())
			{
				entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();

				if (!entityaitaskentry.action.continueExecuting())
				{
					entityaitaskentry.action.resetTask();
					iterator.remove();
				}
			}
		}

		this.theProfiler.startSection("goalStart");
		iterator = arraylist.iterator();

		while (iterator.hasNext())
		{
			entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
			this.theProfiler.startSection(entityaitaskentry.action.getClass().getSimpleName());
			entityaitaskentry.action.startExecuting();
			this.theProfiler.endSection();
		}

		this.theProfiler.endSection();
		this.theProfiler.startSection("goalTick");
		iterator = this.executingTaskEntries.iterator();

		while (iterator.hasNext())
		{
			entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
			entityaitaskentry.action.updateTask();
		}

		this.theProfiler.endSection();
	}

	private boolean canContinue(EntityAITasks.EntityAITaskEntry p_75773_1_)
	{
		this.theProfiler.startSection("canContinue");
		boolean flag = p_75773_1_.action.continueExecuting();
		this.theProfiler.endSection();
		return flag;
	}

	private boolean canUse(EntityAITasks.EntityAITaskEntry p_75775_1_)
	{
		this.theProfiler.startSection("canUse");
		Iterator iterator = this.taskEntries.iterator();

		while (iterator.hasNext())
		{
			EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();

			if (entityaitaskentry != p_75775_1_)
			{
				if (p_75775_1_.priority >= entityaitaskentry.priority)
				{
					if (this.executingTaskEntries.contains(entityaitaskentry) && !this.areTasksCompatible(p_75775_1_, entityaitaskentry))
					{
						this.theProfiler.endSection();
						return false;
					}
				}
				else if (this.executingTaskEntries.contains(entityaitaskentry) && !entityaitaskentry.action.isInterruptible())
				{
					this.theProfiler.endSection();
					return false;
				}
			}
		}

		this.theProfiler.endSection();
		return true;
	}

	private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry p_75777_1_, EntityAITasks.EntityAITaskEntry p_75777_2_)
	{
		return (p_75777_1_.action.getMutexBits() & p_75777_2_.action.getMutexBits()) == 0;
	}

	public class EntityAITaskEntry
	{
		public EntityAIBase action;
		public int priority;
		private static final String __OBFID = "CL_00001589";

		public EntityAITaskEntry(int p_i1627_2_, EntityAIBase p_i1627_3_)
		{
			this.priority = p_i1627_2_;
			this.action = p_i1627_3_;
		}
	}
}