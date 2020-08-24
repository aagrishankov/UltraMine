package org.ultramine.server.internal;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.ultramine.core.permissions.Permissions;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class OpBasedPermissions implements Permissions
{
	private final Set<String> defaultPermissions = new HashSet<>();

	public void addDefault(String permission)
	{
		defaultPermissions.add(permission);
	}

	private boolean hasDefault(String permission)
	{
		return defaultPermissions.contains(permission);
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return hasDefault(permission);
	}

	public boolean has(String world, GameProfile player, String permission)
	{
		return MinecraftServer.getServer().getConfigurationManager().func_152596_g(player) || hasDefault(permission);
	}

	@Override
	public @Nonnull String getMeta(String world, String player, String key)
	{
		return "";
	}

	@Override
	public @Nonnull String getMeta(String world, GameProfile player, String key)
	{
		return "";
	}

	public boolean useVanillaCommandPermissions()
	{
		return true;
	}
}
