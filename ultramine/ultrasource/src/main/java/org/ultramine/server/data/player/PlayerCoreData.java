package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import org.ultramine.core.economy.Currency;
import org.ultramine.core.economy.service.Economy;
import org.ultramine.core.economy.service.EconomyRegistry;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.Teleporter;
import org.ultramine.server.economy.CurrencyImpl;
import org.ultramine.server.economy.UMIntegratedPlayerHoldings;
import org.ultramine.server.util.WarpLocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;

public class PlayerCoreData extends PlayerDataExtension
{
	private long firstLoginTime = System.currentTimeMillis();
	private long lastLoginTime = firstLoginTime;
	private final Map<String, WarpLocation> homes = new HashMap<String, WarpLocation>();
	private final PlayerAccountHolder account;
	private long unmuteTime;
	private boolean commandsMuted;
	private boolean hidden;
	
	//undatabased
	private Teleporter teleporter;
	private long nextTeleportationTime;
	private WarpLocation lastLocation;
	private String lastMessagedPlayer;
	
	public PlayerCoreData(PlayerData data)
	{
		super(data);
		this.account = new PlayerAccountHolder(data);
	}
	
	public long getFirstLoginTime()
	{
		return firstLoginTime;
	}
	
	public long getLastLoginTime()
	{
		return lastLoginTime;
	}
	
	public void onLogin()
	{
		this.lastLoginTime = System.currentTimeMillis();
	}
	
	public WarpLocation getHome(String name)
	{
		return homes.get(name);
	}
	
	public void setHome(String name, WarpLocation home)
	{
		homes.put(name, home);
	}
	
	public Map<String, WarpLocation> getHomes()
	{
		return homes;
	}

	@Nullable
	public UMIntegratedPlayerHoldings getHoldingsInternal(Currency currency)
	{
		return account.getHoldings(currency);
	}

	public void setHoldingsInternal(UMIntegratedPlayerHoldings holdings)
	{
		account.setHoldings(holdings);
	}
	
	public Teleporter getTeleporter()
	{
		return teleporter;
	}
	
	public void setTeleporter(Teleporter teleporter)
	{
		this.teleporter = teleporter;
	}
	
	public long getNextTeleportationTime()
	{
		return nextTeleportationTime;
	}

	public void setNextTeleportationTime(long nextTeleportationTime)
	{
		this.nextTeleportationTime = nextTeleportationTime;
	}
	
	public WarpLocation getLastLocation()
	{
		return lastLocation;
	}

	public void setLastLocation(WarpLocation lastLocation)
	{
		this.lastLocation = lastLocation;
	}
	
	public String getLastMessagedPlayer()
	{
		return lastMessagedPlayer;
	}
	
	public void setLastMessagedPlayer(String lastMessagedPlayer)
	{
		this.lastMessagedPlayer = lastMessagedPlayer;
	}
	
	public void mute(long time)
	{
		if(time == Long.MAX_VALUE)
			this.unmuteTime = Long.MAX_VALUE;
		else
			this.unmuteTime = System.currentTimeMillis() + time;
	}
	
	public void mute(long time, boolean commandsMuted)
	{
		mute(time);
		this.commandsMuted = commandsMuted;
	}
	
	public void unmute()
	{
		this.unmuteTime = 0;
		this.commandsMuted = false;
	}
	
	public boolean isMuted()
	{
		if(unmuteTime == 0)
			return false;
		else if(unmuteTime == Long.MAX_VALUE)
			return true;
		else if(System.currentTimeMillis() <= unmuteTime)
			return true;
		else
		{
			unmuteTime = 0;
			commandsMuted = false;
			return false;
		}
	}
	
	public long getUnmuteTime()
	{
		return unmuteTime;
	}
	
	public boolean isCommandsMuted()
	{
		return isMuted() && commandsMuted;
	}
	
	public boolean isHidden()
	{
		return hidden;
	}
	
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setLong("flt", firstLoginTime);
		nbt.setLong("llt", lastLoginTime);
		NBTTagList homeList = new NBTTagList();
		for(Map.Entry<String, WarpLocation> ent : homes.entrySet())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			nbt1.setString("k", ent.getKey());
			nbt1.setTag("v", ent.getValue().toNBT());
			homeList.appendTag(nbt1);
		}
		nbt.setTag("homes", homeList);
		
		NBTTagCompound accnbt = new NBTTagCompound();
		account.writeToNBT(accnbt);
		nbt.setTag("acc", accnbt);
		nbt.setLong("m", unmuteTime);
		nbt.setBoolean("mc", commandsMuted);
		nbt.setBoolean("h", hidden);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("flt"))
			firstLoginTime = nbt.getLong("flt");
		lastLoginTime = nbt.getLong("llt");
		NBTTagList homeList = nbt.getTagList("homes", 10);
		for(int i = 0, s = homeList.tagCount(); i < s; i++)
		{
			NBTTagCompound nbt1 = homeList.getCompoundTagAt(i);
			homes.put(nbt1.getString("k"), WarpLocation.getFromNBT(nbt1.getCompoundTag("v")));
		}
		
		account.readFromNBT(nbt.getCompoundTag("acc"));
		unmuteTime = nbt.getLong("m");
		commandsMuted = nbt.getBoolean("mc");
		hidden = nbt.getBoolean("h");
	}

	private static class PlayerAccountHolder
	{
		@InjectService private static EconomyRegistry economyRegistry;
		@InjectService private static Economy economy;

		private final Map<String, Object> holdingsMap = new HashMap<>();
		private final PlayerData data;
		private PlayerAccountHolder(PlayerData data)
		{
			this.data = data;
		}

		@Nullable
		public UMIntegratedPlayerHoldings getHoldings(Currency currency)
		{
			Object val = holdingsMap.get(currency.getId());
			if(val instanceof UMIntegratedPlayerHoldings)
			{
				UMIntegratedPlayerHoldings holdings = (UMIntegratedPlayerHoldings) val;
				if(holdings.getCurrency() != currency)
				{
					UMIntegratedPlayerHoldings newHoldings = new UMIntegratedPlayerHoldings(holdings.getAccount(), holdings.getCurrency(), data);
					newHoldings.setBalanceInternal(holdings.getBalanceInternal());
					holdingsMap.put(currency.getId(), newHoldings);
					return newHoldings;
				}
				return holdings;
			}
			else if(val != null)
			{
				PlayerAccount realAccount = economy.getPlayerAccount(data.getProfile());
				UMIntegratedPlayerHoldings holdings = new UMIntegratedPlayerHoldings(realAccount, currency, data);
				holdings.readFromNBT((NBTTagCompound)val);
				holdingsMap.put(currency.getId(), holdings);
				return holdings;
			}

			return null;
		}

		public void setHoldings(UMIntegratedPlayerHoldings holdings)
		{
			holdingsMap.put(holdings.getCurrency().getId(), holdings);
		}

		public void writeToNBT(NBTTagCompound nbt)
		{
			for(Map.Entry<String, ?> ent : holdingsMap.entrySet())
			{
				Object val = ent.getValue();
				if(val instanceof UMIntegratedPlayerHoldings)
				{
					NBTTagCompound nbt1 = new NBTTagCompound();
					((UMIntegratedPlayerHoldings) val).writeToNBT(nbt1);
					nbt.setTag(ent.getKey(), nbt1);
				}
				else
				{
					nbt.setTag(ent.getKey(), (NBTTagCompound) val);
				}
			}
		}

		public void readFromNBT(NBTTagCompound nbt)
		{
			PlayerAccount realAccount = economy.getPlayerAccount(data.getProfile());

			for(Object code : nbt.func_150296_c())
			{
				NBTBase b = nbt.getTag((String)code);
				if(!(b instanceof NBTTagCompound))
					continue;
				Currency currency = economyRegistry.getCurrencyNullable((String)code);
				if(currency != null && currency instanceof CurrencyImpl)
				{
					UMIntegratedPlayerHoldings holdings = new UMIntegratedPlayerHoldings(realAccount, currency, data);
					holdings.readFromNBT((NBTTagCompound)b);
					holdingsMap.put(currency.getId(), holdings);
				}
				else
				{
					holdingsMap.put((String)code, nbt.getTag((String)code));
				}
			}
		}
	}
}
