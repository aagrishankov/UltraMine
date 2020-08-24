package org.ultramine.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import org.ultramine.commands.syntax.ArgumentsPatternParser;
import org.ultramine.server.util.MapWrapper;
import org.ultramine.server.util.SetWrapper;
import org.ultramine.server.util.TranslitTable;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandRegistry
{
	private final Map<String, NameInfo> nameInfos = new HashMap<>();
	private final SortedSet<IExtendedCommand> registeredCommands = new TreeSet<>();
	private final ArgumentsPatternParser argumentsPatternParser = new ArgumentsPatternParser();
	// vanilla compatibility
	@SuppressWarnings("unchecked")
	private final Map<String, IExtendedCommand> commandMap = (Map) new MapWrapper<String, Object>(Maps.transformValues(nameInfos, NameInfo::getCurrent))
	{
		@Override
		public IExtendedCommand put(String key, Object value)
		{
			if(value instanceof IExtendedCommand)
				return addName(key, (IExtendedCommand) value);
			else
				registerVanillaCommand((ICommand) value);
			return null;
		}

		@Override
		public void putAll(Map<? extends String, ?> m)
		{
			for(Map.Entry<? extends String, ?> ent : m.entrySet())
				put(ent.getKey(), ent.getValue());
		}

		@Override
		public IExtendedCommand remove(Object key)
		{
			return key == null || key.getClass() != String.class ? null : remove((String) key);
		}

		private IExtendedCommand remove(String key)
		{
			return null; // TODO remove ?
		}
	};
	@SuppressWarnings("unchecked")
	private final Set<Object> commandSet = new SetWrapper<Object>((Set) registeredCommands)
	{
		@Override
		public boolean add(Object o)
		{
			return false;
		}

		@Override
		public boolean remove(Object o)
		{
			return false;
		}

		@Override
		public boolean addAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return false;
		}
	};

	public CommandRegistry()
	{
	}

	public static class NameInfo
	{
		private final List<IExtendedCommand> available = new ArrayList<>();
		private final String name;
		private IExtendedCommand current;

		private NameInfo(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		IExtendedCommand add(IExtendedCommand command)
		{
			available.remove(command);
			available.add(command);
			IExtendedCommand old = current;
			current = command;
			return old;
		}

		void remove(IExtendedCommand command)
		{
			available.remove(command);
			if(current == command)
				current = available.get(available.size() - 1);
		}

		boolean switchTo(IExtendedCommand command)
		{
			if(current != command && available.remove(command))
			{
				available.add(command);
				current = command;
				return true;
			}

			return false;
		}

		public IExtendedCommand getCurrent()
		{
			return current;
		}
	}

	@SuppressWarnings("unchecked")
	private Iterable<String> getDefaultNames(IExtendedCommand command)
	{
		List<String> aliases = command.getCommandAliases();
		if(aliases == null)
			aliases = Collections.emptyList();
		return Iterables.concat(
				Collections.singletonList(command.getCommandName()),
				aliases
		);
	}

	private Iterable<String> getAllNames(IExtendedCommand command)
	{
		Iterable<String> names = getDefaultNames(command);
		return Iterables.concat(
				names,
				Iterables.transform(names, TranslitTable::translitENRU)
		);
	}

	private IExtendedCommand addName(String name, IExtendedCommand command)
	{
		return nameInfos.computeIfAbsent(name, NameInfo::new).add(command);
	}

	private void removeName(String name, IExtendedCommand command)
	{
		nameInfos.computeIfAbsent(name, NameInfo::new).remove(command);
	}

	public void registerCommand(IExtendedCommand command)
	{
		registeredCommands.add(command);

		addName(command.getGroup() + ":" + command.getCommandName(), command);
		for(String name : getAllNames(command))
			addName(name, command);
	}

	public void unregisterCommand(IExtendedCommand command)
	{
		if(registeredCommands.remove(command))
			for(String name : getAllNames(command))
				removeName(name, command);
	}

	public IExtendedCommand registerVanillaCommand(ICommand command)
	{
		ModContainer active = Loader.instance().activeModContainer();
		IExtendedCommand exCommand = new VanillaCommandWrapper(command, active != null ? active.getModId().toLowerCase() : "vanilla");
		registerCommand(exCommand);
		return exCommand;
	}

	public void registerCommands(Class<?> cls)
	{
		List<HandlerBasedCommand.Builder> builders = new ArrayList<>();
		Map<String, Map<String, ICommandHandler>> actions = new HashMap<>();

		for (Method method : cls.getMethods())
		{
			if (method.isAnnotationPresent(Command.class) && Modifier.isStatic(method.getModifiers()))
			{
				Command data = method.getAnnotation(Command.class);
				HandlerBasedCommand.Builder builder = new HandlerBasedCommand.Builder(data.name(), data.group(), new MethodBasedCommandHandler(method))
						.setAliases(data.aliases())
						.setPermissions(data.permissions())
						.setUsableFromServer(data.isUsableFromServer());

				for (String completion : data.syntax())
					builder.addArgumentsPattern(argumentsPatternParser.parse(completion));

				builders.add(builder);
			}

			if (method.isAnnotationPresent(Action.class) && Modifier.isStatic(method.getModifiers()))
			{
				Action data = method.getAnnotation(Action.class);

				if (!actions.containsKey(data.command()))
					actions.put(data.command(), new HashMap<>());

				actions.get(data.command()).put(data.name(), new MethodBasedCommandHandler(method));
			}
		}

		for (HandlerBasedCommand.Builder builder : builders)
		{
			if (actions.containsKey(builder.getName()))
			{
				for (Map.Entry<String, ICommandHandler> action : actions.get(builder.getName()).entrySet())
					builder.addAction(action.getKey(), action.getValue());
			}

			registerCommand(builder.build());
		}
	}

	public IExtendedCommand get(String name)
	{
		return commandMap.get(name);
	}

	public Map<String, IExtendedCommand> getCommandMap()
	{
		return commandMap;
	}

	public Set<Object> getCommandSet()
	{
		return commandSet;
	}

	public List<String> filterPossibleCommandsNames(ICommandSender sender, String filter)
	{
		List<String> result = new ArrayList<String>();

		for (Map.Entry<String, IExtendedCommand> entry : commandMap.entrySet())
		{
			if (CommandBase.doesStringStartWith(filter, entry.getKey()) && entry.getValue().canCommandSenderUseCommand(sender))
				result.add(entry.getKey());
		}

		return result;
	}

	public List<IExtendedCommand> getPossibleCommands(ICommandSender sender)
	{
		List<IExtendedCommand> result = new ArrayList<IExtendedCommand>();

		for (IExtendedCommand command : registeredCommands)
		{
			if (command.canCommandSenderUseCommand(sender))
				result.add(command);
		}

		return result;
	}

	public ArgumentsPatternParser getArgumentsParser()
	{
		return argumentsPatternParser;
	}
}
