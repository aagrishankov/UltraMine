package org.ultramine.server.event;

import org.ultramine.server.util.WarpLocation;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PreDimChangeEvent extends PlayerEvent
{
	private final EntityPlayerMP player;
	private int dimTo;
	private Teleporter teleporter;

	public PreDimChangeEvent(EntityPlayerMP player, int dimTo, Teleporter teleporter)
	{
		super(player);
		this.player = player;
		this.dimTo = dimTo;
		this.teleporter = teleporter;
	}
	
	public EntityPlayerMP getPlayer()
	{
		return player;
	}
	
	public int getDimFrom()
	{
		return player.dimension;
	}

	public int getDimTo()
	{
		return dimTo;
	}

	/** UNSAFE! */
	public void setDimTo(int dimTo)
	{
		this.dimTo = dimTo;
	}

	public Teleporter getTeleporter()
	{
		return teleporter;
	}

	/** UNSAFE! */
	public void setTeleporter(Teleporter teleporter)
	{
		this.teleporter = teleporter;
	}
	
	public boolean isVanilla()
	{
		return teleporter != null;
	}
	
	public void setDestination(double x, double y, double z)
	{
		setTeleporter(null);
		player.setPositionAndRotation(x, y, z, player.rotationYaw, player.rotationPitch);
	}
	
	public void setDestination(int dim, double x, double y, double z)
	{
		setTeleporter(null);
		setDimTo(dim);
		player.setPositionAndRotation(x, y, z, player.rotationYaw, player.rotationPitch);
	}
	
	public void setDestination(int dim, double x, double y, double z, float yaw, float pitch)
	{
		setTeleporter(null);
		setDimTo(dim);
		player.setPositionAndRotation(x, y, z, yaw, pitch);
	}
	
	public void setDestination(WarpLocation loc)
	{
		setTeleporter(null);
		setDimTo(loc.dimension);
		player.setPositionAndRotation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
	}
}
