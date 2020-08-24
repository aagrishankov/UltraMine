package org.ultramine.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.ArrayUtils;
import org.ultramine.commands.syntax.ArgumentsPattern;
import org.ultramine.core.service.InjectService;
import org.ultramine.core.permissions.Permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HandlerBasedCommand implements IExtendedCommand
{
	@InjectService private static Permissions perms;
	private String name;
	private String usage;
	private String group;
	private String description;

	private ICommandHandler handler;
	private List<ArgumentsPattern> argumentsPatterns;
	private Map<String, ICommandHandler> actionHandlers;

	private List<String> aliases;
	private String[] permissions;
	private boolean isUsableFromServer = true;

	public HandlerBasedCommand(String name, String group, ICommandHandler handler)
	{
		this.name = name;
		this.group = group;
		this.handler = handler;
		this.usage = "command." + name + ".usage";
		this.description = "command." + name + ".description";
		this.argumentsPatterns = new ArrayList<ArgumentsPattern>();
		this.actionHandlers = new HashMap<String, ICommandHandler>();
	}

	@Override
	public String getCommandName()
	{
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return usage;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getGroup()
	{
		return group;
	}

	@Override
	public List getCommandAliases()
	{
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2)
	{
		CommandContext.Builder builder = new CommandContext.Builder(this, var1, var2);

		if (argumentsPatterns.size() > 0)
		{
			ArgumentsPattern pattern = findArgumentsPattern(var2);
			if (pattern == null)
				throw new WrongUsageException(usage);

			builder.resolveArguments(pattern.getArgumentsNames());

			String actionName = pattern.resolveActionName(var2);
			if (!actionName.isEmpty())
				builder.setAction(actionName, actionHandlers.get(actionName));
		}

		handler.processCommand(builder.build());
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return (isUsableFromServer && !(var1 instanceof EntityPlayer)) || perms.hasAny(var1, permissions);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		if (argumentsPatterns.size() == 0)
			return null;

		List<String> result = null;
		Set<String> dupChecker = null;
		String[] tail = ArrayUtils.remove(var2, var2.length - 1);
		int minArgsCount = 1;

		for (ArgumentsPattern argumentsPattern : argumentsPatterns)
		{
			if (argumentsPattern.getArgumentsCount() < minArgsCount)
				continue;

			ArgumentsPattern.MatchResult currentMatch = argumentsPattern.partialMatch(tail);

			if (currentMatch != ArgumentsPattern.MatchResult.NOT)
			{
				List<String> options = argumentsPattern.getCompletionOptions(var2);
				if (options == null || options.size() == 0)
					continue;

				if (result == null)
				{
					result = options;
				}
				else
				{
					if (dupChecker == null)
					{
						dupChecker = new HashSet<String>(result);
					}

					for (String option: options)
					{
						if (!dupChecker.contains(option))
						{
							result.add(option);
							dupChecker.add(option);
						}
					}
				}

				if (currentMatch == ArgumentsPattern.MatchResult.FULLY)
					minArgsCount = argumentsPattern.getArgumentsCount() + 1;
			}
		}
		return result;
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		ArgumentsPattern argumentsPattern = findArgumentsPattern(var1);
		return argumentsPattern != null && argumentsPattern.isUsernameIndex(var2);
	}

	private ArgumentsPattern findArgumentsPattern(String[] args)
	{
		if (argumentsPatterns.size() == 0)
			return null;

		for (ArgumentsPattern argumentsPattern : argumentsPatterns)
		{
			if (argumentsPattern.match(args))
				return argumentsPattern;
		}
		return null;
	}

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof IExtendedCommand)
		{
			int result = getGroup().compareTo(((IExtendedCommand) o).getGroup());
			if (result == 0)
				result = getCommandName().compareTo(((IExtendedCommand) o).getCommandName());

			return result;
		}
		return -1;
	}

	public static class Builder
	{
		private HandlerBasedCommand command;

		public Builder(String name, String group, ICommandHandler handler)
		{
			command = new HandlerBasedCommand(name, group, handler);
		}

		public String getName()
		{
			return command.name;
		}

		public Builder setAliases(String... aliases)
		{
			command.aliases = Arrays.asList(aliases);
			return this;
		}

		public Builder addArgumentsPattern(ArgumentsPattern argumentsPattern)
		{
			command.argumentsPatterns.add(argumentsPattern);
			return this;
		}

		public Builder setPermissions(String... permissions)
		{
			command.permissions = permissions;
			return this;
		}

		public Builder setUsableFromServer(boolean isUsableFromServer)
		{
			command.isUsableFromServer = isUsableFromServer;
			return this;
		}

		public Builder addAction(String name, ICommandHandler action)
		{
			command.actionHandlers.put(name, action);
			return this;
		}

		public HandlerBasedCommand build()
		{
			return command;
		}
	}
}
