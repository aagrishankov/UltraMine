package net.minecraft.command;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.ultramine.commands.CommandRegistry;

public class CommandHandler implements ICommandManager
{
	private static final Logger logger = LogManager.getLogger();
	private final CommandRegistry registry = new CommandRegistry();
	private final Map commandMap = registry.getCommandMap();
	private final Set commandSet = registry.getCommandSet();
	private static final String __OBFID = "CL_00001765";

	public int executeCommand(ICommandSender par1ICommandSender, String par2Str)
	{
		par2Str = par2Str.trim();

		if (par2Str.startsWith("/"))
		{
			par2Str = par2Str.substring(1);
		}

		String[] astring = par2Str.split(" ");
		String s1 = astring[0];
		astring = dropFirstString(astring);
		ICommand icommand = registry.get(s1);
		int i = this.getUsernameIndex(icommand, astring);
		int j = 0;
		ChatComponentTranslation chatcomponenttranslation;

		try
		{
			if (icommand == null)
			{
				throw new CommandNotFoundException();
			}

			if (icommand.canCommandSenderUseCommand(par1ICommandSender))
			{
				CommandEvent event = new CommandEvent(icommand, par1ICommandSender, astring);
				if (MinecraftForge.EVENT_BUS.post(event))
				{
					if (event.exception != null)
					{
						throw event.exception;
					}
					return 1;
				}

				if (i > -1)
				{
					EntityPlayerMP[] aentityplayermp = PlayerSelector.matchPlayers(par1ICommandSender, astring[i]);
					String s2 = astring[i];
					EntityPlayerMP[] aentityplayermp1 = aentityplayermp;
					int k = aentityplayermp.length;

					for (int l = 0; l < k; ++l)
					{
						EntityPlayerMP entityplayermp = aentityplayermp1[l];
						astring[i] = entityplayermp.getCommandSenderName();

						try
						{
							icommand.processCommand(par1ICommandSender, astring);
							++j;
						}
						catch (CommandException commandexception)
						{
							ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorOjbects());
							chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
							par1ICommandSender.addChatMessage(chatcomponenttranslation1);
						}
					}

					astring[i] = s2;
				}
				else
				{
					icommand.processCommand(par1ICommandSender, astring);
					++j;
				}
			}
			else
			{
				ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
				chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
				par1ICommandSender.addChatMessage(chatcomponenttranslation2);
			}
		}
		catch (WrongUsageException wrongusageexception)
		{
			chatcomponenttranslation = new ChatComponentTranslation("commands.generic.usage", new Object[] {new ChatComponentTranslation(wrongusageexception.getMessage(), wrongusageexception.getErrorOjbects())});
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
			par1ICommandSender.addChatMessage(chatcomponenttranslation);
		}
		catch (CommandException commandexception1)
		{
			chatcomponenttranslation = new ChatComponentTranslation(commandexception1.getMessage(), commandexception1.getErrorOjbects());
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
			par1ICommandSender.addChatMessage(chatcomponenttranslation);
		}
		catch (Throwable throwable)
		{
			chatcomponenttranslation = new ChatComponentTranslation("commands.generic.exception", new Object[0]);
			chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
			par1ICommandSender.addChatMessage(chatcomponenttranslation);
			logger.error("Couldn\'t process command", throwable);
		}

		return j;
	}

	public ICommand registerCommand(ICommand par1ICommand)
	{
		return registry.registerVanillaCommand(par1ICommand);
	}

	public CommandRegistry getRegistry()
	{
		return registry;
	}

	private static String[] dropFirstString(String[] par0ArrayOfStr)
	{
		String[] astring1 = new String[par0ArrayOfStr.length - 1];

		for (int i = 1; i < par0ArrayOfStr.length; ++i)
		{
			astring1[i - 1] = par0ArrayOfStr[i];
		}

		return astring1;
	}

	public List getPossibleCommands(ICommandSender par1ICommandSender, String par2Str)
	{
		String[] astring = par2Str.split(" ", -1);
		String s1 = astring[0];

		if (astring.length == 1)
		{
			return registry.filterPossibleCommandsNames(par1ICommandSender, s1);
		}
		else
		{
			if (astring.length > 1)
			{
				ICommand icommand = registry.get(s1);

				if (icommand != null)
				{
					return icommand.addTabCompletionOptions(par1ICommandSender, dropFirstString(astring));
				}
			}

			return null;
		}
	}

	public List getPossibleCommands(ICommandSender par1ICommandSender)
	{
		return registry.getPossibleCommands(par1ICommandSender);
	}

	public Map getCommands()
	{
		return registry.getCommandMap();
	}

	private int getUsernameIndex(ICommand par1ICommand, String[] par2ArrayOfStr)
	{
		if (par1ICommand == null)
		{
			return -1;
		}
		else
		{
			for (int i = 0; i < par2ArrayOfStr.length; ++i)
			{
				if (par1ICommand.isUsernameIndex(par2ArrayOfStr, i) && PlayerSelector.matchesMultiplePlayers(par2ArrayOfStr[i]))
				{
					return i;
				}
			}

			return -1;
		}
	}
}