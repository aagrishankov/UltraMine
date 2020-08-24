package org.ultramine.commands.syntax;

import net.minecraft.command.CommandBase;

import java.util.ArrayList;
import java.util.List;

public class ArgumentsPattern
{
	private final List<IArgument> arguments;
	private final List<String> names;
	private final List<Integer> actionPositions;
	private boolean isInfinite;

	private ArgumentsPattern()
	{
		arguments = new ArrayList<IArgument>();
		names = new ArrayList<String>();
		actionPositions = new ArrayList<Integer>();
		isInfinite = false;
	}

	private IArgument getSafe(int num)
	{
		if (num >= arguments.size())
			return isInfinite ? arguments.get(arguments.size() - 1) : IGNORED;
		else
			return arguments.get(num);
	}

	public List<String> getCompletionOptions(String[] args)
	{
		if (arguments.size() == 0)
			return null;

		return getSafe(args.length - 1).getCompletionOptions(args);
	}

	public boolean isUsernameIndex(int checkArgNum)
	{
		return getSafe(checkArgNum).isUsername();
	}

	public boolean match(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (i == arguments.size())
				return isInfinite;

			IArgument argument = arguments.get(i);
			if (argument.hasValidation() && !argument.validate(args[i]))
				return false;
		}
		return args.length == arguments.size();
	}

	public MatchResult partialMatch(String[] args)
	{
		for (int i = 0; i < arguments.size(); i++)
		{
			IArgument argument = arguments.get(i);

			if (i < args.length)
			{
				if (argument.hasValidation() && !argument.validate(args[i]))
					return MatchResult.NOT;
			}
			else
			{
				if (argument.hasValidation())
					return MatchResult.POSSIBLY;
			}
		}
		if (isInfinite || args.length <= arguments.size())
			return MatchResult.FULLY;
		else
			return MatchResult.NOT;
	}

	public List<String> getArgumentsNames()
	{
		return names;
	}

	public String resolveActionName(String[] args)
	{
		if (actionPositions.size() == 0)
			return "";

		StringBuilder builder = new StringBuilder(args[actionPositions.get(0)]);
		for (int i = 1; i < actionPositions.size(); i++)
			builder.append(' ').append(args[actionPositions.get(i)]);

		return builder.toString();
	}

	public int getArgumentsCount()
	{
		return arguments.size();
	}

	public static class Builder
	{
		private ArgumentsPattern pattern;

		public Builder()
		{
			pattern = new ArgumentsPattern();
		}

		public void addArgument(String name, IArgument argument)
		{
			pattern.arguments.add(argument);
			pattern.names.add(name);
		}

		public void addAction(String[] options)
		{
			pattern.actionPositions.add(pattern.arguments.size());
			addArgument(null, new ActionArgument(options));
		}

		public void makeInfinite()
		{
			pattern.isInfinite = true;
		}

		public ArgumentsPattern build()
		{
			return pattern;
		}
	}

	private static class ActionArgument implements IArgument
	{
		private final String[] options;

		private ActionArgument(String[] options)
		{
			this.options = options;
		}

		@Override
		public boolean isUsername()
		{
			return false;
		}

		@Override
		public List<String> getCompletionOptions(String[] args)
		{
			List<String> result = new ArrayList<String>();
			String val = args[args.length - 1];
			for (String option : options)
			{
				if (CommandBase.doesStringStartWith(val, option))
					result.add(option);
			}
			return result;
		}

		@Override
		public boolean hasValidation()
		{
			return true;
		}

		@Override
		public boolean validate(String val)
		{
			for (String action : options)
			{
				if (action.equalsIgnoreCase(val))
					return true;
			}
			return false;
		}
	}

	public static final IArgument IGNORED = new IArgument()
	{
		@Override
		public boolean isUsername()
		{
			return false;
		}

		@Override
		public List<String> getCompletionOptions(String[] args)
		{
			return null;
		}

		@Override
		public boolean hasValidation()
		{
			return false;
		}

		@Override
		public boolean validate(String val)
		{
			return true;
		}
	};

	public static enum MatchResult { FULLY, POSSIBLY, NOT }
}
