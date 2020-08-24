package org.ultramine.server.util;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.google.common.collect.Queues;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public abstract class SyncServerExecutor implements Executor
{
	@Override
	public abstract void execute(Runnable toRun);

	public CompletableFuture<Void> completable(Runnable toRun)
	{
		return CompletableFuture.runAsync(toRun, this);
	}

	public void await(Runnable toRun)
	{
		completable(toRun).join();
	}

	public <T> CompletableFuture<T> completable(Supplier<T> supplier)
	{
		return CompletableFuture.supplyAsync(supplier, this);
	}

	public <T> T await(Supplier<T> supplier)
	{
		return completable(supplier).join();
	}
}
