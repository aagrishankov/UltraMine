package org.ultramine.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBasedCommandHandler implements ICommandHandler
{
	private static final Logger logger = LogManager.getLogger();
	private Method method;

	public MethodBasedCommandHandler(Method method)
	{
		this.method = method;
	}

	@Override
	public void processCommand(CommandContext context)
	{
		try
		{
			method.invoke(null, context);
		}
		catch (IllegalAccessException e)
		{
			logger.error("Error while executing method for command " + context.getCommand().getCommandName(), e);
			throw new CommandNotFoundException();
		}
		catch (InvocationTargetException e)
		{
			if (e.getCause() == null)
			{
				logger.error("Error while executing method for command " + context.getCommand().getCommandName(), e);
				throw new CommandNotFoundException();
			}
			else if (e.getCause() instanceof CommandException)
				throw (CommandException) e.getCause();
			else
				throw new RuntimeException("Error while executing method for command " + context.getCommand().getCommandName(), e.getCause());
		}
	}
}
