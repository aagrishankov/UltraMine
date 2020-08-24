package org.ultramine.server.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WeakObjectPool<T>
{
	private final List<WeakReference<T>> pool = new ArrayList<>();
	private final Supplier<T> factory;
	private final int limit;

	public WeakObjectPool(Supplier<T> factory, int limit)
	{
		this.factory = factory;
		this.limit = limit;
	}

	public WeakObjectPool(Supplier<T> factory)
	{
		this(factory, Integer.MAX_VALUE);
	}

	public T getOrCreateInstance()
	{
		for(int size; (size = pool.size()) != 0;)
		{
			T val = pool.remove(size - 1).get();
			if(val != null)
				return val;
		}

		return factory.get();
	}

	public void release(T val)
	{
		if(pool.size() < limit)
			pool.add(new WeakReference<>(val));
	}
}
