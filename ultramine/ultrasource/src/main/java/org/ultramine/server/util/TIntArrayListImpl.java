package org.ultramine.server.util;

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;

public class TIntArrayListImpl extends TIntArrayList
{
	public TIntArrayListImpl()
	{
		super();
	}

	public TIntArrayListImpl(int capacity, int no_entry_value)
	{
		super(capacity, no_entry_value);
	}

	public TIntArrayListImpl(int capacity)
	{
		super(capacity);
	}

	public TIntArrayListImpl(int[] values, int no_entry_value, boolean wrap)
	{
		super(values, no_entry_value, wrap);
	}

	public TIntArrayListImpl(int[] values)
	{
		super(values);
	}

	public TIntArrayListImpl(TIntCollection collection)
	{
		super(collection);
	}

	public void sort(IntComparator comp)
	{
		CollectionUtil.sort(_data, 0, _pos, comp);
	}

	public void backSort(IntComparator comp)
	{
		sort(new IntComparator()
		{
			@Override
			public int compare(int i1, int i2)
			{
				return comp.compare(i2, i1);
			}
		});
	}
}
