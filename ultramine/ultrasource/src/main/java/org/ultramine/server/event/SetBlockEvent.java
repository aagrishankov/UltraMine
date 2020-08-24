package org.ultramine.server.event;

import net.minecraft.block.Block;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public class SetBlockEvent extends Event
{
	public final WorldServer world;
	public final int x;
	public final int y;
	public final int z;
	
	public final Block newBlock;
	public final int newMeta;
	
	public final WorldUpdateObject initiator;
	public final boolean isNeighborChange;
	public final boolean isNeighborChangeItself;

	public SetBlockEvent(WorldServer world, int x, int y, int z, Block newBlock, int newMeta, WorldUpdateObject initiator, boolean isNeighborChange, boolean isNeighborChangeItself)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.newBlock = newBlock;
		this.newMeta = newMeta;
		this.initiator = initiator;
		this.isNeighborChange = isNeighborChange;
		this.isNeighborChangeItself = isNeighborChangeItself;
	}
}
