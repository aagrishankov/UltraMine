package org.ultramine.core.service;

import org.ultramine.core.util.Undoable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@Service(
		singleProvider = true
)
@ThreadSafe
public interface ServiceManager
{
	<T> Undoable register(@Nonnull Class<T> serviceClass, @Nonnull T provider, int priority);

	<T> Undoable register(@Nonnull Class<T> serviceClass, @Nonnull ServiceProviderLoader<T> providerLoader, int priority);

	@Nonnull <T> T provide(@Nonnull Class<T> service);
}
