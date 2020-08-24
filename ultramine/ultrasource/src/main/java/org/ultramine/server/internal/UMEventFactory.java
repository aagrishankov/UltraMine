package org.ultramine.server.internal;

import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import org.ultramine.server.event.HangingEvent;
import org.ultramine.server.event.InventoryCloseEvent;
import org.ultramine.server.event.PlayerDeathEvent;

public class UMEventFactory
{
	public static void fireInventoryClose(EntityPlayerMP player)
	{
		MinecraftForge.EVENT_BUS.post(new InventoryCloseEvent(player));
	}
	
	public static boolean fireHangingBreak(EntityHanging entity, DamageSource source)
	{
		return MinecraftForge.EVENT_BUS.post(new HangingEvent.HangingBreakEvent(entity, source));
	}

	public static PlayerDeathEvent firePlayerDeath(EntityPlayerMP player, DamageSource damageSource, IChatComponent deathMessage, boolean keepInv)
	{
		PlayerDeathEvent event = new PlayerDeathEvent(player, damageSource, deathMessage, keepInv);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
}
