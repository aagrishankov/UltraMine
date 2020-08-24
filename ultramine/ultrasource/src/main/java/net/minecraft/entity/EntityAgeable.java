package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class EntityAgeable extends EntityCreature
{
	private float field_98056_d = -1.0F;
	private float field_98057_e;
	private static final String __OBFID = "CL_00001530";

	public EntityAgeable(World p_i1578_1_)
	{
		super(p_i1578_1_);
	}

	public abstract EntityAgeable createChild(EntityAgeable p_90011_1_);

	public boolean interact(EntityPlayer p_70085_1_)
	{
		ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();

		if (itemstack != null && itemstack.getItem() == Items.spawn_egg)
		{
			if (!this.worldObj.isRemote)
			{
				Class oclass = EntityList.getClassFromID(itemstack.getItemDamage());

				if (oclass != null && oclass.isAssignableFrom(this.getClass()))
				{
					EntityAgeable entityageable = this.createChild(this);

					if (entityageable != null)
					{
						entityageable.setGrowingAge(-24000);
						entityageable.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
						this.worldObj.spawnEntityInWorld(entityageable);

						if (itemstack.hasDisplayName())
						{
							entityageable.setCustomNameTag(itemstack.getDisplayName());
						}

						if (!p_70085_1_.capabilities.isCreativeMode)
						{
							--itemstack.stackSize;

							if (itemstack.stackSize <= 0)
							{
								p_70085_1_.inventory.setInventorySlotContents(p_70085_1_.inventory.currentItem, (ItemStack)null);
							}
						}
					}
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(12, new Integer(0));
	}

	public int getGrowingAge()
	{
		return this.dataWatcher.getWatchableObjectInt(12);
	}

	public void addGrowth(int p_110195_1_)
	{
		int j = this.getGrowingAge();
		j += p_110195_1_ * 20;

		if (j > 0)
		{
			j = 0;
		}

		this.setGrowingAge(j);
	}

	public void setGrowingAge(int p_70873_1_)
	{
		this.dataWatcher.updateObject(12, Integer.valueOf(p_70873_1_));
		this.setScaleForAge(this.isChild());
	}

	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setInteger("Age", this.getGrowingAge());
	}

	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		this.setGrowingAge(p_70037_1_.getInteger("Age"));
	}

	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		if (this.worldObj.isRemote)
		{
			this.setScaleForAge(this.isChild());
		}
		else
		{
			int i = this.getGrowingAge();

			if (i < 0)
			{
				++i;
				this.setGrowingAge(i);
			}
			else if (i > 0)
			{
				--i;
				this.setGrowingAge(i);
			}
		}
	}

	public boolean isChild()
	{
		return this.getGrowingAge() < 0;
	}

	public void setScaleForAge(boolean p_98054_1_)
	{
		this.setScale(p_98054_1_ ? 0.5F : 1.0F);
	}

	protected final void setSize(float p_70105_1_, float p_70105_2_)
	{
		boolean flag = this.field_98056_d > 0.0F;
		this.field_98056_d = p_70105_1_;
		this.field_98057_e = p_70105_2_;

		if (!flag)
		{
			this.setScale(1.0F);
		}
	}

	protected final void setScale(float p_98055_1_)
	{
		super.setSize(this.field_98056_d * p_98055_1_, this.field_98057_e * p_98055_1_);
	}
}