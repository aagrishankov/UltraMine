package net.minecraft.entity.passive;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityWaterMob extends EntityCreature implements IAnimals
{
	private static final String __OBFID = "CL_00001653";

	public EntityWaterMob(World p_i1695_1_)
	{
		super(p_i1695_1_);
	}

	public boolean canBreatheUnderwater()
	{
		return true;
	}

	public boolean getCanSpawnHere()
	{
		return this.worldObj.checkNoEntityCollision(this.boundingBox);
	}

	public int getTalkInterval()
	{
		return 120;
	}

	protected boolean canDespawn()
	{
		return true;
	}

	protected int getExperiencePoints(EntityPlayer p_70693_1_)
	{
		return 1 + this.worldObj.rand.nextInt(3);
	}

	public void onEntityUpdate()
	{
		int i = this.getAir();
		super.onEntityUpdate();

		if (this.isEntityAlive() && !this.isInWater())
		{
			--i;
			this.setAir(i);

			if (this.getAir() == -20)
			{
				this.setAir(0);
				this.attackEntityFrom(DamageSource.drown, 2.0F);
			}
		}
		else
		{
			this.setAir(300);
		}
	}
}