package org.ultramine.server.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerSwingItemEvent extends PlayerEvent
{
	public PlayerSwingItemEvent(EntityPlayer player)
	{
		super(player);
	}
}
