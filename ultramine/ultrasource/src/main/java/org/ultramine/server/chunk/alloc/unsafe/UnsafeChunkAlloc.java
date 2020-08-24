package org.ultramine.server.chunk.alloc.unsafe;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import org.ultramine.server.chunk.alloc.ChunkAllocService;
import org.ultramine.server.chunk.alloc.MemSlot;
import org.ultramine.server.util.UnsafeUtil;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.LongFunction;

import static org.ultramine.server.chunk.alloc.unsafe.AbstractUnsafeMemSlot.SLOT_SIZE;

@ThreadSafe
public class UnsafeChunkAlloc implements ChunkAllocService
{
	private static final Unsafe U = UnsafeUtil.getUnsafe();
	private static final int SLOT_LIMIT = Integer.parseInt(System.getProperty("org.ultramine.chunk.alloc.offheap.memlimit", "6")) * (1024 * 1024 * 1024 / SLOT_SIZE);
	// We delaying freeing not only for caching, but to be sure that nobody access this memory from other threads through data races
	private static final int SLOT_FREE_DELAY = 5000;
	private static final boolean USE_8_LAYOUT = System.getProperty("org.ultramine.chunk.alloc.layout", "7").equals("8"); // false by default

	private final LongFunction<MemSlot> slotFactory = USE_8_LAYOUT ? pointer -> new Unsafe8MemSlot(this, pointer) : pointer -> new Unsafe7MemSlot(this, pointer);
	private final Deque<ReleasedSlot> releasedSlots = new ArrayDeque<>();
	private int slots;

	public UnsafeChunkAlloc()
	{
		new Timer("OffHeapChunkAlloc cleaner", true).schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				releaseAvailableSlots();
			}
		}, 2000, 2000);
	}

	@Nonnull
	@Override
	public synchronized MemSlot allocateSlot()
	{
		ReleasedSlot released = releasedSlots.poll();
		if(released != null)
			return slotFactory.apply(released.pointer);
		slots++;
		if(slots >= SLOT_LIMIT)
			throw new OutOfMemoryError("Off-heap chunk storage");
		return slotFactory.apply(U.allocateMemory(SLOT_SIZE));
	}

	synchronized void releaseSlot(long pointer)
	{
		releasedSlots.add(new ReleasedSlot(pointer));
	}

	@Override
	public long getOffHeapTotalMemory()
	{
		return (long)slots * SLOT_SIZE;
	}

	@Override
	public long getOffHeapUsedMemory()
	{
		return (long)(slots - releasedSlots.size()) * SLOT_SIZE;
	}

	private void releaseAvailableSlots()
	{
		TLongList toRelease = new TLongArrayList();
		synchronized(this)
		{
			long time = System.currentTimeMillis();
			while(true)
			{
				ReleasedSlot slot = releasedSlots.peek();
				if(slot == null || time - slot.time < SLOT_FREE_DELAY)
					break;
				releasedSlots.poll();
				toRelease.add(slot.pointer);
			}
			slots -= toRelease.size();
		}

		for(TLongIterator it = toRelease.iterator(); it.hasNext();)
			U.freeMemory(it.next());
	}

	private static class ReleasedSlot
	{
		final long pointer;
		final long time;

		public ReleasedSlot(long pointer)
		{
			this.pointer = pointer;
			time = System.currentTimeMillis();
		}
	}
}
