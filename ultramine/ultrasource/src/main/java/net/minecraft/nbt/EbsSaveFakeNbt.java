package net.minecraft.nbt;

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCounted;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.ultramine.server.chunk.alloc.MemSlot;
import org.ultramine.server.internal.LambdaHolder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

// Localed in net.minecraft.nbt package due to access issues
public class EbsSaveFakeNbt extends NBTTagCompound implements ReferenceCounted
{
	private static final byte[] EMPTY_2048_ARR = new byte[2048];
	private static final ThreadLocal<byte[]> LOCAL_BUFFER = ThreadLocal.withInitial(LambdaHolder.newByteArray(4096));
	private static final AtomicIntegerFieldUpdater<EbsSaveFakeNbt> REF_CNT_UPDATER = AtomicIntegerFieldUpdater.newUpdater(EbsSaveFakeNbt.class, "refCnt");
	private final ExtendedBlockStorage ebs;
	private final boolean hasNoSky;

	private volatile boolean isNbt;
	private volatile int refCnt = 1;

	public EbsSaveFakeNbt(ExtendedBlockStorage ebs, boolean hasNoSky)
	{
		super(Collections.emptyMap());
		this.ebs = ebs;
		this.hasNoSky = hasNoSky;
	}

	public ExtendedBlockStorage getEbs()
	{
		return ebs;
	}

	public void convertToNbt()
	{
		if(isNbt)
			return;
		createMap(0);
		setByte("Y", (byte)(ebs.getYLocation() >> 4 & 255));
		MemSlot slot = ebs.getSlot();
		setByteArray("Blocks", slot.copyLSB());
		setByteArray("Add", slot.copyMSB());
		setByteArray("Data", slot.copyBlockMetadata());
		setByteArray("BlockLight", slot.copyBlocklight());
		setByteArray("SkyLight", hasNoSky ? new byte[2048] : slot.copySkylight());

		isNbt = true;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		if(isNbt)
		{
			super.write(out);
			return;
		}
		writeNbt(out, "Y", new NBTTagByte((byte)(ebs.getYLocation() >> 4 & 255)));
		MemSlot slot = ebs.getSlot();
		byte[] buf = LOCAL_BUFFER.get();
		slot.copyLSB(buf, 0);
		writeByteArray(out, "Blocks", buf, 0, 4096);
		slot.copyMSB(buf, 0);
		writeByteArray(out, "Add", buf, 0, 2048);
		slot.copyBlockMetadata(buf, 0);
		writeByteArray(out, "Data", buf, 0, 2048);
		slot.copyBlocklight(buf, 0);
		writeByteArray(out, "BlockLight", buf, 0, 2048);
		if(hasNoSky)
		{
			writeByteArray(out, "SkyLight", EMPTY_2048_ARR, 0, 2048);
		}
		else
		{
			slot.copySkylight(buf, 0);
			writeByteArray(out, "SkyLight", buf, 0, 2048);
		}
		out.writeByte(0);
	}

	private static void writeNbt(DataOutput out, String key, NBTBase nbt) throws IOException
	{
		out.writeByte(nbt.getId());

		if(nbt.getId() != 0)
		{
			out.writeUTF(key);
			nbt.write(out);
		}
	}

	private static void writeByteArray(DataOutput out, String key, byte[] byteArray, int off, int len) throws IOException
	{
		out.writeByte((byte)7);
		out.writeUTF(key);
		out.writeInt(len - off);
		out.write(byteArray, off, len);
	}

	@Override
	public void func_152446_a(DataInput p_152446_1_, int p_152446_2_, NBTSizeTracker p_152446_3_) throws IOException
	{
		if(!isNbt)
			throw new UnsupportedOperationException();
		super.func_152446_a(p_152446_1_, p_152446_2_, p_152446_3_);
	}

	@Override
	public String toString()
	{
		if(!isNbt)
			return "";
		return super.toString();
	}

	@Override
	public NBTBase copy()
	{
		if(!isNbt)
			return new EbsSaveFakeNbt(ebs.copy(), hasNoSky);
		return super.copy();
	}

	@Override
	public boolean equals(Object o)
	{
		if(!isNbt)
			throw new UnsupportedOperationException();
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		if(!isNbt)
			throw new UnsupportedOperationException();
		return super.hashCode();
	}

	// Copied from io.netty.buffer.AbstractReferenceCountedByteBuf

	@Override
	public final int refCnt() {
		return REF_CNT_UPDATER.get(this);
	}

	@Override
	public EbsSaveFakeNbt retain() {
		for (;;) {
			int refCnt = this.refCnt;
			if (refCnt == 0) {
				throw new IllegalReferenceCountException(0, 1);
			}
			if (refCnt == Integer.MAX_VALUE) {
				throw new IllegalReferenceCountException(Integer.MAX_VALUE, 1);
			}
			if (REF_CNT_UPDATER.compareAndSet(this, refCnt, refCnt + 1)) {
				break;
			}
		}
		return this;
	}

	@Override
	public EbsSaveFakeNbt retain(int increment) {
		if (increment <= 0) {
			throw new IllegalArgumentException("increment: " + increment + " (expected: > 0)");
		}

		for (;;) {
			int refCnt = this.refCnt;
			if (refCnt == 0) {
				throw new IllegalReferenceCountException(0, increment);
			}
			if (refCnt > Integer.MAX_VALUE - increment) {
				throw new IllegalReferenceCountException(refCnt, increment);
			}
			if (REF_CNT_UPDATER.compareAndSet(this, refCnt, refCnt + increment)) {
				break;
			}
		}
		return this;
	}

	@Override
	public final boolean release() {
		for (;;) {
			int refCnt = this.refCnt;
			if (refCnt == 0) {
				throw new IllegalReferenceCountException(0, -1);
			}

			if (REF_CNT_UPDATER.compareAndSet(this, refCnt, refCnt - 1)) {
				if (refCnt == 1) {
					deallocate();
					return true;
				}
				return false;
			}
		}
	}

	@Override
	public final boolean release(int decrement) {
		if (decrement <= 0) {
			throw new IllegalArgumentException("decrement: " + decrement + " (expected: > 0)");
		}

		for (;;) {
			int refCnt = this.refCnt;
			if (refCnt < decrement) {
				throw new IllegalReferenceCountException(refCnt, -decrement);
			}

			if (REF_CNT_UPDATER.compareAndSet(this, refCnt, refCnt - decrement)) {
				if (refCnt == decrement) {
					deallocate();
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * Called once {@link #refCnt()} is equals 0.
	 */
	private void deallocate()
	{
		ebs.release();
	}
}
