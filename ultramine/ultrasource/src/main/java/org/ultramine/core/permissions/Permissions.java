package org.ultramine.core.permissions;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.ultramine.core.service.Service;
import org.ultramine.server.world.WorldDescriptor;

import javax.annotation.Nonnull;

@Service
public interface Permissions
{
	boolean has(String world, String player, String permission);

	@Nonnull String getMeta(String world, String player, String key);

	default boolean has(String world, GameProfile player, String permission)
	{
		return has(world, player.getName(), permission);
	}

	default boolean has(WorldDescriptor world, GameProfile player, String permission)
	{
		return has(world.getName(), player, permission);
	}

	default boolean has(World world, GameProfile player, String permission)
	{
		return has(MinecraftServer.getServer().getMultiWorld().getDescByID(world.provider.dimensionId), player, permission);
	}

	default boolean has(ICommandSender player, String permission)
	{
		return !(player instanceof EntityPlayerMP) || has(player.getEntityWorld(), ((EntityPlayerMP) player).getGameProfile(), permission);
	}

	default boolean hasAny(ICommandSender player, String... permissions)
	{
		if (permissions == null)
			return true;

		for (String permission : permissions)
			if (has(player, permission))
				return true;
		return false;
	}

	default @Nonnull String getMeta(String world, GameProfile player, String permission)
	{
		return getMeta(world, player.getName(), permission);
	}

	default @Nonnull String getMeta(WorldDescriptor world, GameProfile player, String permission)
	{
		return getMeta(world.getName(), player, permission);
	}

	default @Nonnull String getMeta(World world, GameProfile player, String permission)
	{
		return getMeta(MinecraftServer.getServer().getMultiWorld().getDescByID(world.provider.dimensionId), player, permission);
	}

	default @Nonnull String getMeta(ICommandSender player, String permission)
	{
		if(!(player instanceof EntityPlayerMP))
			return "";
		return getMeta(player.getEntityWorld(), ((EntityPlayerMP) player).getGameProfile(), permission);
	}

	default boolean useVanillaCommandPermissions()
	{
		return false;
	}
}
