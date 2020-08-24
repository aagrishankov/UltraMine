package org.ultramine.core.economy.holdings;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.exception.CurrencyNotSupportedException;
import org.ultramine.core.economy.exception.InsufficientFundsException;
import org.ultramine.core.economy.exception.NegativeAmountException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.function.DoubleUnaryOperator;

/**
 * Holdings represents a single balance associated with pair (account, currency). It must always be fully thread-safe, so
 * constructions like setBalance(getBalance() - ...) will not work anyway. Use {@link Holdings#computeBalance(DoubleUnaryOperator, String)}
 * if you want to perform some custom operation with balance
 */
@ThreadSafe
public interface Holdings
{
	@Nonnull
	Account getAccount();

	@Nonnull
	Currency getCurrency();

	/**
	 * Return the balance of this holdings. This method may return not relevant balance, for example, in sql-based holdings balance may be cached.
	 * So, never use constructions like setBalance(getBalance() - ...);
	 * @return the current balance of this holdings
	 */
	double getBalance();

	/**
	 * Sets this holdings balance
	 * @param balance the balance to be set
	 * @param comment the comment to be logged with this transaction
	 * @return previous balance
	 */
	double setBalance(double balance, @Nullable String comment);

	/**
	 * Deposits the specified amount to this holdings.
	 * @param amount the amount to deposit
	 * @param comment the comment to be logged with this transaction
	 * @return new balance
	 * @throws org.ultramine.core.economy.exception.NegativeAmountException if the specified amount is less than zero
	 */
	double deposit(double amount, @Nullable String comment) throws NegativeAmountException;

	/**
	 * Withdraws the specified amount from this holdings. Unlike {@link Holdings#withdraw}, the amount may be greater than
	 * the current holdings balance and after transaction balance may be negative.
	 * @param amount the amount to withdraw
	 * @param comment the comment to be logged with this transaction
	 * @return new balance
	 * @throws NegativeAmountException if the specified amount is less than zero
	 */
	double withdrawUnchecked(double amount, @Nullable String comment) throws NegativeAmountException;

	/**
	 * Withdraws the specified amount from this holdings. Unlike {@link Holdings#withdrawUnchecked}, the amount may <b>not</b>
	 * be greater than the current holdings balance: InsufficientFundsException will be thrown
	 * @param amount the amount to withdraw
	 * @param comment the comment to be logged with this transaction
	 * @return new balance
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws org.ultramine.core.economy.exception.InsufficientFundsException if the current holdings balance is less than the specified amount
	 */
	double withdraw(double amount, @Nullable String comment) throws NegativeAmountException, InsufficientFundsException;

	/**
	 * Performs custom operation on balance in thread-safe, transactional way. Use it always instead of constructions
	 * like setBalance(getBalance() - ...)
	 * @return new balance
	 * @param operation the action to be performed on a balance
	 * @param comment the comment to be logged with this transaction
	 */
	double computeBalance(DoubleUnaryOperator operation, @Nullable String comment);

	/**
	 * Sets this holdings balance
	 * @param balance the balance to be set
	 * @return previous balance
	 */
	default double setBalance(double balance)
	{
		return setBalance(balance, null);
	}

	/**
	 * Deposits the specified amount to this holdings.
	 * @param amount the amount to deposit
	 * @return new balance
	 * @throws NegativeAmountException if the specified amount is less than zero
	 */
	default double deposit(double amount) throws NegativeAmountException
	{
		return deposit(amount, null);
	}

	/**
	 * Withdraws the specified amount from this holdings. Unlike {@link Holdings#withdraw}, the amount may be greater than
	 * the current holdings balance and after transaction balance may be negative.
	 * @param amount the amount to withdraw
	 * @return new balance
	 * @throws NegativeAmountException if the specified amount is less than zero
	 */
	default double withdrawUnchecked(double amount) throws NegativeAmountException
	{
		return withdrawUnchecked(amount, null);
	}

	/**
	 * Withdraws the specified amount from this holdings. Unlike {@link Holdings#withdrawUnchecked}, the amount may <b>not</b>
	 * be greater than the current holdings balance: InsufficientFundsException will be thrown
	 * @param amount the amount to withdraw
	 * @return new balance
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws InsufficientFundsException if the current holdings balance is less than the specified amount
	 */
	default double withdraw(double amount) throws NegativeAmountException, InsufficientFundsException
	{
		return withdraw(amount, null);
	}

	/**
	 * Performs custom operation on balance in thread-safe, transactional way. Use it always instead of constructions
	 * like setBalance(getBalance() - ...). Note that {@code operation} may be executed multiple times.
	 * @return new balance
	 * @param operation the action to be performed on a balance
	 */
	default double computeBalance(DoubleUnaryOperator operation)
	{
		return computeBalance(operation, null);
	}

	/**
	 * @return true if balance is less than zero
	 */
	default boolean isNegative()
	{
		return getBalance() < 0;
	}

	/**
	 * @return true only if balance is greater or equals to the specified amount
	 */
	default boolean hasEnough(double amount)
	{
		return getBalance() >= amount;
	}

	/**
	 * Transfers the specified amount of the current {@link Currency} from this holdings to the holdings for the same
	 * currency of the destination account. Unlike {@link Holdings#transfer}, the amount may be greater than the
	 * current holdings balance and after this transaction the current holdings balance may be negative.
	 * @param to the recipient account
	 * @param amount the amount to transfer
	 * @param comment the comment to be logged with this transaction
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws org.ultramine.core.economy.exception.CurrencyNotSupportedException if the recipient account do not support the current {@link Currency}
	 */
	default double transferUnchecked(@Nonnull Account to, double amount, @Nullable String comment) throws NegativeAmountException, CurrencyNotSupportedException
	{
		Holdings holdingsTo = to.getHoldings(getCurrency());
		double ret = withdrawUnchecked(amount, comment);
		holdingsTo.deposit(amount, comment);
		return ret;
	}

	/**
	 * Transfers the specified amount of the current {@link Currency} from this holdings to the holdings for the same
	 * currency of the destination account. Unlike {@link Holdings#transfer}, the amount may be greater than the
	 * current holdings balance and after this transaction the current holdings balance may be negative.
	 * @param to the recipient account
	 * @param amount the amount to transfer
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws CurrencyNotSupportedException if the recipient account do not support the current {@link Currency}
	 */
	default double transferUnchecked(@Nonnull Account to, double amount) throws NegativeAmountException, CurrencyNotSupportedException
	{
		return transferUnchecked(to, amount, null);
	}

	/**
	 * Transfers the specified amount of the current {@link Currency} from this holdings to the holdings for the same
	 * currency of the destination account. Unlike {@link Holdings#transferUnchecked}, the amount may <b>not</b>
	 * be greater than the current holdings balance: InsufficientFundsException will be thrown
	 * @param to the recipient account
	 * @param amount the amount to transfer
	 * @param comment the comment to be logged with this transaction
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws InsufficientFundsException if the current holdings balance is less than the specified amount
	 * @throws CurrencyNotSupportedException if the recipient account do not support the current {@link Currency}
	 */
	default double transfer(@Nonnull Account to, double amount, @Nullable String comment) throws NegativeAmountException, InsufficientFundsException, CurrencyNotSupportedException
	{
		Holdings holdingsTo = to.getHoldings(getCurrency());
		double ret = withdraw(amount, comment);
		holdingsTo.deposit(amount, comment);
		return ret;
	}

	/**
	 * Transfers the specified amount of the current {@link Currency} from this holdings to the holdings for the same
	 * currency of the destination account. Unlike {@link Holdings#transferUnchecked}, the amount may <b>not</b>
	 * be greater than the current holdings balance: InsufficientFundsException will be thrown
	 * @param to the recipient account
	 * @param amount the amount to transfer
	 * @throws NegativeAmountException if the specified amount is less than zero
	 * @throws InsufficientFundsException if the current holdings balance is less than the specified amount
	 * @throws CurrencyNotSupportedException if the recipient account do not support the current {@link Currency}
	 */
	default double transfer(@Nonnull Account to, double amount) throws NegativeAmountException, InsufficientFundsException, CurrencyNotSupportedException
	{
		return transfer(to, amount, null);
	}

	/**
	 * @return true if this implementation of Holdings may block caller thread for a long time. For example, if it is SQL-based Holdings
	 */
	default boolean isAsyncPreferred()
	{
		return false;
	}

	@Nonnull
	default AsyncHoldings asAsync()
	{
		return isAsyncPreferred() ? new RealAsyncHoldings(this) : new FakeAsyncHoldings(this);
	}
}
