package org.ultramine.server.economy;

import com.mojang.authlib.GameProfile;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.service.DefaultCurrencyService;
import org.ultramine.core.economy.service.DefaultHoldingsProvider;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.economy.exception.CurrencyNotFoundException;
import org.ultramine.core.economy.exception.CurrencyNotSupportedException;
import org.ultramine.core.service.InjectService;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class PlayerAccountImpl implements PlayerAccount
{
	@InjectService private static EconomyRegistry economyRegistry;
	@InjectService private static DefaultHoldingsProvider defaultHoldings;
	@InjectService private static DefaultCurrencyService defaultCurrency;

	private final GameProfile profile;
	private final Map<Currency, Holdings> holdingsMap = new ConcurrentHashMap<>();

	public PlayerAccountImpl(GameProfile profile)
	{
		this.profile = profile;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return profile.getName();
	}

	@Nonnull
	@Override
	public Collection<? extends Currency> getSupportedCurrencies()
	{
		return economyRegistry.getRegisteredCurrencies();
	}

	@Override
	public boolean isCurrencySupported(@Nonnull Currency currency)
	{
		return economyRegistry.isCurrencyRegistered(currency.getId());
	}

	@Nonnull
	@Override
	public Currency getSupportedCurrency(@Nonnull String id) throws CurrencyNotFoundException, CurrencyNotSupportedException
	{
		return economyRegistry.getCurrency(id);
	}

	@Nonnull
	@Override
	public Holdings getHoldings(@Nonnull Currency currency)
	{
		return holdingsMap.computeIfAbsent(currency, id -> {
			HoldingsFactory factory = ((CurrencyImpl)currency).getFactory();
			if(factory == null)
				factory = defaultHoldings.getDefaultHoldingsFactory();
			return factory.createHoldings(this, currency);
		});
	}

	@Nonnull
	@Override
	public Holdings getDefaultHoldings()
	{
		return getHoldings(defaultCurrency.getDefaultCurrency());
	}

	@Nonnull
	@Override
	public GameProfile getProfile()
	{
		return profile;
	}
}
