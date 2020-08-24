package org.ultramine.core.economy.service;

import org.ultramine.core.economy.Currency;
import org.ultramine.core.service.Service;

import javax.annotation.Nonnull;

@Service
public interface DefaultCurrencyService
{
	@Nonnull
	Currency getDefaultCurrency();
}
