package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SideOnly(Side.SERVER)
public class RConOutputStream
{
	private ByteArrayOutputStream byteArrayOutput;
	private DataOutputStream output;
	private static final String __OBFID = "CL_00001798";

	public RConOutputStream(int p_i1533_1_)
	{
		this.byteArrayOutput = new ByteArrayOutputStream(p_i1533_1_);
		this.output = new DataOutputStream(this.byteArrayOutput);
	}

	public void writeByteArray(byte[] p_72670_1_) throws IOException
	{
		this.output.write(p_72670_1_, 0, p_72670_1_.length);
	}

	public void writeString(String p_72671_1_) throws IOException
	{
		this.output.writeBytes(p_72671_1_);
		this.output.write(0);
	}

	public void writeInt(int p_72667_1_) throws IOException
	{
		this.output.write(p_72667_1_);
	}

	public void writeShort(short p_72668_1_) throws IOException
	{
		this.output.writeShort(Short.reverseBytes(p_72668_1_));
	}

	public byte[] toByteArray()
	{
		return this.byteArrayOutput.toByteArray();
	}

	public void reset()
	{
		this.byteArrayOutput.reset();
	}
}