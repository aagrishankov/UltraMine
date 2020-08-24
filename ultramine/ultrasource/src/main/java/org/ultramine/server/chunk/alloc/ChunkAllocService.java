package org.ultramine.server.chunk.alloc;

import org.ultramine.core.service.Service;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Service(singleProvider = true)
public interface ChunkAllocService
{
	@Nonnull
	MemSlot allocateSlot();

	long getOffHeapTotalMemory();

	long getOffHeapUsedMemory();
}
