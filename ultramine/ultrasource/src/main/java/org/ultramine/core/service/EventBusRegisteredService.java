package org.ultramine.core.service;

import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraftforge.common.MinecraftForge;

public abstract class EventBusRegisteredService implements ServiceStateHandler
{
	private final EventBus[] buses;

	public EventBusRegisteredService(EventBus... buses)
	{
		this.buses = buses;
	}

	public EventBusRegisteredService()
	{
		this(MinecraftForge.EVENT_BUS);
	}

	@Override
	public void onEnabled()
	{
		for(EventBus bus : buses)
			bus.register(this);
	}

	@Override
	public void onDisabled()
	{
		for(EventBus bus : buses)
			bus.unregister(this);
	}
}
