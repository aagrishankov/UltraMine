package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlayerInfo
{
	public final String name;
	private final String nameinLowerCase;
	public int responseTime;
	private static final String __OBFID = "CL_00000888";

	public GuiPlayerInfo(String p_i1190_1_)
	{
		this.name = p_i1190_1_;
		this.nameinLowerCase = p_i1190_1_.toLowerCase();
	}
}