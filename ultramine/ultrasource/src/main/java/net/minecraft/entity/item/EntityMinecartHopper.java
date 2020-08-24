package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper
{
	private boolean isBlocked = true;
	private int transferTicker = -1;
	private static final String __OBFID = "CL_00001676";

	public EntityMinecartHopper(World p_i1720_1_)
	{
		super(p_i1720_1_);
	}

	public EntityMinecartHopper(World p_i1721_1_, double p_i1721_2_, double p_i1721_4_, double p_i1721_6_)
	{
		super(p_i1721_1_, p_i1721_2_, p_i1721_4_, p_i1721_6_);
	}

	public int getMinecartType()
	{
		return 5;
	}

	public Block func_145817_o()
	{
		return Blocks.hopper;
	}

	public int getDefaultDisplayTileOffset()
	{
		return 1;
	}

	public int getSizeInventory()
	{
		return 5;
	}

	public boolean interactFirst(EntityPlayer p_130002_1_)
	{
		if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, p_130002_1_))) return true;
		if (!this.worldObj.isRemote)
		{
			p_130002_1_.displayGUIHopperMinecart(this);
		}

		return true;
	}

	public void onActivatorRailPass(int p_96095_1_, int p_96095_2_, int p_96095_3_, boolean p_96095_4_)
	{
		boolean flag1 = !p_96095_4_;

		if (flag1 != this.getBlocked())
		{
			this.setBlocked(flag1);
		}
	}

	public boolean getBlocked()
	{
		return this.isBlocked;
	}

	public void setBlocked(boolean p_96110_1_)
	{
		this.isBlocked = p_96110_1_;
	}

	public World getWorldObj()
	{
		return this.worldObj;
	}

	public double getXPos()
	{
		return this.posX;
	}

	public double getYPos()
	{
		return this.posY;
	}

	public double getZPos()
	{
		return this.posZ;
	}

	public void onUpdate()
	{
		super.onUpdate();

		if (!this.worldObj.isRemote && this.isEntityAlive() && this.getBlocked())
		{
			--this.transferTicker;

			if (!this.canTransfer())
			{
				this.setTransferTicker(0);

				if (this.func_96112_aD())
				{
					this.setTransferTicker(4);
					this.markDirty();
				}
			}
		}
	}

	public boolean func_96112_aD()
	{
		if (TileEntityHopper.func_145891_a(this))
		{
			return true;
		}
		else
		{
			List list = this.worldObj.selectEntitiesWithinAABB(EntityItem.class, this.boundingBox.expand(0.25D, 0.0D, 0.25D), IEntitySelector.selectAnything);

			if (list.size() > 0)
			{
				TileEntityHopper.func_145898_a(this, (EntityItem)list.get(0));
			}

			return false;
		}
	}

	public void killMinecart(DamageSource p_94095_1_)
	{
		super.killMinecart(p_94095_1_);
		this.func_145778_a(Item.getItemFromBlock(Blocks.hopper), 1, 0.0F);
	}

	protected void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setInteger("TransferCooldown", this.transferTicker);
	}

	protected void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		this.transferTicker = p_70037_1_.getInteger("TransferCooldown");
	}

	public void setTransferTicker(int p_98042_1_)
	{
		this.transferTicker = p_98042_1_;
	}

	public boolean canTransfer()
	{
		return this.transferTicker > 0;
	}
}