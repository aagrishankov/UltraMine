package org.ultramine.scheduler.pattern;

import gnu.trove.set.TIntSet;

class IntSetValueMatcher implements IValueMatcher
{
	private final TIntSet values;

	public IntSetValueMatcher(TIntSet values)
	{
		this.values = values;
	}

	@Override
	public boolean match(int value)
	{
		return values.contains(value);
	}
}
