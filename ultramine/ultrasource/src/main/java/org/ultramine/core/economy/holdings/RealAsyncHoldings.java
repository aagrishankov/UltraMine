package org.ultramine.core.economy.holdings;

import org.ultramine.server.util.GlobalExecutors;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RealAsyncHoldings extends AbstractAsyncHoldings
{
	public RealAsyncHoldings(Holdings holdings)
	{
		super(holdings);
	}

	@Nonnull
	@Override
	protected <T> CompletableFuture<T> execute(@Nonnull Supplier<T> action)
	{
		return CompletableFuture.supplyAsync(action, GlobalExecutors.cachedIO());
	}
}
