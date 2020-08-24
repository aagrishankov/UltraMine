package net.minecraft.stats;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IStatType
{
	@SideOnly(Side.CLIENT)
	String format(int p_75843_1_);
}