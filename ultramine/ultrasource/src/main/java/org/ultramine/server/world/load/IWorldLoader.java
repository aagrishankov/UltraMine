package org.ultramine.server.world.load;

import net.minecraft.world.WorldServer;

public interface IWorldLoader
{
	boolean hasAsyncLoadPhase();
	
	void doAsyncLoadPhase();
	
	WorldServer doLoad();
	
	void dispose();
}
