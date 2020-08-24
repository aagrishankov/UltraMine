package net.minecraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

public class ItemColored extends ItemBlock
{
	private final Block field_150944_b;
	private String[] field_150945_c;
	private static final String __OBFID = "CL_00000003";

	public ItemColored(Block p_i45332_1_, boolean p_i45332_2_)
	{
		super(p_i45332_1_);
		this.field_150944_b = p_i45332_1_;

		if (p_i45332_2_)
		{
			this.setMaxDamage(0);
			this.setHasSubtypes(true);
		}
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack p_82790_1_, int p_82790_2_)
	{
		return this.field_150944_b.getRenderColor(p_82790_1_.getItemDamage());
	}

	public int getMetadata(int p_77647_1_)
	{
		return p_77647_1_;
	}

	public ItemColored func_150943_a(String[] p_150943_1_)
	{
		this.field_150945_c = p_150943_1_;
		return this;
	}

	public String getUnlocalizedName(ItemStack p_77667_1_)
	{
		if (this.field_150945_c == null)
		{
			return super.getUnlocalizedName(p_77667_1_);
		}
		else
		{
			int i = p_77667_1_.getItemDamage();
			return i >= 0 && i < this.field_150945_c.length ? super.getUnlocalizedName(p_77667_1_) + "." + this.field_150945_c[i] : super.getUnlocalizedName(p_77667_1_);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int p_77617_1_)
	{
		return this.field_150944_b.getIcon(0, p_77617_1_);
	}
}