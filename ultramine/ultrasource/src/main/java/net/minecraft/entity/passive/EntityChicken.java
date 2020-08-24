package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityChicken extends EntityAnimal
{
	public float field_70886_e;
	public float destPos;
	public float field_70884_g;
	public float field_70888_h;
	public float field_70889_i = 1.0F;
	public int timeUntilNextEgg;
	public boolean field_152118_bv;
	private static final String __OBFID = "CL_00001639";

	public EntityChicken(World p_i1682_1_)
	{
		super(p_i1682_1_);
		this.setSize(0.3F, 0.7F);
		this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
		this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
		this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.wheat_seeds, false));
		this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
		this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
	}

	public boolean isAIEnabled()
	{
		return true;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
	}

	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		this.field_70888_h = this.field_70886_e;
		this.field_70884_g = this.destPos;
		this.destPos = (float)((double)this.destPos + (double)(this.onGround ? -1 : 4) * 0.3D);

		if (this.destPos < 0.0F)
		{
			this.destPos = 0.0F;
		}

		if (this.destPos > 1.0F)
		{
			this.destPos = 1.0F;
		}

		if (!this.onGround && this.field_70889_i < 1.0F)
		{
			this.field_70889_i = 1.0F;
		}

		this.field_70889_i = (float)((double)this.field_70889_i * 0.9D);

		if (!this.onGround && this.motionY < 0.0D)
		{
			this.motionY *= 0.6D;
		}

		this.field_70886_e += this.field_70889_i * 2.0F;

		if (!this.worldObj.isRemote && !this.isChild() && !this.func_152116_bZ() && --this.timeUntilNextEgg <= 0)
		{
			this.playSound("mob.chicken.plop", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
			this.dropItem(Items.egg, 1);
			this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
		}
	}

	protected void fall(float p_70069_1_) {}

	protected String getLivingSound()
	{
		return "mob.chicken.say";
	}

	protected String getHurtSound()
	{
		return "mob.chicken.hurt";
	}

	protected String getDeathSound()
	{
		return "mob.chicken.hurt";
	}

	protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	{
		this.playSound("mob.chicken.step", 0.15F, 1.0F);
	}

	protected Item getDropItem()
	{
		return Items.feather;
	}

	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
	{
		int j = this.rand.nextInt(3) + this.rand.nextInt(1 + p_70628_2_);

		for (int k = 0; k < j; ++k)
		{
			this.dropItem(Items.feather, 1);
		}

		if (this.isBurning())
		{
			this.dropItem(Items.cooked_chicken, 1);
		}
		else
		{
			this.dropItem(Items.chicken, 1);
		}
	}

	public EntityChicken createChild(EntityAgeable p_90011_1_)
	{
		return new EntityChicken(this.worldObj);
	}

	public boolean isBreedingItem(ItemStack p_70877_1_)
	{
		return p_70877_1_ != null && p_70877_1_.getItem() instanceof ItemSeeds;
	}

	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		this.field_152118_bv = p_70037_1_.getBoolean("IsChickenJockey");
	}

	protected int getExperiencePoints(EntityPlayer p_70693_1_)
	{
		return this.func_152116_bZ() ? 10 : super.getExperiencePoints(p_70693_1_);
	}

	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setBoolean("IsChickenJockey", this.field_152118_bv);
	}

	protected boolean canDespawn()
	{
		return this.func_152116_bZ() && this.riddenByEntity == null;
	}

	public void updateRiderPosition()
	{
		super.updateRiderPosition();
		float f = MathHelper.sin(this.renderYawOffset * (float)Math.PI / 180.0F);
		float f1 = MathHelper.cos(this.renderYawOffset * (float)Math.PI / 180.0F);
		float f2 = 0.1F;
		float f3 = 0.0F;
		this.riddenByEntity.setPosition(this.posX + (double)(f2 * f), this.posY + (double)(this.height * 0.5F) + this.riddenByEntity.getYOffset() + (double)f3, this.posZ - (double)(f2 * f1));

		if (this.riddenByEntity instanceof EntityLivingBase)
		{
			((EntityLivingBase)this.riddenByEntity).renderYawOffset = this.renderYawOffset;
		}
	}

	public boolean func_152116_bZ()
	{
		return this.field_152118_bv;
	}

	public void func_152117_i(boolean p_152117_1_)
	{
		this.field_152118_bv = p_152117_1_;
	}
}