package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IProgressUpdate
{
	void displayProgressMessage(String p_73720_1_);

	@SideOnly(Side.CLIENT)
	void resetProgressAndMessage(String p_73721_1_);

	void resetProgresAndWorkingMessage(String p_73719_1_);

	void setLoadingProgress(int p_73718_1_);

	@SideOnly(Side.CLIENT)
	void func_146586_a();
}