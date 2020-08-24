package org.ultramine.core.economy.service;

import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.service.Service;

import javax.annotation.Nonnull;

@Service
public interface DefaultHoldingsProvider
{
	@Nonnull
	HoldingsFactory getDefaultHoldingsFactory();
}
