/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     cpw - implementation
 */

package cpw.mods.fml.common.event;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.LoaderState.ModState;
import org.ultramine.commands.CommandRegistry;
import org.ultramine.commands.IExtendedCommand;
import org.ultramine.commands.syntax.ArgumentsPatternParser;
import org.ultramine.commands.syntax.IArgumentCompletionHandler;
import org.ultramine.commands.syntax.IArgumentValidationHandler;

public class FMLServerStartingEvent extends FMLStateEvent
{

	private MinecraftServer server;

	public FMLServerStartingEvent(Object... data)
	{
		super(data);
		this.server = (MinecraftServer) data[0];
	}
	@Override
	public ModState getModState()
	{
		return ModState.AVAILABLE;
	}

	public MinecraftServer getServer()
	{
		return server;
	}

	public void registerServerCommand(ICommand command)
	{
		CommandHandler ch = (CommandHandler) getServer().getCommandManager();
		ch.registerCommand(command);
	}

	/* ========================================= ULTRAMINE START ======================================*/

	public void registerCommand(IExtendedCommand command)
	{
		getCommandRegistry().registerCommand(command);
	}

	public void registerCommands(Class<?> holder)
	{
		getCommandRegistry().registerCommands(holder);
	}

	public void registerArgumentHandler(String argumentType, IArgumentCompletionHandler handler)
	{
		getArgumentsParser().registerHandler(argumentType, handler);
	}

	public void registerArgumentHandler(String argumentType, IArgumentValidationHandler handler)
	{
		getArgumentsParser().registerHandler(argumentType, handler);
	}

	public void registerArgumentHandlers(Class<?> holder)
	{
		getArgumentsParser().registerHandlers(holder);
	}

	private CommandRegistry getCommandRegistry()
	{
		return ((CommandHandler) getServer().getCommandManager()).getRegistry();
	}

	private ArgumentsPatternParser getArgumentsParser()
	{
		return getCommandRegistry().getArgumentsParser();
	}
}