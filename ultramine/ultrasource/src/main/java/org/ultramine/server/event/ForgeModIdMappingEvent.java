package org.ultramine.server.event;

import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.eventhandler.Event;

public class ForgeModIdMappingEvent extends Event
{
	public final FMLModIdMappingEvent e;
	
	public ForgeModIdMappingEvent(FMLModIdMappingEvent e)
	{
		this.e = e;
	}
}
