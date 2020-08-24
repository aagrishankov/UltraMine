package org.ultramine.core.service;

import org.ultramine.server.service.UMServiceManager;

public class ServiceBytecodeAdapter
{
	private static ServiceManager manager = new UMServiceManager();

	static
	{
		manager.register(ServiceManager.class, manager, 0);
	}

	public static Object provideService(Class<?> serviceClass)
	{
		return manager.provide(serviceClass);
	}
}
