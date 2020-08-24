package org.ultramine.scheduler.pattern;

class AlwaysTrueValueMatcher implements IValueMatcher
{
	@Override
	public boolean match(int value)
	{
		return true;
	}
}
