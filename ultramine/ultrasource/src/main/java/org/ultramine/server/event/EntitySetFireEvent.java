package org.ultramine.server.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

@Cancelable
public class EntitySetFireEvent extends EntityEvent
{
	public int fireTicks;

	public EntitySetFireEvent(Entity entity, int fireTicks)
	{
		super(entity);
		this.fireTicks = fireTicks;
	}
}
