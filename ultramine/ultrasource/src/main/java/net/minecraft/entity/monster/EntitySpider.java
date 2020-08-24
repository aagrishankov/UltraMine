package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntitySpider extends EntityMob
{
	private static final String __OBFID = "CL_00001699";

	public EntitySpider(World p_i1743_1_)
	{
		super(p_i1743_1_);
		this.setSize(1.4F, 0.9F);
	}

	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte)0));
	}

	public void onUpdate()
	{
		super.onUpdate();

		if (!this.worldObj.isRemote)
		{
			this.setBesideClimbableBlock(this.isCollidedHorizontally);
		}
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.800000011920929D);
	}

	protected Entity findPlayerToAttack()
	{
		float f = this.getBrightness(1.0F);

		if (f < 0.5F)
		{
			double d0 = 16.0D;
			return this.worldObj.getClosestVulnerablePlayerToEntity(this, d0);
		}
		else
		{
			return null;
		}
	}

	protected String getLivingSound()
	{
		return "mob.spider.say";
	}

	protected String getHurtSound()
	{
		return "mob.spider.say";
	}

	protected String getDeathSound()
	{
		return "mob.spider.death";
	}

	protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	{
		this.playSound("mob.spider.step", 0.15F, 1.0F);
	}

	protected void attackEntity(Entity p_70785_1_, float p_70785_2_)
	{
		float f1 = this.getBrightness(1.0F);

		if (f1 > 0.5F && this.rand.nextInt(100) == 0)
		{
			this.entityToAttack = null;
		}
		else
		{
			if (p_70785_2_ > 2.0F && p_70785_2_ < 6.0F && this.rand.nextInt(10) == 0)
			{
				if (this.onGround)
				{
					double d0 = p_70785_1_.posX - this.posX;
					double d1 = p_70785_1_.posZ - this.posZ;
					float f2 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
					this.motionX = d0 / (double)f2 * 0.5D * 0.800000011920929D + this.motionX * 0.20000000298023224D;
					this.motionZ = d1 / (double)f2 * 0.5D * 0.800000011920929D + this.motionZ * 0.20000000298023224D;
					this.motionY = 0.4000000059604645D;
				}
			}
			else
			{
				super.attackEntity(p_70785_1_, p_70785_2_);
			}
		}
	}

	protected Item getDropItem()
	{
		return Items.string;
	}

	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
	{
		super.dropFewItems(p_70628_1_, p_70628_2_);

		if (p_70628_1_ && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + p_70628_2_) > 0))
		{
			this.dropItem(Items.spider_eye, 1);
		}
	}

	public boolean isOnLadder()
	{
		return this.isBesideClimbableBlock();
	}

	public void setInWeb() {}

	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.ARTHROPOD;
	}

	public boolean isPotionApplicable(PotionEffect p_70687_1_)
	{
		return p_70687_1_.getPotionID() == Potion.poison.id ? false : super.isPotionApplicable(p_70687_1_);
	}

	public boolean isBesideClimbableBlock()
	{
		return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
	}

	public void setBesideClimbableBlock(boolean p_70839_1_)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);

		if (p_70839_1_)
		{
			b0 = (byte)(b0 | 1);
		}
		else
		{
			b0 &= -2;
		}

		this.dataWatcher.updateObject(16, Byte.valueOf(b0));
	}

	public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_)
	{
		Object p_110161_1_1 = super.onSpawnWithEgg(p_110161_1_);

		if (this.worldObj.rand.nextInt(100) == 0)
		{
			EntitySkeleton entityskeleton = new EntitySkeleton(this.worldObj);
			entityskeleton.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
			entityskeleton.onSpawnWithEgg((IEntityLivingData)null);
			this.worldObj.spawnEntityInWorld(entityskeleton);
			entityskeleton.mountEntity(this);
		}

		if (p_110161_1_1 == null)
		{
			p_110161_1_1 = new EntitySpider.GroupData();

			if (this.worldObj.difficultySetting == EnumDifficulty.HARD && this.worldObj.rand.nextFloat() < 0.1F * this.worldObj.func_147462_b(this.posX, this.posY, this.posZ))
			{
				((EntitySpider.GroupData)p_110161_1_1).func_111104_a(this.worldObj.rand);
			}
		}

		if (p_110161_1_1 instanceof EntitySpider.GroupData)
		{
			int i = ((EntitySpider.GroupData)p_110161_1_1).field_111105_a;

			if (i > 0 && Potion.potionTypes[i] != null)
			{
				this.addPotionEffect(new PotionEffect(i, Integer.MAX_VALUE));
			}
		}

		return (IEntityLivingData)p_110161_1_1;
	}

	public static class GroupData implements IEntityLivingData
		{
			public int field_111105_a;
			private static final String __OBFID = "CL_00001700";

			public void func_111104_a(Random p_111104_1_)
			{
				int i = p_111104_1_.nextInt(5);

				if (i <= 1)
				{
					this.field_111105_a = Potion.moveSpeed.id;
				}
				else if (i <= 2)
				{
					this.field_111105_a = Potion.damageBoost.id;
				}
				else if (i <= 3)
				{
					this.field_111105_a = Potion.regeneration.id;
				}
				else if (i <= 4)
				{
					this.field_111105_a = Potion.invisibility.id;
				}
			}
		}
}