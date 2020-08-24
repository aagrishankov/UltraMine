package net.minecraft.command.server;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CommandBroadcast extends CommandBase
{
	private static final String __OBFID = "CL_00000191";

	public String getCommandName()
	{
		return "say";
	}

	public int getRequiredPermissionLevel()
	{
		return 1;
	}

	public String getCommandUsage(ICommandSender p_71518_1_)
	{
		return "commands.say.usage";
	}

	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_)
	{
		if (p_71515_2_.length > 0 && p_71515_2_[0].length() > 0)
		{
			IChatComponent ichatcomponent = func_147176_a(p_71515_1_, p_71515_2_, 0, true);
			ChatComponentTranslation text = new ChatComponentTranslation("chat.type.announcement", new ChatComponentTranslation("command.say.server"), ichatcomponent);
			text.getChatStyle().setColor(EnumChatFormatting.DARK_PURPLE);
			MinecraftServer.getServer().getConfigurationManager().sendChatMsg(text);
		}
		else
		{
			throw new WrongUsageException("commands.say.usage", new Object[0]);
		}
	}

	public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
	{
		return p_71516_2_.length >= 1 ? getListOfStringsMatchingLastWord(p_71516_2_, MinecraftServer.getServer().getAllUsernames()) : null;
	}
}