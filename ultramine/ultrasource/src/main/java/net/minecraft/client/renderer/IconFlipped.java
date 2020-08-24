package net.minecraft.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

@SideOnly(Side.CLIENT)
public class IconFlipped implements IIcon
{
	private final IIcon baseIcon;
	private final boolean flipU;
	private final boolean flipV;
	private static final String __OBFID = "CL_00001511";

	public IconFlipped(IIcon p_i1560_1_, boolean p_i1560_2_, boolean p_i1560_3_)
	{
		this.baseIcon = p_i1560_1_;
		this.flipU = p_i1560_2_;
		this.flipV = p_i1560_3_;
	}

	public int getIconWidth()
	{
		return this.baseIcon.getIconWidth();
	}

	public int getIconHeight()
	{
		return this.baseIcon.getIconHeight();
	}

	public float getMinU()
	{
		return this.flipU ? this.baseIcon.getMaxU() : this.baseIcon.getMinU();
	}

	public float getMaxU()
	{
		return this.flipU ? this.baseIcon.getMinU() : this.baseIcon.getMaxU();
	}

	public float getInterpolatedU(double p_94214_1_)
	{
		float f = this.getMaxU() - this.getMinU();
		return this.getMinU() + f * ((float)p_94214_1_ / 16.0F);
	}

	public float getMinV()
	{
		return this.flipV ? this.baseIcon.getMinV() : this.baseIcon.getMinV();
	}

	public float getMaxV()
	{
		return this.flipV ? this.baseIcon.getMinV() : this.baseIcon.getMaxV();
	}

	public float getInterpolatedV(double p_94207_1_)
	{
		float f = this.getMaxV() - this.getMinV();
		return this.getMinV() + f * ((float)p_94207_1_ / 16.0F);
	}

	public String getIconName()
	{
		return this.baseIcon.getIconName();
	}
}