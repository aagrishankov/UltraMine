package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class ItemBucketMilk extends Item
{
	private static final String __OBFID = "CL_00000048";

	public ItemBucketMilk()
	{
		this.setMaxStackSize(1);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_)
	{
		if (!p_77654_3_.capabilities.isCreativeMode)
		{
			--p_77654_1_.stackSize;
		}

		if (!p_77654_2_.isRemote)
		{
			p_77654_3_.curePotionEffects(p_77654_1_);
		}

		return p_77654_1_.stackSize <= 0 ? new ItemStack(Items.bucket) : p_77654_1_;
	}

	public int getMaxItemUseDuration(ItemStack p_77626_1_)
	{
		return 32;
	}

	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.drink;
	}

	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
	{
		p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_));
		return p_77659_1_;
	}
}