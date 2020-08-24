package org.ultramine.core.economy.exception;

import net.minecraft.command.CommandException;

public class EconomyException extends CommandException
{
	public EconomyException(String translationKey, Object... args)
	{
		super(translationKey, args);
	}

	public EconomyException(Throwable t)
	{
		this("Unexpected exception occurred during economy operation", t);
	}

	public EconomyException(String message, Throwable t)
	{
		this(message);
		initCause(t);
	}
}
