package org.ultramine.server.service;

public class NotResolvedServiceProvider
{
	// This hack is needed because this class is requested before UMServiceManager registering
	public static UMServiceManager services;

	public Object resolveProvider()
	{
		return services.resolveProvider(this);
	}
}
