package org.ultramine.core.economy.holdings;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.exception.AccountTypeNotSupportedException;

import javax.annotation.Nonnull;

public interface HoldingsFactory
{
	@Nonnull
	Holdings createHoldings(@Nonnull Account account, @Nonnull Currency currency) throws AccountTypeNotSupportedException;
}
