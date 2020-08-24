package net.minecraft.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureOffset
{
	public final int textureOffsetX;
	public final int textureOffsetY;
	private static final String __OBFID = "CL_00000875";

	public TextureOffset(int p_i1175_1_, int p_i1175_2_)
	{
		this.textureOffsetX = p_i1175_1_;
		this.textureOffsetY = p_i1175_2_;
	}
}