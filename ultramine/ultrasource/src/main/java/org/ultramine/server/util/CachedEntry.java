package org.ultramine.server.util;

public class CachedEntry<T> implements Comparable<CachedEntry<T>>
{
	private final T value;
	private long time;

	private CachedEntry(T value, long time)
	{
		this.value = value;
		this.time = time;
	}

	private CachedEntry(T value)
	{
		this(value, System.nanoTime());
	}

	public static <T> CachedEntry<T> of(T val)
	{
		return new CachedEntry<>(val);
	}

	public T getValueAndUpdateTime()
	{
		this.time = System.nanoTime();
		return value;
	}

	public long getTime()
	{
		return time;
	}

	@Override
	public int compareTo(CachedEntry<T> o)
	{
		return Long.compare(time, o.time);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		CachedEntry<?> that = (CachedEntry<?>) o;

		return value != null ? value.equals(that.value) : that.value == null;
	}

	@Override
	public int hashCode()
	{
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public String toString()
	{
		return "CachedEntry{" +
				"value=" + value +
				'}';
	}
}
