package org.ultramine.server.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SetWrapper<T> implements Set<T>
{
	private final Set<T> wrapped;

	public SetWrapper(Set<T> wrapped)
	{
		this.wrapped = wrapped;
	}

	@Override
	public int size()
	{
		return wrapped.size();
	}

	@Override
	public boolean isEmpty()
	{
		return wrapped.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return wrapped.contains(o);
	}

	@Override
	public Iterator<T> iterator()
	{
		return wrapped.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return wrapped.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a)
	{
		return wrapped.toArray(a);
	}

	@Override
	public boolean add(T t)
	{
		return wrapped.add(t);
	}

	@Override
	public boolean remove(Object o)
	{
		return wrapped.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return wrapped.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		return wrapped.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return wrapped.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return wrapped.removeAll(c);
	}

	@Override
	public void clear()
	{
		wrapped.clear();
	}

	@Override
	public boolean equals(Object o)
	{
		return wrapped.equals(o);
	}

	@Override
	public int hashCode()
	{
		return wrapped.hashCode();
	}

	@Override
	public Spliterator<T> spliterator()
	{
		return wrapped.spliterator();
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter)
	{
		return wrapped.removeIf(filter);
	}

	@Override
	public Stream<T> stream()
	{
		return wrapped.stream();
	}

	@Override
	public Stream<T> parallelStream()
	{
		return wrapped.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super T> action)
	{
		wrapped.forEach(action);
	}
}
