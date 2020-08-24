package net.minecraft.item;

public class ItemBook extends Item
{
	private static final String __OBFID = "CL_00001775";

	public boolean isItemTool(ItemStack p_77616_1_)
	{
		return p_77616_1_.stackSize == 1;
	}

	public int getItemEnchantability()
	{
		return 1;
	}
}