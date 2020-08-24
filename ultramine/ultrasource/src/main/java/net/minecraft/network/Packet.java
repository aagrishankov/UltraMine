package net.minecraft.network;

import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Packet
{
	private static final Logger logger = LogManager.getLogger();
	private static final String __OBFID = "CL_00001272";

	public static Packet generatePacket(BiMap p_148839_0_, int p_148839_1_)
	{
		try
		{
			Class oclass = (Class)p_148839_0_.get(Integer.valueOf(p_148839_1_));
			return oclass == null ? null : (Packet)oclass.newInstance();
		}
		catch (Exception exception)
		{
			logger.error("Couldn\'t create packet " + p_148839_1_, exception);
			return null;
		}
	}

	public static void writeBlob(ByteBuf p_148838_0_, byte[] p_148838_1_)
	{
		p_148838_0_.writeShort(p_148838_1_.length);
		p_148838_0_.writeBytes(p_148838_1_);
	}

	public static byte[] readBlob(ByteBuf p_148834_0_) throws IOException
	{
		short short1 = p_148834_0_.readShort();

		if (short1 < 0)
		{
			throw new IOException("Key was smaller than nothing!  Weird key!");
		}
		else
		{
			byte[] abyte = new byte[short1];
			p_148834_0_.readBytes(abyte);
			return abyte;
		}
	}

	public abstract void readPacketData(PacketBuffer p_148837_1_) throws IOException;

	public abstract void writePacketData(PacketBuffer p_148840_1_) throws IOException;

	public abstract void processPacket(INetHandler p_148833_1_);

	public boolean hasPriority()
	{
		return false;
	}

	public String toString()
	{
		return this.getClass().getSimpleName();
	}

	public String serialize()
	{
		return "";
	}
}