package org.ultramine.core.economy.exception;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.account.Account;

import javax.annotation.Nonnull;

public class AccountTypeNotSupportedException extends EconomyException
{
	@Nonnull private final Account account;
	@Nonnull private final Currency currency;

	public AccountTypeNotSupportedException(@Nonnull Account account, @Nonnull Currency currency)
	{
		super("ultramine.economy.fail.account_type_not_supported", account.getName(), currency.getId());
		this.account = account;
		this.currency = currency;
	}

	@Nonnull
	public Account getAccount()
	{
		return account;
	}

	@Nonnull
	public Currency getCurrency()
	{
		return currency;
	}
}
