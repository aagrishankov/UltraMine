package org.ultramine.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import org.ultramine.core.service.InjectService;
import org.ultramine.core.permissions.Permissions;

import java.util.List;

public class VanillaCommandWrapper implements IExtendedCommand
{
	@InjectService private static Permissions perms;
	private final ICommand wrappedCommand;
	private final String permission;
	private final String description;
	private final String group;

	public VanillaCommandWrapper(ICommand wrappedCommand, String group)
	{
		this.wrappedCommand = wrappedCommand;
		this.group = group;
		this.permission = "command." + group + "." + wrappedCommand.getCommandName();
		this.description = "command." + wrappedCommand.getCommandName().toLowerCase() + ".description";
	}

	@Override
	public String getCommandName()
	{
		return wrappedCommand.getCommandName();
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return wrappedCommand.getCommandUsage(var1);
	}

	@Override
	public List getCommandAliases()
	{
		return wrappedCommand.getCommandAliases();
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2)
	{
		wrappedCommand.processCommand(var1, var2);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1)
	{
		return perms.useVanillaCommandPermissions() ? wrappedCommand.canCommandSenderUseCommand(var1) : perms.has(var1, permission) || !(var1 instanceof EntityPlayer);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2)
	{
		return wrappedCommand.addTabCompletionOptions(var1, var2);
	}

	@Override
	public boolean isUsernameIndex(String[] var1, int var2)
	{
		return wrappedCommand.isUsernameIndex(var1, var2);
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
}
