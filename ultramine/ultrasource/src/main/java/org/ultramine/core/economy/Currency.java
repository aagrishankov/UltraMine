package org.ultramine.core.economy;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface Currency
{
	/**
	 * @return the currency's unique id
	 */
	@Nonnull
	String getId();

	/**
	 * @return the currency's display name, in singular form. Ex: Dollar.
	 */
	@Nonnull
	String getDisplayName();

	/**
	 * @return the currency's display name in plural form. Ex: Dollars.
	 */
	@Nonnull
	String getPluralDisplayName();

	/**
	 * @return the currency's symbol. Ex. $
	 */
	@Nonnull
	String getSymbol();

	/**
	 * @return number of digits after the decimal point kept or -1 if any number
	 */
	int getFractionalDigits();

	@Nonnull
	String format(double amount);
}
