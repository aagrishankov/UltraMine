package net.minecraft.enchantment;

public class EnchantmentOxygen extends Enchantment
{
	private static final String __OBFID = "CL_00000120";

	public EnchantmentOxygen(int p_i1935_1_, int p_i1935_2_)
	{
		super(p_i1935_1_, p_i1935_2_, EnumEnchantmentType.armor_head);
		this.setName("oxygen");
	}

	public int getMinEnchantability(int p_77321_1_)
	{
		return 10 * p_77321_1_;
	}

	public int getMaxEnchantability(int p_77317_1_)
	{
		return this.getMinEnchantability(p_77317_1_) + 30;
	}

	public int getMaxLevel()
	{
		return 3;
	}
}