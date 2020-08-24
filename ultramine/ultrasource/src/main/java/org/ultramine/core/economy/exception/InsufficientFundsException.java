package org.ultramine.core.economy.exception;

import org.ultramine.core.economy.holdings.Holdings;

/**
 * The exception thrown if you try to withdraw more than account contains
 */
public class InsufficientFundsException extends EconomyException
{
	private final Holdings holdings;
	private final double balance;
	private final double amount;

	public InsufficientFundsException(Holdings holdings, double balance, double amount)
	{
		super("ultramine.economy.fail.insufficient_funds", balance, amount);
		this.holdings = holdings;
		this.balance = balance;
		this.amount = amount;
	}

	public Holdings getHoldings()
	{
		return holdings;
	}

	public double getBalance()
	{
		return balance;
	}

	public double getAmount()
	{
		return amount;
	}
}
