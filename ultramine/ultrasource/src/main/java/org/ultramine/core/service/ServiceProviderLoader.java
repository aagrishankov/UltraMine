package org.ultramine.core.service;

public interface ServiceProviderLoader<T>
{
	void load(ServiceDelegate<T> service);

	void unload();
}
