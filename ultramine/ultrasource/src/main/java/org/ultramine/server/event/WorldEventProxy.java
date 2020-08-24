package org.ultramine.server.event;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WorldEventProxy
{
	protected static WorldEventProxy current;
	
	public static WorldEventProxy getCurrent()
	{
		return current;
	}
	
	public World getWorld()
	{
		return null;
	}
	
	public void pushState(WorldUpdateObjectType state)
	{
	}

	public void popState()
	{
	}

	public void startEntity(Entity entity)
	{
	}

	public void startTileEntity(TileEntity tile)
	{
	}

	public void startBlock(Block block, int x, int y, int z)
	{
		startBlock(block, x, y, z, null);
	}

	public void startBlock(Block block, int x, int y, int z, GameProfile initiator)
	{
	}

	public void startNeighbor(int x, int y, int z)
	{
	}

	public void endNeighbor()
	{
	}

	public void startInteract(ItemStack stack, Block block, int x, int y, int z)
	{
	}

	public void endInteract()
	{
	}

	public GameProfile getObjectOwner()
	{
		return null;
	}
	
	public WorldUpdateObject getUpdateObject()
	{
		return null;
	}

	public boolean canChangeBlock(int x, int y, int z, Block block, int meta, int flags)
	{
		return true;
	}
}
