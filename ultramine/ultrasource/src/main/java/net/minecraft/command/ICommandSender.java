package net.minecraft.command;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public interface ICommandSender
{
	String getCommandSenderName();

	IChatComponent func_145748_c_();

	void addChatMessage(IChatComponent p_145747_1_);

	boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_);

	ChunkCoordinates getPlayerCoordinates();

	World getEntityWorld();
}