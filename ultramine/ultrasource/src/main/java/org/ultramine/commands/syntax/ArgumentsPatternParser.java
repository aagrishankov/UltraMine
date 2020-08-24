package org.ultramine.commands.syntax;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentsPatternParser
{
	private static final Logger logger = LogManager.getLogger();

	private static final Pattern argumentPattern = Pattern.compile("([\\[<])\\s*(([^<>%\\[\\]\\s]*)\\s*([^<>%\\[\\]]*))(|%\\s*([^<>%\\[\\]\\s]*)\\s*)[\\]>]");

	private final Map<String, IArgumentCompletionHandler> completionHandlers = new HashMap<String, IArgumentCompletionHandler>();
	private final Map<String, IArgumentValidationHandler> validationHandlers = new HashMap<String, IArgumentValidationHandler>();
	private final List<HandlerBasedArgument> arguments = new ArrayList<HandlerBasedArgument>();

	public ArgumentsPattern parse(String completionString)
	{
		ArgumentsPattern.Builder builder = new ArgumentsPattern.Builder();
		Matcher matcher = argumentPattern.matcher(completionString);

		while (matcher.find())
		{
			if (matcher.group(1).equals("["))
			{
				String[] params = StringUtils.split(matcher.group(2));
				builder.addAction(params);
			}
			else
			{
				String handlerName = matcher.group(3);
				String argumentName = matcher.group(6);
				if (argumentName == null || argumentName.isEmpty())
					argumentName = handlerName;

				String[] params = StringUtils.split(matcher.group(4));
				HandlerBasedArgument argument = new HandlerBasedArgument(handlerName, params);
				argument.setValidationHandler(validationHandlers.get(handlerName));
				argument.setCompletionHandler(completionHandlers.get(handlerName));
				arguments.add(argument);
				builder.addArgument(argumentName, argument);
			}
		}

		if (completionString.endsWith("..."))
			builder.makeInfinite();

		return builder.build();
	}

	public void registerHandler(String name, IArgumentCompletionHandler handler)
	{
		registerHandlerDelayed(name, handler);
		updateArguments();
	}

	public void registerHandler(String name, IArgumentValidationHandler handler)
	{
		registerHandlerDelayed(name, handler);
		updateArguments();
	}

	public void registerHandlers(Class<?> cls)
	{
		for (Method handler : cls.getMethods())
		{
			if (!Modifier.isStatic(handler.getModifiers()))
				continue;

			if (handler.isAnnotationPresent(ArgumentCompleter.class))
			{
				ArgumentCompleter data = handler.getAnnotation(ArgumentCompleter.class);
				registerHandlerDelayed(data.value(), new WrappedCompletionHandler(handler, data.isUsername()));
			}

			if (handler.isAnnotationPresent(ArgumentValidator.class))
			{
				ArgumentValidator data = handler.getAnnotation(ArgumentValidator.class);
				registerHandlerDelayed(data.value(), new WrappedValidationHandler(handler));
			}
		}
		updateArguments();
	}

	private void registerHandlerDelayed(String name, IArgumentCompletionHandler handler)
	{
		if (!completionHandlers.containsKey(name))
			completionHandlers.put(name, handler);
		else
			logger.warn("Completion handler name is already registered: " + name);
	}

	private void registerHandlerDelayed(String name, IArgumentValidationHandler handler)
	{
		if (!validationHandlers.containsKey(name))
			validationHandlers.put(name, handler);
		else
			logger.warn("Validation handler name is already registered: " + name);
	}

	private void updateArguments()
	{
		for (HandlerBasedArgument argument : arguments)
		{
			argument.setCompletionHandler(completionHandlers.get(argument.getHandlerName()));
			argument.setValidationHandler(validationHandlers.get(argument.getHandlerName()));
		}
	}

	private static class WrappedCompletionHandler implements IArgumentCompletionHandler
	{
		private Method method;
		private boolean isUsername;

		private WrappedCompletionHandler(Method method, boolean isUsername)
		{
			this.method = method;
			this.isUsername = isUsername;
		}

		@Override
		public List<String> handleCompletion(String val, String[] params)
		{
			try
			{
				return (List<String>) method.invoke(null, val, params);
			}
			catch (IllegalAccessException ignored)
			{
			}
			catch (InvocationTargetException ignored)
			{
			}

			return null;
		}

		@Override
		public boolean isUsername()
		{
			return isUsername;
		}
	}

	private static class WrappedValidationHandler implements IArgumentValidationHandler
	{
		private Method method;

		private WrappedValidationHandler(Method method)
		{
			this.method = method;
		}

		@Override
		public boolean handleValidation(String val, String[] params)
		{
			try
			{
				return (Boolean) method.invoke(null, val, params);
			}
			catch (IllegalAccessException ignored)
			{
			}
			catch (InvocationTargetException ignored)
			{
			}

			return true;
		}
	}
}
