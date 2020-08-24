package org.ultramine.server.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.ultramine.server.ConfigurationHandler;

@SideOnly(Side.SERVER)
public class WatchdogThread extends Thread
{
	private static final Logger log = LogManager.getLogger();

	private static WatchdogThread instance;
	private volatile long lastTick;
	private volatile boolean stopping;

	private WatchdogThread()
	{
		super("Watchdog Thread");
	}

	public static void doStart()
	{
		if(instance == null)
		{
			instance = new WatchdogThread();
			instance.start();
		}
	}


	public static void tick()
	{
		instance.lastTick = System.currentTimeMillis();
	}

	public static void doStop()
	{
		if(instance != null)
		{
			instance.stopping = true;
		}
	}

	@Override
	public void run()
	{
		while(!stopping)
		{
			//
			if(lastTick != 0 && System.currentTimeMillis() > lastTick + ConfigurationHandler.getServerConfig().settings.watchdogThread.timeout*1000)
			{
				log.log(Level.FATAL, "The server has stopped responding!");

				log.log(Level.FATAL, "Current Thread State:");
				ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);

				for(ThreadInfo thread : threads) //main thread first
				{
					if(thread.getThreadName().equals("Server thread"))
						displayThreadInfo(thread);
				}
				
				for(ThreadInfo thread : threads)
				{
					if(!thread.getThreadName().equals("Server thread"))
						displayThreadInfo(thread);
				}

				log.log(Level.FATAL, "------------------------------");

				if(ConfigurationHandler.getServerConfig().settings.watchdogThread.restart)
				{
					try
					{
						sleep(2000); //await log output
					} catch (InterruptedException ex) {}
					
					Thread.currentThread().interrupt();
					FMLCommonHandler.instance().handleExit(0);
				}

				break;
			}

			try
			{
				sleep(10000);
			} catch (InterruptedException ex)
			{
				// interrupt();
			}
		}
	}
	
	private static void displayThreadInfo(ThreadInfo thread)
	{
		if(thread.getThreadState() != State.WAITING)
		{
			log.log(Level.FATAL, "------------------------------");
			//
			log.log(Level.FATAL, "Current Thread: " + thread.getThreadName());
			log.log(Level.FATAL, "\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: " + thread.isInNative() + " | State: "
					+ thread.getThreadState());

			if(thread.getLockedMonitors().length != 0)
			{
				log.log(Level.FATAL, "\tThread is waiting on monitor(s):");

				for(MonitorInfo monitor : thread.getLockedMonitors())
				{
					log.log(Level.FATAL, "\t\tLocked on:" + monitor.getLockedStackFrame());
				}
			}

			log.log(Level.FATAL, "\tStack:");
			//
			StackTraceElement[] stack = thread.getStackTrace();

			for(int line = 0; line < stack.length; line++)
			{
				log.log(Level.FATAL, "\t\t" + stack[line].toString());
			}
		}
	}
}
