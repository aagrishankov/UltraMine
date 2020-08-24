package org.ultramine.server.economy;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.service.DefaultCurrencyService;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.service.InjectService;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.core.util.Undoable;
import org.ultramine.core.util.UndoableValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class UMEconomyRegistry implements EconomyRegistry
{
	private static final Double DEFAULT_PLAYER_BALANCE = 0.0d;
	@InjectService private static ServiceManager services;

	private final Map<String, CurrencyImpl> currencies = new ConcurrentHashMap<>();
	private final Map<Currency, Double> startPlayerBalances = new ConcurrentHashMap<>();

	@Nonnull
	@Override
	public UndoableValue<Currency> registerCurrency(
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits,
			int priority
	) {
		return registerCurrencyInternal(id, name, pluralName, sign, fractionalDigits, priority, null);
	}

	@Override
	public UndoableValue<Currency> registerCurrency(
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits,
			int priority,
			@Nonnull HoldingsFactory factory
	) {
		factory.getClass(); // NPE
		return registerCurrencyInternal(id, name, pluralName, sign, fractionalDigits, priority, factory);
	}

	private UndoableValue<Currency> registerCurrencyInternal(
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits,
			int priority,
			@Nullable HoldingsFactory factory
	) {
		id.getClass(); // NPE
		name.getClass(); // NPE
		pluralName.getClass(); // NPE
		sign.getClass(); // NPE

		CurrencyImpl currency = new CurrencyImpl(factory, id, name, pluralName, sign, fractionalDigits);
		currencies.compute(id, (s, currency1) -> {
			if(currency1 != null)
				throw new IllegalStateException("Currency with id: " + s + " is already registered");
			return currency;
		});
		Undoable undoService = services.register(DefaultCurrencyService.class, new DefaultCurrencyServiceProvider(currency), priority);
		return UndoableValue.of(currency, Undoable.ofAll(undoService, () -> currencies.computeIfPresent(id, (s, currency1) -> currency1 == currency ? null : currency1)));
	}

	@Override
	public Currency getCurrencyNullable(@Nonnull String id)
	{
		id.getClass(); // NPE
		return currencies.get(id);
	}

	@Override
	public boolean isCurrencyRegistered(@Nonnull String id)
	{
		return currencies.containsKey(id);
	}

	@Nonnull
	@Override
	public Collection<? extends Currency> getRegisteredCurrencies()
	{
		return currencies.values();
	}

	@Override
	public Undoable registerStartPlayerBalance(Currency currency, double balance)
	{
		startPlayerBalances.compute(currency, (s, currency1) -> {
			if(currency1 != null)
				throw new IllegalStateException("Start player balance for currency with id: " + s + " is already registered");
			return balance;
		});

		return () -> startPlayerBalances.remove(currency);
	}

	@Override
	public double getStartPlayerBalance(Currency currency)
	{
		return startPlayerBalances.getOrDefault(currency, DEFAULT_PLAYER_BALANCE);
	}
}
