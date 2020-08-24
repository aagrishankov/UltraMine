package org.ultramine.core.service;

import cpw.mods.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

public abstract class ServiceSwitchEvent extends Event
{
	private final Class<?> serviceClass;
	private final ServiceDelegate<?> delegate;
	private final @Nonnull ServiceProviderLoader<?> oldProviderLoader;
	private final @Nonnull ServiceProviderLoader<?> newProviderLoader;

	public ServiceSwitchEvent(Class<?> serviceClass, ServiceDelegate<?> delegate, ServiceProviderLoader<?> oldProviderLoader, @Nonnull ServiceProviderLoader<?> newProviderLoader)
	{
		this.serviceClass = serviceClass;
		this.delegate = delegate;
		this.oldProviderLoader = oldProviderLoader;
		this.newProviderLoader = newProviderLoader;
	}

	public ServiceDelegate<?> getServiceDelegate()
	{
		return delegate;
	}

	public Class<?> getServiceClass()
	{
		return serviceClass;
	}

	@Nonnull
	public ServiceProviderLoader<?> getOldProviderLoader()
	{
		return oldProviderLoader;
	}

	@Nonnull
	public ServiceProviderLoader<?> getNewProviderLoader()
	{
		return newProviderLoader;
	}

	public static class Pre extends ServiceSwitchEvent
	{
		public Pre(Class<?> serviceClass, ServiceDelegate<?> delegate, ServiceProviderLoader<?> oldProvider, @Nonnull ServiceProviderLoader<?> newProvider)
		{
			super(serviceClass, delegate, oldProvider, newProvider);
		}
	}

	public static class Post extends ServiceSwitchEvent
	{
		public Post(Class<?> serviceClass, ServiceDelegate<?> delegate, ServiceProviderLoader<?> oldProvider, @Nonnull ServiceProviderLoader<?> newProvider)
		{
			super(serviceClass, delegate, oldProvider, newProvider);
		}
	}
}
