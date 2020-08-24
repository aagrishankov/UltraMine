package net.minecraft.enchantment;

public class EnchantmentFireAspect extends Enchantment
{
	private static final String __OBFID = "CL_00000116";

	protected EnchantmentFireAspect(int p_i1932_1_, int p_i1932_2_)
	{
		super(p_i1932_1_, p_i1932_2_, EnumEnchantmentType.weapon);
		this.setName("fire");
	}

	public int getMinEnchantability(int p_77321_1_)
	{
		return 10 + 20 * (p_77321_1_ - 1);
	}

	public int getMaxEnchantability(int p_77317_1_)
	{
		return super.getMinEnchantability(p_77317_1_) + 50;
	}

	public int getMaxLevel()
	{
		return 2;
	}
}