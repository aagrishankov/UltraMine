package org.ultramine.server.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerSneakingEvent extends PlayerEvent
{
	public PlayerSneakingEvent(EntityPlayer player)
	{
		super(player);
	}
}
