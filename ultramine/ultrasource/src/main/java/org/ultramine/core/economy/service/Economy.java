package org.ultramine.core.economy.service;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import org.ultramine.core.economy.account.PlayerAccount;
import org.ultramine.core.service.Service;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@Service
@ThreadSafe
public interface Economy
{
	@Nonnull
	PlayerAccount getPlayerAccount(@Nonnull GameProfile profile);

	@Nonnull
	default PlayerAccount getPlayerAccount(@Nonnull EntityPlayerMP player)
	{
		return getPlayerAccount(player.getGameProfile());
	}
}
