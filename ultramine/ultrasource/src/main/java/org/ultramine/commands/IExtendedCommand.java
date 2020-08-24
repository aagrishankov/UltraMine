package org.ultramine.commands;

import net.minecraft.command.ICommand;

public interface IExtendedCommand extends ICommand
{
	public String getDescription();
	public String getGroup();
}