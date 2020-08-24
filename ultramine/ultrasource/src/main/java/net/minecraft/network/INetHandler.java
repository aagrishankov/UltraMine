package net.minecraft.network;

import net.minecraft.util.IChatComponent;

public interface INetHandler
{
	void onDisconnect(IChatComponent p_147231_1_);

	void onConnectionStateTransition(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_);

	void onNetworkTick();
}