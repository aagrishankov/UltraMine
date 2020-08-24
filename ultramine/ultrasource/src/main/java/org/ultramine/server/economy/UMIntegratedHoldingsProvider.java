package org.ultramine.server.economy;

import org.ultramine.core.economy.service.DefaultHoldingsProvider;
import org.ultramine.core.economy.holdings.HoldingsFactory;

public class UMIntegratedHoldingsProvider implements DefaultHoldingsProvider
{
	private final HoldingsFactory factory = new UMIntegratedPlayerHoldingsFactory();

	@Override
	public HoldingsFactory getDefaultHoldingsFactory()
	{
		return factory;
	}
}
