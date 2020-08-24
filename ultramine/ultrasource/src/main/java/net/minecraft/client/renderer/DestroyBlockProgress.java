package net.minecraft.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DestroyBlockProgress
{
	private final int miningPlayerEntId;
	private final int partialBlockX;
	private final int partialBlockY;
	private final int partialBlockZ;
	private int partialBlockProgress;
	private int createdAtCloudUpdateTick;
	private static final String __OBFID = "CL_00001427";

	public DestroyBlockProgress(int p_i1511_1_, int p_i1511_2_, int p_i1511_3_, int p_i1511_4_)
	{
		this.miningPlayerEntId = p_i1511_1_;
		this.partialBlockX = p_i1511_2_;
		this.partialBlockY = p_i1511_3_;
		this.partialBlockZ = p_i1511_4_;
	}

	public int getPartialBlockX()
	{
		return this.partialBlockX;
	}

	public int getPartialBlockY()
	{
		return this.partialBlockY;
	}

	public int getPartialBlockZ()
	{
		return this.partialBlockZ;
	}

	public void setPartialBlockDamage(int p_73107_1_)
	{
		if (p_73107_1_ > 10)
		{
			p_73107_1_ = 10;
		}

		this.partialBlockProgress = p_73107_1_;
	}

	public int getPartialBlockDamage()
	{
		return this.partialBlockProgress;
	}

	public void setCloudUpdateTick(int p_82744_1_)
	{
		this.createdAtCloudUpdateTick = p_82744_1_;
	}

	public int getCreationCloudUpdateTick()
	{
		return this.createdAtCloudUpdateTick;
	}
}