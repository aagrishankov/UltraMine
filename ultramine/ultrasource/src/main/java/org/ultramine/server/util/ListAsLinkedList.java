package org.ultramine.server.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ListAsLinkedList<E> extends LinkedList<E> implements RandomAccess
{
	private final List<E> wrapped;

	public ListAsLinkedList(List<E> wrapped)
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
	public Iterator<E> iterator()
	{
		return wrapped.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return wrapped.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return wrapped.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return wrapped.add(e);
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
	public boolean addAll(Collection<? extends E> c)
	{
		return wrapped.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		return wrapped.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return wrapped.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return wrapped.retainAll(c);
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator)
	{
		wrapped.replaceAll(operator);
	}

	@Override
	public void sort(Comparator<? super E> c)
	{
		wrapped.sort(c);
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
	public E get(int index)
	{
		return wrapped.get(index);
	}

	@Override
	public E set(int index, E element)
	{
		return wrapped.set(index, element);
	}

	@Override
	public void add(int index, E element)
	{
		wrapped.add(index, element);
	}

	@Override
	public E remove(int index)
	{
		return wrapped.remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return wrapped.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return wrapped.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return wrapped.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return wrapped.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return wrapped.subList(fromIndex, toIndex);
	}

	@Override
	public Spliterator<E> spliterator()
	{
		return wrapped.spliterator();
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter)
	{
		return wrapped.removeIf(filter);
	}

	@Override
	public Stream<E> stream()
	{
		return wrapped.stream();
	}

	@Override
	public Stream<E> parallelStream()
	{
		return wrapped.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super E> action)
	{
		wrapped.forEach(action);
	}

	@Override
	public E getFirst()
	{
		if(isEmpty())
			throw new NoSuchElementException();
		return get(0);
	}

	@Override
	public E getLast()
	{
		if(isEmpty())
			throw new NoSuchElementException();
		return get(size() - 1);
	}

	@Override
	public E removeFirst()
	{
		if(isEmpty())
			throw new NoSuchElementException();
		return remove(0);
	}

	@Override
	public E removeLast()
	{
		if(isEmpty())
			throw new NoSuchElementException();
		return remove(size() - 1);
	}

	@Override
	public void addFirst(E e)
	{
		add(0, e);
	}

	@Override
	public void addLast(E e)
	{
		add(e);
	}

	@Override
	public boolean removeFirstOccurrence(Object o)
	{
		return remove(o);
	}

	@Override
	public boolean removeLastOccurrence(Object o)
	{
		int ind = lastIndexOf(o);
		if(ind != -1)
		{
			remove(ind);
			return true;
		}
		return false;
	}
}
