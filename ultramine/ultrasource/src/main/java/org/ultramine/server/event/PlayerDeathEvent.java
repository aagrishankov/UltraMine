package org.ultramine.server.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;

public class PlayerDeathEvent extends PlayerEvent
{
	public final DamageSource damageSource;
	private @Nullable IChatComponent deathMessage;
	private boolean keepInventory;
	private boolean processDrops = true;

	public PlayerDeathEvent(EntityPlayer player, DamageSource damageSource, IChatComponent deathMessage, boolean keepInventory)
	{
		super(player);
		this.damageSource = damageSource;
		this.deathMessage = deathMessage;
		this.keepInventory = keepInventory;
	}

	public @Nullable IChatComponent getDeathMessage()
	{
		return deathMessage;
	}

	public void setDeathMessage(@Nullable IChatComponent deathMessage)
	{
		this.deathMessage = deathMessage;
	}

	public boolean isKeepInventory()
	{
		return keepInventory;
	}

	public void setKeepInventory(boolean keepInventory)
	{
		this.keepInventory = keepInventory;
	}

	public boolean isProcessDrops()
	{
		return processDrops;
	}

	public void setProcessDrops(boolean processDrops)
	{
		this.processDrops = processDrops;
	}
}
