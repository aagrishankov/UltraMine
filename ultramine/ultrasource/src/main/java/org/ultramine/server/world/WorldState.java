package org.ultramine.server.world;

public enum WorldState
{
	LOADED(true), AVAILABLE(false), HELD(false), UNREGISTERED(false);
	
	private final boolean isLoaded;
	
	private WorldState(boolean isLoaded)
	{
		this.isLoaded = isLoaded;
	}
	
	public boolean isLoaded()
	{
		return isLoaded;
	}
}
