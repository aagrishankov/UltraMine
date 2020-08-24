package net.minecraft.command;

import java.util.List;

public interface ICommand extends Comparable
{
	String getCommandName();

	String getCommandUsage(ICommandSender p_71518_1_);

	List getCommandAliases();

	void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_);

	boolean canCommandSenderUseCommand(ICommandSender p_71519_1_);

	List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_);

	boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_);
}