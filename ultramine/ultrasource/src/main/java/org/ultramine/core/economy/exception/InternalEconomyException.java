package org.ultramine.core.economy.exception;

public class InternalEconomyException extends EconomyException
{
	public InternalEconomyException(String translationKey, Object... args)
	{
		super(translationKey, args);
	}

	public InternalEconomyException(Throwable t)
	{
		super(t);
	}

	public InternalEconomyException(String message, Throwable t)
	{
		super(message, t);
	}
}
