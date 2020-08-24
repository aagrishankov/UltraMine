package net.minecraft.nbt;

import java.io.DataOutputStream;
import java.io.IOException;

// Localed in net.minecraft.nbt package due to access issues
public class NBTOutputStream implements AutoCloseable
{
	private final DataOutputStream out;

	public NBTOutputStream(DataOutputStream out) throws IOException
	{
		this.out = out;
		out.writeByte((byte)10);
		out.writeUTF("");
	}

	public void startCompoundTag(String name) throws IOException
	{
		out.writeByte((byte)10);
		out.writeUTF(name);
	}

	public void endCompoundTag() throws IOException
	{
		out.writeByte((byte)0);
	}

	public void startTagList(String name, int tagType, int count) throws IOException
	{
		out.writeByte((byte)9);
		out.writeUTF(name);
		out.writeByte((byte)tagType);
		out.writeInt(count);
	}

	public void entTagList() throws IOException
	{

	}

	public void writeListItemTag(NBTBase tag) throws IOException
	{
		tag.write(out);
	}

	public void writeTag(String name, NBTBase tag) throws IOException
	{
		out.writeByte(tag.getId());

		if(tag.getId() != 0)
		{
			out.writeUTF(name);
			tag.write(out);
		}
	}

	public void writeInt(String name, int value) throws IOException
	{
		out.writeByte((byte)3);
		out.writeUTF(name);
		out.writeInt(value);
	}

	public void writeString(String name, String value) throws IOException
	{
		out.writeByte((byte)8);
		out.writeUTF(name);
		out.writeUTF(value);
	}

	public void close() throws IOException
	{
		out.writeByte((byte)0);
		out.close();
	}
}
