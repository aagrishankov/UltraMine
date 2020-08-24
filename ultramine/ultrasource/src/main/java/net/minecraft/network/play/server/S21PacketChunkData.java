package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.ultramine.server.chunk.ChunkSnapshot;
import org.ultramine.server.internal.UMHooks;

public class S21PacketChunkData extends Packet
{
	private int field_149284_a;
	private int field_149282_b;
	private int field_149283_c;
	private int field_149280_d;
	private byte[] field_149281_e;
	private byte[] field_149278_f;
	private boolean field_149279_g;
	private int field_149285_h;
	private static byte[] field_149286_i = new byte[196864];
	private static final String __OBFID = "CL_00001304";
	private Semaphore deflateGate;
	
	private static final byte[] unloadSequence = new byte[] {0x78, (byte) 0x9C, 0x63, 0x64, 0x1C, (byte) 0xD9, 0x00, 0x00, (byte) 0x81, (byte) 0x80, 0x01, 0x01};
	private ChunkSnapshot chunkSnapshot;

	public S21PacketChunkData() {}

	public S21PacketChunkData(Chunk p_i45196_1_, boolean p_i45196_2_, int p_i45196_3_)
	{
		this.field_149284_a = p_i45196_1_.xPosition;
		this.field_149282_b = p_i45196_1_.zPosition;
		this.field_149279_g = p_i45196_2_;
		S21PacketChunkData.Extracted extracted = func_149269_a(p_i45196_1_, p_i45196_2_, p_i45196_3_);
		this.field_149280_d = extracted.field_150281_c;
		this.field_149283_c = extracted.field_150280_b;
		this.field_149278_f = extracted.field_150282_a;
		this.deflateGate = new Semaphore(1);
	}

	private S21PacketChunkData(ChunkSnapshot chunkSnapshot)
	{
		this.field_149284_a = chunkSnapshot.getX();
		this.field_149282_b = chunkSnapshot.getZ();
		this.field_149279_g = true;
		this.chunkSnapshot = chunkSnapshot;
	}
	
	private S21PacketChunkData(int cx, int cz) //for unload
	{
		this.field_149284_a = cx;
		this.field_149282_b = cz;
		this.field_149279_g = true;
		
		this.field_149285_h = unloadSequence.length;
		this.field_149281_e = unloadSequence;
	}

	public void deflate()
	{
		Deflater deflater = new Deflater(7);
		try
		{
			if(chunkSnapshot != null)
			{
				UMHooks.ChunkPacketData data = UMHooks.extractAndDeflateChunkPacketData(deflater, chunkSnapshot);
				field_149281_e = data.data;
				field_149285_h = data.length;

				this.field_149280_d = data.ebsMask;
				this.field_149283_c = data.ebsMask;
				this.field_149278_f = null;
				chunkSnapshot.release();
				chunkSnapshot = null;
				return;
			}
			deflater.setInput(this.field_149278_f, 0, this.field_149278_f.length);
			deflater.finish();
			byte[] deflated = new byte[4096];
			int dataLen = 0;
			while (!deflater.finished()) {
				if(dataLen == deflated.length)
					deflated = Arrays.copyOf(deflated, deflated.length * 2);
				dataLen += deflater.deflate(deflated, dataLen, deflated.length - dataLen);
			}
			this.field_149285_h = dataLen;
			this.field_149281_e = deflated;
		}
		finally
		{
			deflater.end();
		}
	}

	public static int func_149275_c()
	{
		return 196864;
	}

	public void readPacketData(PacketBuffer p_148837_1_) throws IOException
	{
		this.field_149284_a = p_148837_1_.readInt();
		this.field_149282_b = p_148837_1_.readInt();
		this.field_149279_g = p_148837_1_.readBoolean();
		this.field_149283_c = p_148837_1_.readShort();
		this.field_149280_d = p_148837_1_.readShort();
		this.field_149285_h = p_148837_1_.readInt();

		if (field_149286_i.length < this.field_149285_h)
		{
			field_149286_i = new byte[this.field_149285_h];
		}

		p_148837_1_.readBytes(field_149286_i, 0, this.field_149285_h);
		int i = 0;
		int j;
		int msb = 0; //BugFix: MC does not read the MSB array from the packet properly, causing issues for servers that use blocks > 256

		for (j = 0; j < 16; ++j)
		{
			i += this.field_149283_c >> j & 1;
			msb += this.field_149283_c >> j & 1;
		}

		j = 12288 * i;
		j += 2048 * msb;

		if (this.field_149279_g)
		{
			j += 256;
		}

		this.field_149278_f = new byte[j];
		Inflater inflater = new Inflater();
		inflater.setInput(field_149286_i, 0, this.field_149285_h);

		try
		{
			inflater.inflate(this.field_149278_f);
		}
		catch (DataFormatException dataformatexception)
		{
			throw new IOException("Bad compressed data format");
		}
		finally
		{
			inflater.end();
		}
	}

	public void writePacketData(PacketBuffer p_148840_1_) throws IOException
	{
		if (this.field_149281_e == null)
		{
			deflateGate.acquireUninterruptibly();
			if (this.field_149281_e == null)
			{
				deflate();
			}
			deflateGate.release();
		}
		p_148840_1_.writeInt(this.field_149284_a);
		p_148840_1_.writeInt(this.field_149282_b);
		p_148840_1_.writeBoolean(this.field_149279_g);
		p_148840_1_.writeShort((short)(this.field_149283_c & 65535));
		p_148840_1_.writeShort((short)(this.field_149280_d & 65535));
		p_148840_1_.writeInt(this.field_149285_h);
		p_148840_1_.writeBytes(this.field_149281_e, 0, this.field_149285_h);
	}

	public void processPacket(INetHandlerPlayClient p_148833_1_)
	{
		p_148833_1_.handleChunkData(this);
	}

	public String serialize()
	{
		return String.format("x=%d, z=%d, full=%b, sects=%d, add=%d, size=%d", new Object[] {Integer.valueOf(this.field_149284_a), Integer.valueOf(this.field_149282_b), Boolean.valueOf(this.field_149279_g), Integer.valueOf(this.field_149283_c), Integer.valueOf(this.field_149280_d), Integer.valueOf(this.field_149285_h)});
	}

	@SideOnly(Side.CLIENT)
	public byte[] func_149272_d()
	{
		return this.field_149278_f;
	}

	public static S21PacketChunkData.Extracted func_149269_a(Chunk p_149269_0_, boolean p_149269_1_, int p_149269_2_)
	{
		int j = 0;
		ExtendedBlockStorage[] aextendedblockstorage = p_149269_0_.getBlockStorageArray();
		int k = 0;
		S21PacketChunkData.Extracted extracted = new S21PacketChunkData.Extracted();
		byte[] abyte = field_149286_i;

		if (p_149269_1_)
		{
			p_149269_0_.sendUpdates = true;
		}

		if(aextendedblockstorage[0] == null)
			aextendedblockstorage[0] = new ExtendedBlockStorage(0, true);

		int l;

		for (l = 0; l < aextendedblockstorage.length; ++l)
		{
			if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty()) && (p_149269_2_ & 1 << l) != 0)
			{
				extracted.field_150280_b |= 1 << l;

				if (true/*aextendedblockstorage[l].getBlockMSBArray() != null*/)
				{
					extracted.field_150281_c |= 1 << l;
					++k;
				}
			}
		}

		for (l = 0; l < aextendedblockstorage.length; ++l)
		{
			if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty()) && (p_149269_2_ & 1 << l) != 0)
			{
				aextendedblockstorage[l].getSlot().copyLSB(abyte, j);
				j += 4096;
			}
		}

		NibbleArray nibblearray;

		for (l = 0; l < aextendedblockstorage.length; ++l)
		{
			if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty()) && (p_149269_2_ & 1 << l) != 0)
			{
				aextendedblockstorage[l].getSlot().copyBlockMetadata(abyte, j);
				j += 2048;
			}
		}

		for (l = 0; l < aextendedblockstorage.length; ++l)
		{
			if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty()) && (p_149269_2_ & 1 << l) != 0)
			{
				aextendedblockstorage[l].getSlot().copyBlocklight(abyte, j);
				j += 2048;
			}
		}

		if (!p_149269_0_.worldObj.provider.hasNoSky)
		{
			for (l = 0; l < aextendedblockstorage.length; ++l)
			{
				if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty()) && (p_149269_2_ & 1 << l) != 0)
				{
					aextendedblockstorage[l].getSlot().copySkylight(abyte, j);
					j += 2048;
				}
			}
		}

		if (k > 0)
		{
			for (l = 0; l < aextendedblockstorage.length; ++l)
			{
				if (aextendedblockstorage[l] != null && (l == 0 || !p_149269_1_ || !aextendedblockstorage[l].isEmpty())/* && aextendedblockstorage[l].getBlockMSBArray() != null*/ && (p_149269_2_ & 1 << l) != 0)
				{
					aextendedblockstorage[l].getSlot().copyMSB(abyte, j);
					j += 2048;
				}
			}
		}

		if (p_149269_1_)
		{
			byte[] abyte2 = p_149269_0_.getBiomeArray();
			System.arraycopy(abyte2, 0, abyte, j, abyte2.length);
			j += abyte2.length;
		}

		extracted.field_150282_a = new byte[j];
		System.arraycopy(abyte, 0, extracted.field_150282_a, 0, j);
		return extracted;
	}

	public void processPacket(INetHandler p_148833_1_)
	{
		this.processPacket((INetHandlerPlayClient)p_148833_1_);
	}

	@SideOnly(Side.CLIENT)
	public int func_149273_e()
	{
		return this.field_149284_a;
	}

	@SideOnly(Side.CLIENT)
	public int func_149271_f()
	{
		return this.field_149282_b;
	}

	@SideOnly(Side.CLIENT)
	public int func_149276_g()
	{
		return this.field_149283_c;
	}

	@SideOnly(Side.CLIENT)
	public int func_149270_h()
	{
		return this.field_149280_d;
	}

	@SideOnly(Side.CLIENT)
	public boolean func_149274_i()
	{
		return this.field_149279_g;
	}

	public static class Extracted
		{
			public byte[] field_150282_a;
			public int field_150280_b;
			public int field_150281_c;
			private static final String __OBFID = "CL_00001305";
		}
	
	public static S21PacketChunkData makeForSend(Chunk chunk)
	{
		return new S21PacketChunkData(chunk, true, 65535);
	}

	public static S21PacketChunkData makeForSend(ChunkSnapshot chunkSnapshot)
	{
		return new S21PacketChunkData(chunkSnapshot);
	}
	
	public static S21PacketChunkData makeForUnload(Chunk chunk)
	{
		return new S21PacketChunkData(chunk.xPosition, chunk.zPosition);
	}
	
	public static S21PacketChunkData makeForUnload(int cx, int cz)
	{
		return new S21PacketChunkData(cx, cz);
	}
}
