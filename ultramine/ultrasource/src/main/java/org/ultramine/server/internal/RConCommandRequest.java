package org.ultramine.server.internal;

import net.minecraft.server.MinecraftServer;

import java.util.function.Supplier;

public class RConCommandRequest implements Supplier<String>
{
	private final String command;

	public RConCommandRequest(String command)
	{
		this.command = command;
	}

	@Override
	public String get()
	{
		return MinecraftServer.getServer().handleRConCommand(command);
	}
}
