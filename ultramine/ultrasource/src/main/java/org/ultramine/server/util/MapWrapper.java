package org.ultramine.server.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MapWrapper<K, V> implements Map<K, V>
{
	private final Map<K, V> wrapped;

	public MapWrapper(Map<K, V> wrapped)
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
	public boolean containsKey(Object key)
	{
		return wrapped.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return wrapped.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return wrapped.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return wrapped.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return wrapped.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		wrapped.putAll(m);
	}

	@Override
	public void clear()
	{
		wrapped.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return wrapped.keySet();
	}

	@Override
	public Collection<V> values()
	{
		return wrapped.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return wrapped.entrySet();
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
	public V getOrDefault(Object key, V defaultValue)
	{
		return wrapped.getOrDefault(key, defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		wrapped.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
	{
		wrapped.replaceAll(function);
	}

	@Override
	public V putIfAbsent(K key, V value)
	{
		return wrapped.putIfAbsent(key, value);
	}

	@Override
	public boolean remove(Object key, Object value)
	{
		return wrapped.remove(key, value);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		return wrapped.replace(key, oldValue, newValue);
	}

	@Override
	public V replace(K key, V value)
	{
		return wrapped.replace(key, value);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		return wrapped.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
	{
		return wrapped.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
	{
		return wrapped.compute(key, remappingFunction);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
	{
		return wrapped.merge(key, value, remappingFunction);
	}
}
