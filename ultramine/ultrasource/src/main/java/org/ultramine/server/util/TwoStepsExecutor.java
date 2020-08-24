package org.ultramine.server.util;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Function;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class TwoStepsExecutor
{
	private final Executor exec;
	private final Queue<Runnable> queue = Queues.newConcurrentLinkedQueue();
	
	public TwoStepsExecutor(Executor exec)
	{
		this.exec = exec;
	}
	
	public TwoStepsExecutor(String threadNameFormat)
	{
		this(Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(threadNameFormat).setDaemon(true).build()));
	}
	
	public void register()
	{
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public void unregister()
	{
		FMLCommonHandler.instance().bus().unregister(this);
	}
	
	public <T> void execute(final Supplier<T> async, final Consumer<T> sync)
	{
		exec.execute(() -> queue.add(new CallbackDataStruct<T>(sync, async.get())));
	}
	
	@Deprecated
	public <P, R> void execute(final Function<P, R> async, final Function<R, Void> sync)
	{
		execute(null, async, sync);
	}
	
	@Deprecated
	public <P, R> void execute(final P param, final Function<P, R> async, final Function<R, Void> sync)
	{
		exec.execute(() -> queue.add(new OldCallbackDataStruct<R>(sync, async.apply(param))));
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			for(Runnable toRun; (toRun = queue.poll()) != null;)
				toRun.run();
		}
	}
	
	private static class CallbackDataStruct<T> implements Runnable
	{
		private Consumer<T> callback;
		private T param;
		
		public CallbackDataStruct(Consumer<T> callback, T param)
		{
			this.callback = callback;
			this.param = param;
		}
		
		public void run()
		{
			callback.accept(param);
		}
	}
	
	private static class OldCallbackDataStruct<T> implements Runnable
	{
		private Function<T, Void> callback;
		private T param;
		
		public OldCallbackDataStruct(Function<T, Void> callback, T param)
		{
			this.callback = callback;
			this.param = param;
		}
		
		public void run()
		{
			callback.apply(param);
		}
	}
}
