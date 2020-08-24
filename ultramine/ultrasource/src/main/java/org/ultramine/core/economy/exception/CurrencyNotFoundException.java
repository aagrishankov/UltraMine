package org.ultramine.core.economy.exception;

public class CurrencyNotFoundException extends EconomyException
{
	private final String currencyId;

	public CurrencyNotFoundException(String currencyId)
	{
		super("ultramine.economy.fail.currency_not_exists", currencyId);
		this.currencyId = currencyId;
	}

	public String getCurrencyId()
	{
		return currencyId;
	}
}
