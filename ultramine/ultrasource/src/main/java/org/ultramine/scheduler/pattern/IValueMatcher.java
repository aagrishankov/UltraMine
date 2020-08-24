package org.ultramine.scheduler.pattern;

/**
 * <p>
 * This interface describes the ValueMatcher behavior. A ValueMatcher is an
 * object that validate an integer value against a set of rules.
 * </p>
 */
interface IValueMatcher
{
	/**
	 * Validate the given integer value against a set of rules.
	 * 
	 * @param value
	 *            The value.
	 * @return true if the given value matches the rules of the ValueMatcher,
	 *         false otherwise.
	 */
	boolean match(int value);
}
