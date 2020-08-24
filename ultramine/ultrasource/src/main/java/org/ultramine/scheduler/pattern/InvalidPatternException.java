package org.ultramine.scheduler.pattern;

/**
 * <p>
 * This kind of exception is thrown if an invalid scheduling pattern is
 * encountered by the scheduler.
 * </p>
 */
public class InvalidPatternException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	InvalidPatternException()
	{
	}

	InvalidPatternException(String message)
	{
		super(message);
	}
}
