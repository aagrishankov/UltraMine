package org.ultramine.commands.basic;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import org.ultramine.commands.IExtendedCommand;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.Teleporter;
import org.ultramine.core.permissions.Permissions;

public class FastWarpCommand implements IExtendedCommand
{
	@InjectService private static Permissions perms;
	private final String name;
	
	public FastWarpCommand(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getCommandName()
	{
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_)
	{
		return '/'+name;
	}

	@Override
	public List<String> getCommandAliases()
	{
		return Collections.emptyList();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		Teleporter.tpLater((EntityPlayerMP)sender, MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(name));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return sender instanceof EntityPlayerMP && perms.has(sender, "command.fastwarp."+name);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
	{
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
	{
		return false;
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
		return "command.fastwarp.use.description";
	}

	@Override
	public String getGroup()
	{
		return "basic";
	}
}
