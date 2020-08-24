package org.ultramine.commands;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.core.economy.service.Economy;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.service.InjectService;
import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.BasicTypeFormatter;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.core.permissions.Permissions;
import org.ultramine.server.util.GlobalExecutors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CommandContext
{
	@InjectService private static Permissions perms;
	@InjectService private static Economy economy;
	private static final Logger log = LogManager.getLogger();
	private ICommandSender sender;
	private String[] args;
	private IExtendedCommand command;
	private Map<String, Argument> argumentMap;
	private int lastArgumentNum;
	private String actionName;
	private ICommandHandler actionHandler;

	private CommandContext(IExtendedCommand command, ICommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;
		this.command = command;
		this.argumentMap = new HashMap<String, Argument>(args.length);
		this.actionName = "";
		this.lastArgumentNum = args.length - 1;
	}

	public Argument get(String key)
	{
		if (!argumentMap.containsKey(key))
			throwBadUsage();

		return argumentMap.get(key);
	}

	public Argument get(int num)
	{
		if (num < 0 || num >= args.length)
			throwBadUsage();

		return new Argument(num);
	}

	public boolean contains(String key)
	{
		return argumentMap.containsKey(key);
	}

	public Argument set(String key, String value)
	{
		Argument arg = new Argument(value);
		argumentMap.put(key, arg);
		return arg;
	}

	public String getAction()
	{
		return actionName;
	}

	public boolean actionIs(String action)
	{
		return actionName.equalsIgnoreCase(action);
	}

	public void doAction()
	{
		if (actionHandler != null)
			actionHandler.processCommand(this);
	}

	public ICommandSender getSender()
	{
		return sender;
	}

	public boolean senderIsServer()
	{
		return !(sender instanceof EntityPlayer);
	}
	
	public boolean senderIsPlayer()
	{
		return sender instanceof EntityPlayer;
	}

	public EntityPlayerMP getSenderAsPlayer()
	{
		return CommandBase.getCommandSenderAsPlayer(sender);
	}

	public void notifyAdmins(String messageKey, Object... messageArgs)
	{
		CommandBase.func_152373_a(sender, null, messageKey, messageArgs);
	}
	
	public void notifyOtherAdmins(String messageKey, Object... messageArgs)
	{
		CommandBase.func_152374_a(sender, null, 1, messageKey, messageArgs);
	}

	public void checkSenderPermission(String permission)
	{
		checkSenderPermission(permission, "command.generic.permission");
	}
	
	public void checkSenderPermission(String permission, String msg)
	{
		if (!senderIsServer() && !perms.has(sender, permission))
			throw new CommandException(msg);
	}

	public void checkSenderPermissionInWorld(String world, String permission)
	{
		if (!senderIsServer() && !perms.has(world, sender.getCommandSenderName(), permission))
			throw new CommandException("command.generic.permission");
	}
	
	public void checkPermissionIfArg(String arg, String permission, String msg)
	{
		if(contains(arg))
			checkSenderPermission(permission, msg);
	}


	/* thread-safe */
	public void sendMessage(IChatComponent comp)
	{
		if(Thread.currentThread() == getServer().getServerThread())
			sender.addChatMessage(comp);
		else
			GlobalExecutors.nextTick().execute(() -> sender.addChatMessage(comp));
	}

	public void sendMessage(EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		sendMessage(BasicTypeFormatter.formatMessage(tplColor, argsColor, msg, args));
	}
	
	public void sendMessage(EnumChatFormatting argsColor, String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.GOLD, argsColor, msg, args);
	}
	
	public void sendMessage(String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.YELLOW, msg, args);
	}

	public void broadcast(IChatComponent comp)
	{
		getServer().getConfigurationManager().sendChatMsg(comp);
	}
	
	public void broadcast(EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		broadcast(BasicTypeFormatter.formatMessage(tplColor, argsColor, msg, args));
	}
	
	public void broadcast(EnumChatFormatting argsColor, String msg, Object... args)
	{
		broadcast(EnumChatFormatting.DARK_PURPLE, argsColor, msg, args);
	}
	
	public void broadcast(String msg, Object... args)
	{
		broadcast(EnumChatFormatting.DARK_PURPLE, msg, args);
	}
	
	public void sendMessage(ICommandSender to, EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		to.addChatMessage(BasicTypeFormatter.formatMessage(tplColor, argsColor, msg, args));
	}
	
	public void sendMessage(ICommandSender to, EnumChatFormatting argsColor, String msg, Object... args)
	{
		sendMessage(to, EnumChatFormatting.GOLD, argsColor, msg, args);
	}
	
	public void sendMessage(ICommandSender to, String msg, Object... args)
	{
		sendMessage(to, EnumChatFormatting.YELLOW, msg, args);
	}

	public void throwBadUsage()
	{
		throw new WrongUsageException(command.getCommandUsage(sender));
	}
	
	public void failure(String msg)
	{
		throw new CommandException(msg);
	}
	
	public void failure(String msg, Object... params)
	{
		throw new CommandException(msg, params);
	}
	
	public void check(boolean flag, String msg)
	{
		if(!flag)
			throw new CommandException(msg);
	}

	public String[] getArgs()
	{
		return args;
	}

	public IExtendedCommand getCommand()
	{
		return command;
	}
	
	public MinecraftServer getServer()
	{
		return MinecraftServer.getServer();
	}
	
	public ServerDataLoader getServerData()
	{
		return getServer().getConfigurationManager().getDataLoader();
	}

	/* thread-safe */
	public void handleException(@Nullable Throwable throwable)
	{
		if(throwable == null)
			return;
		if(throwable instanceof CompletionException && throwable.getCause() != null)
			throwable = throwable.getCause();
		if(throwable instanceof WrongUsageException)
		{
			CommandException cmdEx = (CommandException) throwable;
			ChatComponentTranslation msg = new ChatComponentTranslation("commands.generic.usage", new ChatComponentTranslation(cmdEx.getMessage(), cmdEx.getErrorOjbects()));
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			sendMessage(msg);
		}
		else if(throwable instanceof CommandException)
		{
			CommandException cmdEx = (CommandException) throwable;
			ChatComponentTranslation msg = new ChatComponentTranslation(cmdEx.getMessage(), cmdEx.getErrorOjbects());
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			sendMessage(msg);
		}
		else
		{
			ChatComponentTranslation msg = new ChatComponentTranslation("commands.generic.exception");
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			sendMessage(msg);
			log.error("Couldn\'t process command", throwable);
		}
	}

	/* thread-safe */
	public CompletableFuture<Void> handleException(CompletableFuture<?> future)
	{
		return future.handle((o, throwable) -> {
			handleException(throwable);
			return null;
		});
	}

	/* thread-safe */
	public void finishAfter(CompletableFuture<?> future)
	{
		future.whenComplete((o, throwable) -> finish(throwable));
	}

	/* thread-safe */
	public void finish()
	{
		// TODO For async commands, respond RCon only after this method invocation
	}

	public void finish(@Nullable Throwable throwable)
	{
		handleException(throwable);
		finish();
	}


	public class Argument
	{
		private int num;
		private boolean last;
		private String value;

		private Argument(int num)
		{
			this.value = args[num];
			this.num = num;
			this.last = num == lastArgumentNum;
		}

		private Argument(int num, boolean last)
		{
			this.value = args[num];
			this.num = num;
			this.last = last;
		}

		private Argument(String value)
		{
			this.value = value;
			this.num = -1;
			this.last = false;
		}

		private String[] args()
		{
			if (num >= 0)
				return args;
			else
				return new String[] {value};
		}

		private int num()
		{
			return Math.max(num, 0);
		}

		public String asString()
		{
			if (last)
				return CommandBase.func_82360_a(sender, args(), num());
			else
				return value;
		}

		public Argument[] asArray()
		{
			if (num < 0)
				return new Argument[] {this};

			Argument[] result = new Argument[args.length - num];
			for (int i = num; i < args.length; i++)
				result[i-num] = new Argument(i, false);
			return result;
		}
		
		public String[] asStringArray()
		{
			if(num >= 0 && last && args.length > num+1)
				return Arrays.copyOfRange(args, num, args.length);
			return new String[]{value};
		}
		
		public Map<String, List<String>> asFlags(String... allowedArr)
		{
			Set<String> allowed = allowedArr.length == 0 ? null : new HashSet<String>(Arrays.asList(allowedArr));
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			String curFlag = null;
			List<String> curList = new ArrayList<String>(0);
			for(String s : asStringArray())
			{
				if(s.startsWith("-"))
				{
					if(curFlag != null)
					{
						map.put(curFlag, curList);
						curList = new ArrayList<String>(0);
					}
					
					curFlag = s.substring(1);
					if(allowed != null && !allowed.contains(curFlag))
						failure("###unknown flag %s", curFlag); //TODO
				}
				else
				{
					curList.add(s);
				}
			}
			if(curFlag != null)
				map.put(curFlag, curList);

			return map;
		}

		public int asInt()
		{
			return CommandBase.parseInt(sender, value);
		}

		public int asInt(int minBound)
		{
			return CommandBase.parseIntWithMin(sender, value, minBound);
		}

		public int asInt(int minBound, int maxBound)
		{
			return CommandBase.parseIntBounded(sender, value, minBound, maxBound);
		}

		public double asDouble()
		{
			return CommandBase.parseDouble(sender, value);
		}

		public double asDouble(double minBound)
		{
			return CommandBase.parseDoubleWithMin(sender, value, minBound);
		}

		public double asDouble(double minBound, double maxBound)
		{
			return CommandBase.parseDoubleBounded(sender, value, minBound, maxBound);
		}

		public boolean asBoolean()
		{
			return CommandBase.parseBoolean(sender, value);
		}

		public EntityPlayerMP asPlayer()
		{
			return CommandBase.getPlayer(sender, value);
		}
		
		public PlayerData asPlayerData()
		{
			PlayerData data = getServerData().getPlayerData(value);
			if(data == null)
				throw new PlayerNotFoundException();
			return data;
		}
		
		public OfflinePlayer asOfflinePlayer()
		{
			return new OfflinePlayer(getServer(), asPlayerData());
		}

		public PlayerAccount asAccount()
		{
			return economy.getPlayerAccount(asPlayerData().getProfile());
		}
		
		public WorldServer asWorld()
		{
			WorldServer world = MinecraftServer.getServer().getMultiWorld().getWorldByNameOrID(value);
			if(world == null)
				throw new CommandException("command.generic.world.invalid", value);
			return world;
		}

		public IChatComponent asChatComponent(boolean emphasizePlayers)
		{
			return CommandBase.func_147176_a(sender, args(), num(), emphasizePlayers);
		}

		public double asCoordinate(double original)
		{
			String val = value;
			if(!Character.isDigit(val.charAt(value.length()-1)) && val.length() > 1)
				val = val.substring(0, value.length()-1);
			return CommandBase.func_110666_a(sender, original, val);
		}

		public double asCoordinate(double original, int minBound, int maxBound)
		{
			String val = value;
			if(!Character.isDigit(val.charAt(value.length()-1)) && val.length() > 1)
				val = val.substring(0, value.length()-1);
			return CommandBase.func_110665_a(sender, original, val, minBound, maxBound);
		}

		public Item asItem()
		{
			return CommandBase.getItemByText(sender, value);
		}

		public Block asBlock()
		{
			return CommandBase.getBlockByText(sender, value);
		}

		public ItemStack asItemStack()
		{
			return BasicTypeParser.parseItemStack(asString());
		}
		
		public long asTimeMills()
		{
			return BasicTypeParser.parseTime(asString());
		}
	}

	public static class Builder
	{
		private CommandContext context;

		public Builder(IExtendedCommand command, ICommandSender sender, String[] args)
		{
			context = new CommandContext(command, sender, args);
		}

		public Builder resolveArguments(List<String> names)
		{
			context.lastArgumentNum = names.size() - 1;
			Map<String, Integer> nameCount = new HashMap<String, Integer>();
			for (int i = 0; i < names.size(); i++)
			{
				String name = names.get(i);

				if (name == null || name.isEmpty())
					continue;

				if (context.argumentMap.containsKey(name))
				{
					Integer count = nameCount.containsKey(name) ? nameCount.get(name) + 1 : 2;
					nameCount.put(name, count);
					name = name + count.toString();
				}

				context.argumentMap.put(name, context.new Argument(i));
			}
			return this;
		}

		public Builder setAction(String actionName, ICommandHandler actionHandler)
		{
			context.actionName = actionName;
			context.actionHandler = actionHandler;
			return this;
		}

		public CommandContext build()
		{
			return context;
		}
	}
}
