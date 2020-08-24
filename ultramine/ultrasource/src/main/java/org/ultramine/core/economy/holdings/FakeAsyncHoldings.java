package org.ultramine.core.economy.holdings;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FakeAsyncHoldings extends AbstractAsyncHoldings
{
	public FakeAsyncHoldings(Holdings holdings)
	{
		super(holdings);
	}

	@Nonnull
	@Override
	protected <T> CompletableFuture<T> execute(@Nonnull Supplier<T> action)
	{
		try
		{
			return CompletableFuture.completedFuture(action.get());
		}
		catch (Throwable t)
		{
			CompletableFuture<T> future = new CompletableFuture<>();
			future.completeExceptionally(t);
			return future;
		}
	}
}
