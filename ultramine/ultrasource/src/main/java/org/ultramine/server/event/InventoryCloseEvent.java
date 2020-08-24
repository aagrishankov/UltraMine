package org.ultramine.server.event;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.eventhandler.Event;

public class InventoryCloseEvent extends Event
{
	public final EntityPlayerMP player;
	
	public InventoryCloseEvent(EntityPlayerMP player)
	{
		this.player = player;
	}
}
