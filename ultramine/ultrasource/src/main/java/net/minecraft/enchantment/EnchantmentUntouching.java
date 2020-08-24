package net.minecraft.enchantment;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class EnchantmentUntouching extends Enchantment
{
	private static final String __OBFID = "CL_00000123";

	protected EnchantmentUntouching(int p_i1938_1_, int p_i1938_2_)
	{
		super(p_i1938_1_, p_i1938_2_, EnumEnchantmentType.digger);
		this.setName("untouching");
	}

	public int getMinEnchantability(int p_77321_1_)
	{
		return 15;
	}

	public int getMaxEnchantability(int p_77317_1_)
	{
		return super.getMinEnchantability(p_77317_1_) + 50;
	}

	public int getMaxLevel()
	{
		return 1;
	}

	public boolean canApplyTogether(Enchantment p_77326_1_)
	{
		return super.canApplyTogether(p_77326_1_) && p_77326_1_.effectId != fortune.effectId;
	}

	public boolean canApply(ItemStack p_92089_1_)
	{
		return p_92089_1_.getItem() == Items.shears ? true : super.canApply(p_92089_1_);
	}
}