package org.ultramine.commands.syntax;

import java.util.List;

public class HandlerBasedArgument implements IArgument
{
	private IArgumentCompletionHandler completionHandler;
	private IArgumentValidationHandler validationHandler;
	private final String name;
	private final String[] params;
	private int[] links;

	public HandlerBasedArgument(String name, String[] params) {
		this.name = name;
		this.params = new String[params.length];
		this.links = new int[params.length];
		boolean hasLinks = false;

		for (int i = 0; i < params.length; i++)
		{
			String param = params[i];
			if (param.startsWith("&"))
			{
				try
				{
					int argNum = Integer.valueOf(param.substring(1));
					this.links[i] = argNum;
					this.params[i] = "";
					hasLinks = true;
					continue;
				}
				catch (Exception ignored) {}
			}
			this.links[i] = -1;
			this.params[i] = param;
		}

		if (!hasLinks) links = null;
	}

	public void setCompletionHandler(IArgumentCompletionHandler completionHandler)
	{
		this.completionHandler = completionHandler;
	}

	public void setValidationHandler(IArgumentValidationHandler validationHandler)
	{
		this.validationHandler = validationHandler;
	}

	public String getHandlerName()
	{
		return name;
	}

	@Override
	public boolean isUsername()
	{
		return completionHandler != null && completionHandler.isUsername();
	}

	@Override
	public List<String> getCompletionOptions(String[] args)
	{
		if (completionHandler == null)
			return null;

		String[] params;
		if (links != null)
		{
			params = new String[this.params.length];
			for (int i = 0; i < this.params.length; i++)
			{
				int link = links[i];
				params[i] = link >= 0 && link < args.length ? args[link] : this.params[i];
			}
		}
		else
			params = this.params;

		return completionHandler.handleCompletion(args[args.length - 1], params);
	}

	@Override
	public boolean hasValidation()
	{
		return validationHandler != null;
	}

	@Override
	public boolean validate(String val)
	{
		return validationHandler == null || validationHandler.handleValidation(val, params);
	}
}
