package org.ultramine.core.economy.account;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface PlayerAccount extends Account
{
	@Nonnull
	GameProfile getProfile();
}
