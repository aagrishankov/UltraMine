package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAIOpenDoor extends EntityAIDoorInteract
{
	boolean field_75361_i;
	int field_75360_j;
	private static final String __OBFID = "CL_00001603";

	public EntityAIOpenDoor(EntityLiving p_i1644_1_, boolean p_i1644_2_)
	{
		super(p_i1644_1_);
		this.theEntity = p_i1644_1_;
		this.field_75361_i = p_i1644_2_;
	}

	public boolean continueExecuting()
	{
		return this.field_75361_i && this.field_75360_j > 0 && super.continueExecuting();
	}

	public void startExecuting()
	{
		this.field_75360_j = 20;
		this.field_151504_e.func_150014_a(this.theEntity.worldObj, this.entityPosX, this.entityPosY, this.entityPosZ, true);
	}

	public void resetTask()
	{
		if (this.field_75361_i)
		{
			this.field_151504_e.func_150014_a(this.theEntity.worldObj, this.entityPosX, this.entityPosY, this.entityPosZ, false);
		}
	}

	public void updateTask()
	{
		--this.field_75360_j;
		super.updateTask();
	}
}