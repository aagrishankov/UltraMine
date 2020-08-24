package org.ultramine.server.economy;

import net.minecraft.server.MinecraftServer;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.exception.AccountTypeNotSupportedException;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.holdings.HoldingsFactory;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.data.player.PlayerData;

import javax.annotation.Nonnull;

public class UMIntegratedPlayerHoldingsFactory implements HoldingsFactory
{
	@InjectService private static EconomyRegistry economyRegistry;

	@Nonnull
	@Override
	public Holdings createHoldings(@Nonnull Account account, @Nonnull Currency currency) throws AccountTypeNotSupportedException
	{
		if(!(account instanceof PlayerAccount))
			throw new AccountTypeNotSupportedException(account, currency);
		PlayerData data = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getPlayerData(((PlayerAccount)account).getProfile()); // TODO service
		UMIntegratedPlayerHoldings holdings = data.core().getHoldingsInternal(currency);
		if(holdings != null)
			return holdings;
		holdings = new UMIntegratedPlayerHoldings(account, currency, data);
		holdings.setBalanceSilently(economyRegistry.getStartPlayerBalance(currency));
		data.core().setHoldingsInternal(holdings);
		onHoldingsCreate(holdings);
		return holdings;
	}

	protected void onHoldingsCreate(UMIntegratedPlayerHoldings holdings)
	{

	}
}
