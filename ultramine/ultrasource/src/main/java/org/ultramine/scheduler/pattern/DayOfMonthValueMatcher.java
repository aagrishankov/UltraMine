package org.ultramine.scheduler.pattern;

import gnu.trove.set.TIntSet;

/**
 * <p>
 * A ValueMatcher whose rules are in a set of integer values. When asked
 * to validate a value, this ValueMatcher checks if it is in the set and, if
 * not, checks whether the last-day-of-month setting applies.
 * </p>
 */
class DayOfMonthValueMatcher extends IntSetValueMatcher
{
	private static final int[] lastDays = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	public DayOfMonthValueMatcher(TIntSet integers)
	{
		super(integers);
	}

	/**
	 * Returns true if the given value is included in the matcher list or the
	 * last-day-of-month setting applies.
	 */
	public boolean match(int value, int month, boolean isLeapYear)
	{
		return (super.match(value) || (value > 27 && match(32) && isLastDayOfMonth(value, month, isLeapYear)));
	}

	public boolean isLastDayOfMonth(int value, int month, boolean isLeapYear)
	{
		if(isLeapYear && month == 2)
		{
			return value == 29;
		}
		else
		{
			return value == lastDays[month - 1];
		}
	}
}
