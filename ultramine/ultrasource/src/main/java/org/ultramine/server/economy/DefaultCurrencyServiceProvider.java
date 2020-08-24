package org.ultramine.server.economy;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.service.DefaultCurrencyService;

import javax.annotation.Nonnull;

public class DefaultCurrencyServiceProvider implements DefaultCurrencyService
{
	private final Currency currency;

	public DefaultCurrencyServiceProvider(Currency currency)
	{
		this.currency = currency;
	}

	@Nonnull
	@Override
	public Currency getDefaultCurrency()
	{
		return currency;
	}
}
