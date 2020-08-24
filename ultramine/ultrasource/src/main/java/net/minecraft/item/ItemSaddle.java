package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;

public class ItemSaddle extends Item
{
	private static final String __OBFID = "CL_00000059";

	public ItemSaddle()
	{
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.tabTransport);
	}

	public boolean itemInteractionForEntity(ItemStack p_111207_1_, EntityPlayer p_111207_2_, EntityLivingBase p_111207_3_)
	{
		if (p_111207_3_ instanceof EntityPig)
		{
			EntityPig entitypig = (EntityPig)p_111207_3_;

			if (!entitypig.getSaddled() && !entitypig.isChild())
			{
				entitypig.setSaddled(true);
				entitypig.worldObj.playSoundAtEntity(entitypig, "mob.horse.leather", 0.5F, 1.0F);
				--p_111207_1_.stackSize;
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hitEntity(ItemStack p_77644_1_, EntityLivingBase p_77644_2_, EntityLivingBase p_77644_3_)
	{
		this.itemInteractionForEntity(p_77644_1_, (EntityPlayer)null, p_77644_2_);
		return true;
	}
}