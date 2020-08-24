package org.ultramine.core.economy.holdings;

import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.Currency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class AbstractAsyncHoldings implements AsyncHoldings
{
	private final Holdings holdings;

	public AbstractAsyncHoldings(@Nonnull Holdings holdings)
	{
		this.holdings = holdings;
	}

	@Nonnull
	protected abstract <T> CompletableFuture<T> execute(@Nonnull Supplier<T> action);

	@Override
	public Account getAccount()
	{
		return holdings.getAccount();
	}

	@Nonnull
	@Override
	public Currency getCurrency()
	{
		return holdings.getCurrency();
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> getBalance()
	{
		return execute(holdings::getBalance);
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> setBalance(double balance, @Nullable String comment)
	{
		return execute(() -> holdings.setBalance(balance, comment));
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> deposit(double amount, @Nullable String comment)
	{
		return execute(() -> holdings.deposit(amount, comment));
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> withdrawUnchecked(double amount, @Nullable String comment)
	{
		return execute(() -> holdings.withdrawUnchecked(amount, comment));
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> withdraw(double amount, @Nullable String comment)
	{
		return execute(() -> holdings.withdraw(amount, comment));
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> transferUnchecked(@Nonnull Account to, double amount, @Nullable String comment)
	{
		return execute(() -> holdings.transferUnchecked(to, amount, comment));
	}

	@Nonnull
	@Override
	public CompletableFuture<Double> transfer(@Nonnull Account to, double amount, @Nullable String comment)
	{
		return execute(() -> holdings.transfer(to, amount, comment));
	}
}
