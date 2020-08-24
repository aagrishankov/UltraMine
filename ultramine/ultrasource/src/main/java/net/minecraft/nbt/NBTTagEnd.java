package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagEnd extends NBTBase
{
	private static final String __OBFID = "CL_00001219";

	void func_152446_a(DataInput p_152446_1_, int p_152446_2_, NBTSizeTracker p_152446_3_) throws IOException {}

	void write(DataOutput p_74734_1_) throws IOException {}

	public byte getId()
	{
		return (byte)0;
	}

	public String toString()
	{
		return "END";
	}

	public NBTBase copy()
	{
		return new NBTTagEnd();
	}
}