package org.ultramine.server.event;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class WorldUpdateObject
{
	private WorldUpdateObjectType type;
	private Entity entity;
	private TileEntity tile;
	private Block block;
	private int x;
	private int y;
	private int z;
	
	private boolean isInteracting;
	private ItemStack interactStack;
	private Block interactBlock;
	private int intx;
	private int inty;
	private int intz;

	private GameProfile blockUpdateInitiator;
	
	public WorldUpdateObject()
	{
		this.type = WorldUpdateObjectType.UNKNOWN;
	}
	
	WorldUpdateObject setType(WorldUpdateObjectType type)
	{
		this.type = type;
		return this;
	}
	
	public WorldUpdateObjectType getType()
	{
		return type;
	}
	
	void setEntity(Entity entity)
	{
		this.entity = entity;
	}
	
	public Entity getEntity()
	{
		return entity;
	}
	
	void setTileEntity(TileEntity tile)
	{
		this.tile = tile;
	}
	
	public TileEntity getTileEntity()
	{
		return tile;
	}
	
	void setInteracting(boolean isInteracting)
	{
		this.isInteracting = isInteracting;
	}
	
	public boolean isInteracting()
	{
		return isInteracting;
	}
	
	void setBlock(Block block, int x, int y, int z)
	{
		this.block = block;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	
	void setInteractBlock(ItemStack interactStack, Block interactBlock, int x, int y, int z)
	{
		this.interactStack = interactStack;
		this.interactBlock = interactBlock;
		this.intx = x;
		this.inty = y;
		this.intz = z;
	}
	
	public ItemStack getInteractStack()
	{
		return interactStack;
	}
	
	public Block getInteractBlock()
	{
		return interactBlock;
	}
	
	public int getInteractX()
	{
		return intx;
	}
	
	public int getInteractY()
	{
		return inty;
	}
	
	public int getInteractZ()
	{
		return intz;
	}

	public GameProfile getBlockUpdateInitiator()
	{
		return blockUpdateInitiator;
	}

	public void setBlockUpdateInitiator(GameProfile blockUpdateInitiator)
	{
		this.blockUpdateInitiator = blockUpdateInitiator;
	}

	public GameProfile getOwner()
	{
		switch(type)
		{
		case BLOCK_EVENT:
		case BLOCK_PENDING:
			return blockUpdateInitiator;
		case BLOCK_RANDOM:
			
			break;
		case ENTITY:
			return entity.isEntityPlayerMP() ? ((EntityPlayer)entity).getGameProfile() : entity.getObjectOwner();
		case ENTITY_WEATHER:
			break;
		case PLAYER:
			return ((EntityPlayer)getEntity()).getGameProfile();
		case TILEE_ENTITY:
			return getTileEntity().getObjectOwner();
		case UNKNOWN:
			break;
		case WEATHER:
			break;
		default:
			break;
		}
		
		return null;
	}
}
