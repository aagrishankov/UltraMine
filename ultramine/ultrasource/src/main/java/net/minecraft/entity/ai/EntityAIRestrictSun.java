package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;

public class EntityAIRestrictSun extends EntityAIBase
{
	private EntityCreature theEntity;
	private static final String __OBFID = "CL_00001611";

	public EntityAIRestrictSun(EntityCreature p_i1652_1_)
	{
		this.theEntity = p_i1652_1_;
	}

	public boolean shouldExecute()
	{
		return this.theEntity.worldObj.isDaytime();
	}

	public void startExecuting()
	{
		this.theEntity.getNavigator().setAvoidSun(true);
	}

	public void resetTask()
	{
		this.theEntity.getNavigator().setAvoidSun(false);
	}
}