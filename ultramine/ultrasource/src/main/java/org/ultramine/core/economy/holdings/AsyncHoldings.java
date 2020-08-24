package org.ultramine.core.economy.holdings;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.account.Account;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;

@ThreadSafe
public interface AsyncHoldings
{
	@Nonnull Account getAccount();

	@Nonnull Currency getCurrency();

	@Nonnull CompletableFuture<Double> getBalance();

	@Nonnull CompletableFuture<Double> setBalance(double balance, @Nullable String comment);

	@Nonnull CompletableFuture<Double> deposit(double amount, @Nullable String comment);

	@Nonnull CompletableFuture<Double> withdrawUnchecked(double amount, @Nullable String comment);

	@Nonnull CompletableFuture<Double> withdraw(double amount, @Nullable String comment);

	@Nonnull CompletableFuture<Double> transferUnchecked(Account to, double amount, @Nullable String comment);

	@Nonnull CompletableFuture<Double> transfer(@Nonnull Account to, double amount, @Nullable String comment);

	@Nonnull default CompletableFuture<Double> setBalance(double balance)
	{
		return setBalance(balance, null);
	}

	@Nonnull default CompletableFuture<Double> deposit(double amount)
	{
		return deposit(amount, null);
	}

	@Nonnull default CompletableFuture<Double> withdrawUnchecked(double amount)
	{
		return withdrawUnchecked(amount, null);
	}

	@Nonnull default CompletableFuture<Double> withdraw(double amount)
	{
		return withdraw(amount, null);
	}

	@Nonnull default CompletableFuture<Double> transferUnchecked(@Nonnull Account to, double amount)
	{
		return transferUnchecked(to, amount, null);
	}

	@Nonnull default CompletableFuture<Double> transfer(@Nonnull Account to, double amount)
	{
		return transfer(to, amount, null);
	}
}
