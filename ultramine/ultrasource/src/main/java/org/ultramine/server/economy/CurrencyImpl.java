package org.ultramine.server.economy;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.holdings.HoldingsFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class CurrencyImpl implements Currency
{
	@Nullable private final HoldingsFactory factory;
	@Nonnull private final String id;
	@Nonnull private final String name;
	@Nonnull private final String pluralName;
	@Nonnull private final String sign;
	@Nonnull private final int fractionalDigits;
	@Nonnull private final DecimalFormat format;

	public CurrencyImpl(
			@Nullable HoldingsFactory factory,
			@Nonnull String id,
			@Nonnull String name,
			@Nonnull String pluralName,
			@Nonnull String sign,
			int fractionalDigits
	) {
		this.factory = factory;
		this.id = id;
		this.name = name;
		this.pluralName = pluralName;
		this.sign = sign;
		this.fractionalDigits = fractionalDigits;

		StringBuilder sb = new StringBuilder(fractionalDigits);
		for(int i = 0; i < fractionalDigits; i++)
			sb.append("#");
		format = new DecimalFormat("#0." + sb.toString());
	}

	@Nonnull
	@Override
	public String getId()
	{
		return id;
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return name;
	}

	@Nonnull
	@Override
	public String getPluralDisplayName()
	{
		return pluralName;
	}

	@Nonnull
	@Override
	public String getSymbol()
	{
		return sign;
	}

	@Override
	public int getFractionalDigits()
	{
		return fractionalDigits;
	}

	@Nonnull
	@Override
	public String format(double amount)
	{
		return format.format(amount) + getSymbol();
	}

	@Nullable
	public HoldingsFactory getFactory()
	{
		return factory;
	}
}
