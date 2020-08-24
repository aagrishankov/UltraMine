package net.minecraft.world.chunk;

public class NibbleArray
{
	public final byte[] data;
	private final int depthBits;
	private final int depthBitsPlusFour;
	private static final String __OBFID = "CL_00000371";

	public NibbleArray(int p_i1992_1_, int p_i1992_2_)
	{
		this.data = new byte[p_i1992_1_ >> 1];
		this.depthBits = p_i1992_2_;
		this.depthBitsPlusFour = p_i1992_2_ + 4;
	}

	public NibbleArray(byte[] p_i1993_1_, int p_i1993_2_)
	{
		this.data = p_i1993_1_;
		this.depthBits = p_i1993_2_;
		this.depthBitsPlusFour = p_i1993_2_ + 4;
	}

	public int get(int p_76582_1_, int p_76582_2_, int p_76582_3_)
	{
		int l = p_76582_2_ << this.depthBitsPlusFour | p_76582_3_ << this.depthBits | p_76582_1_;
		int i1 = l >> 1;
		int j1 = l & 1;
		return j1 == 0 ? this.data[i1] & 15 : this.data[i1] >> 4 & 15;
	}

	public void set(int p_76581_1_, int p_76581_2_, int p_76581_3_, int p_76581_4_)
	{
		int i1 = p_76581_2_ << this.depthBitsPlusFour | p_76581_3_ << this.depthBits | p_76581_1_;
		int j1 = i1 >> 1;
		int k1 = i1 & 1;

		if (k1 == 0)
		{
			this.data[j1] = (byte)(this.data[j1] & 240 | p_76581_4_ & 15);
		}
		else
		{
			this.data[j1] = (byte)(this.data[j1] & 15 | (p_76581_4_ & 15) << 4);
		}
	}
}