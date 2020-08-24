package org.ultramine.core.economy.holdings;

import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.exception.InsufficientFundsException;
import org.ultramine.core.economy.exception.NegativeAmountException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.DoubleUnaryOperator;

@ThreadSafe
public class MemoryHoldings implements Holdings
{
	private static final AtomicLongFieldUpdater<MemoryHoldings> BALANCE_FIELD_UPDATER = AtomicLongFieldUpdater.newUpdater(MemoryHoldings.class, "balance_field");
	private final Account account;
	private final Currency currency;

	@SuppressWarnings("unused")
	private volatile long balance_field; // must not be used directly
	protected final double factor;

	public MemoryHoldings(@Nonnull Account account, @Nonnull Currency currency)
	{
		this.account = account;
		this.currency = currency;
		this.factor = Math.pow(10, currency.getFractionalDigits());
	}

	public long getBalanceInternal()
	{
		return BALANCE_FIELD_UPDATER.get(this);
	}

	public void setBalanceInternal(long balance)
	{
		BALANCE_FIELD_UPDATER.set(this, balance);
	}

	public void setBalanceSilently(double balance)
	{
		setBalanceInternal(floor(balance * factor));
	}

	@Nonnull
	@Override
	public Account getAccount()
	{
		return account;
	}

	@Nonnull
	@Override
	public Currency getCurrency()
	{
		return currency;
	}

	@Override
	public double getBalance()
	{
		return getBalanceInternal() / factor;
	}

	@Override
	public double setBalance(double balance, @Nullable String comment)
	{
		long lastBalance = BALANCE_FIELD_UPDATER.getAndSet(this, floor(balance * factor));
		onHoldingsBalanceChange();
		return lastBalance / factor;
	}

	@Override
	public double deposit(double amount, @Nullable String comment) throws NegativeAmountException
	{
		checkAmount(amount);
		long toAdd = floor(amount * factor);
		long ret = BALANCE_FIELD_UPDATER.updateAndGet(this, balance -> Math.addExact(balance, toAdd));
		onHoldingsBalanceChange();
		return ret / factor;
	}

	@Override
	public double withdrawUnchecked(double amount, @Nullable String comment) throws NegativeAmountException
	{
		checkAmount(amount);
		long toSubtract = ceiling(amount * factor);
		long ret = BALANCE_FIELD_UPDATER.updateAndGet(this, balance -> Math.subtractExact(balance, toSubtract));
		onHoldingsBalanceChange();
		return ret / factor;
	}

	@Override
	public double withdraw(double amount, @Nullable String comment) throws NegativeAmountException, InsufficientFundsException
	{
		checkAmount(amount);
		long toSubtract = ceiling(amount * factor);
		long ret = BALANCE_FIELD_UPDATER.updateAndGet(this, balance -> {
			long newBalance = Math.subtractExact(balance, toSubtract);
			if(newBalance < 0)
				throw new InsufficientFundsException(this, balance, amount);
			return newBalance;
		});
		onHoldingsBalanceChange();
		return ret / factor;
	}

	@Override
	public double computeBalance(DoubleUnaryOperator operation, @Nullable String comment)
	{
		long ret = BALANCE_FIELD_UPDATER.updateAndGet(this, balance -> floor(operation.applyAsDouble(balance / factor) * factor));
		onHoldingsBalanceChange();
		return ret / factor;
	}

	protected static void checkAmount(double amount)
	{
		if(amount < 0.0d)
			throw new NegativeAmountException(amount);
	}

	protected static long ceiling(double arg)
	{
		long i = (long)arg;
		return arg > (double)i ? Math.addExact(i, 1) : i;
	}

	protected static long floor(double arg)
	{
		long i = (long)arg;
		return arg < (double)i ? Math.subtractExact(i, 1) : i;
	}

	protected void onHoldingsBalanceChange()
	{

	}
}
