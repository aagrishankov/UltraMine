package net.minecraft.command;

import java.util.List;
import java.util.Map;

public interface ICommandManager
{
	int executeCommand(ICommandSender p_71556_1_, String p_71556_2_);

	List getPossibleCommands(ICommandSender p_71558_1_, String p_71558_2_);

	List getPossibleCommands(ICommandSender p_71557_1_);

	Map getCommands();
}