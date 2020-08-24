package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

public abstract class EntityAITarget extends EntityAIBase
{
	protected EntityCreature taskOwner;
	protected boolean shouldCheckSight;
	private boolean nearbyOnly;
	private int targetSearchStatus;
	private int targetSearchDelay;
	private int field_75298_g;
	private static final String __OBFID = "CL_00001626";

	public EntityAITarget(EntityCreature p_i1669_1_, boolean p_i1669_2_)
	{
		this(p_i1669_1_, p_i1669_2_, false);
	}

	public EntityAITarget(EntityCreature p_i1670_1_, boolean p_i1670_2_, boolean p_i1670_3_)
	{
		this.taskOwner = p_i1670_1_;
		this.shouldCheckSight = p_i1670_2_;
		this.nearbyOnly = p_i1670_3_;
	}

	public boolean continueExecuting()
	{
		EntityLivingBase entitylivingbase = this.taskOwner.getAttackTarget();

		if (entitylivingbase == null)
		{
			return false;
		}
		else if (!entitylivingbase.isEntityAlive())
		{
			return false;
		}
		else
		{
			double d0 = this.getTargetDistance();

			if (this.taskOwner.getDistanceSqToEntity(entitylivingbase) > d0 * d0)
			{
				return false;
			}
			else
			{
				if (this.shouldCheckSight)
				{
					if (this.taskOwner.getEntitySenses().canSee(entitylivingbase))
					{
						this.field_75298_g = 0;
					}
					else if (++this.field_75298_g > 60)
					{
						return false;
					}
				}

				return !(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP)entitylivingbase).theItemInWorldManager.isCreative();
			}
		}
	}

	protected double getTargetDistance()
	{
		IAttributeInstance iattributeinstance = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange);
		return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
	}

	public void startExecuting()
	{
		this.targetSearchStatus = 0;
		this.targetSearchDelay = 0;
		this.field_75298_g = 0;
	}

	public void resetTask()
	{
		this.taskOwner.setAttackTarget((EntityLivingBase)null);
	}

	protected boolean isSuitableTarget(EntityLivingBase p_75296_1_, boolean p_75296_2_)
	{
		if (p_75296_1_ == null)
		{
			return false;
		}
		else if (p_75296_1_ == this.taskOwner)
		{
			return false;
		}
		else if (!p_75296_1_.isEntityAlive())
		{
			return false;
		}
		else if (!this.taskOwner.canAttackClass(p_75296_1_.getClass()))
		{
			return false;
		}
		else
		{
			if (this.taskOwner instanceof IEntityOwnable && StringUtils.isNotEmpty(((IEntityOwnable)this.taskOwner).func_152113_b()))
			{
				if (p_75296_1_ instanceof IEntityOwnable && ((IEntityOwnable)this.taskOwner).func_152113_b().equals(((IEntityOwnable)p_75296_1_).func_152113_b()))
				{
					return false;
				}

				if (p_75296_1_ == ((IEntityOwnable)this.taskOwner).getOwner())
				{
					return false;
				}
			}
			else if (p_75296_1_ instanceof EntityPlayer && !p_75296_2_ && ((EntityPlayer)p_75296_1_).capabilities.disableDamage)
			{
				return false;
			}

			if (!this.taskOwner.isWithinHomeDistance(MathHelper.floor_double(p_75296_1_.posX), MathHelper.floor_double(p_75296_1_.posY), MathHelper.floor_double(p_75296_1_.posZ)))
			{
				return false;
			}
			else if (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(p_75296_1_))
			{
				return false;
			}
			else
			{
				if (this.nearbyOnly)
				{
					if (--this.targetSearchDelay <= 0)
					{
						this.targetSearchStatus = 0;
					}

					if (this.targetSearchStatus == 0)
					{
						this.targetSearchStatus = this.canEasilyReach(p_75296_1_) ? 1 : 2;
					}

					if (this.targetSearchStatus == 2)
					{
						return false;
					}
				}

				return true;
			}
		}
	}

	private boolean canEasilyReach(EntityLivingBase p_75295_1_)
	{
		this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
		PathEntity pathentity = this.taskOwner.getNavigator().getPathToEntityLiving(p_75295_1_);

		if (pathentity == null)
		{
			return false;
		}
		else
		{
			PathPoint pathpoint = pathentity.getFinalPathPoint();

			if (pathpoint == null)
			{
				return false;
			}
			else
			{
				int i = pathpoint.xCoord - MathHelper.floor_double(p_75295_1_.posX);
				int j = pathpoint.zCoord - MathHelper.floor_double(p_75295_1_.posZ);
				return (double)(i * i + j * j) <= 2.25D;
			}
		}
	}
}