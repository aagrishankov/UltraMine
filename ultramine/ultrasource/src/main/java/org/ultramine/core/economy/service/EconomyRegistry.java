package org.ultramine.core.economy.service;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.exception.CurrencyNotFoundException;
import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.service.Service;
import org.ultramine.core.util.Undoable;
import org.ultramine.core.util.UndoableValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

@Service
@ThreadSafe
public interface EconomyRegistry
{
	@Nonnull
	UndoableValue<Currency> registerCurrency(
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits,
			int priority
	);

	@Nonnull
	UndoableValue<Currency> registerCurrency(
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits,
			int priority,
			@Nonnull HoldingsFactory factory
	);

	@Nullable
	Currency getCurrencyNullable(@Nonnull String id);

	boolean isCurrencyRegistered(@Nonnull String id);

	@Nonnull
	Collection<? extends Currency> getRegisteredCurrencies();

	@Nonnull
	default Currency getCurrency(@Nonnull String id) throws CurrencyNotFoundException
	{
		Currency currency = getCurrencyNullable(id);
		if(currency == null)
			throw new CurrencyNotFoundException(id);
		return currency;
	}

	Undoable registerStartPlayerBalance(Currency currency, double balance);

	double getStartPlayerBalance(Currency currency);
}
