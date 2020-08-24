package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovementInput
{
	public float moveStrafe;
	public float moveForward;
	public boolean jump;
	public boolean sneak;
	private static final String __OBFID = "CL_00000936";

	public void updatePlayerMoveState() {}
}