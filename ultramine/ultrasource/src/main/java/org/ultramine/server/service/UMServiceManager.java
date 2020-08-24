package org.ultramine.server.service;

import net.minecraftforge.common.MinecraftForge;
import org.ultramine.core.service.Service;
import org.ultramine.core.service.ServiceDelegate;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.core.service.ServiceProviderLoader;
import org.ultramine.core.service.ServiceStateHandler;
import org.ultramine.core.service.ServiceSwitchEvent;
import org.ultramine.core.util.Undoable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public class UMServiceManager implements ServiceManager
{
	private final Map<Class<?>, ServiceWrapper> services = new ConcurrentHashMap<>();

	public UMServiceManager()
	{
		NotResolvedServiceProvider.services = this;
	}

	@SuppressWarnings("unchecked")
	private @Nonnull <T> ServiceWrapper<T> getOrCreateService(Class<T> serviceClass1)
	{
		return ((Map<Class<T>, ServiceWrapper<T>>) (Object) services).computeIfAbsent(serviceClass1, serviceClass -> {
			Service desc = serviceClass.getAnnotation(Service.class);
			if(desc == null)
				throw new IllegalArgumentException("Given class is not a service class: "+serviceClass.getName());
			ServiceDelegate<T> delegate;
			T notResolvedProvider;
			try {
				delegate = ServiceDelegateGenerator.makeServiceDelegate(getClass(), serviceClass.getSimpleName() + "_delegate", serviceClass).newInstance();
				notResolvedProvider = ServiceDelegateGenerator.makeNotResolvedServiceProvider(getClass(), serviceClass.getSimpleName() + "_notResolvedProvider", serviceClass).newInstance();
			} catch(InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return new ServiceWrapper<>(serviceClass, delegate, desc, new ServiceProviderRegistration<>(new SimpleServiceProviderLoader<>(notResolvedProvider), Integer.MIN_VALUE));
		});
	}

	@Override
	public <T> Undoable register(@Nonnull Class<T> serviceClass, @Nonnull T provider, int priority)
	{
		serviceClass.getClass(); // NPE
		provider.getClass(); // NPE
		return register(serviceClass, new SimpleServiceProviderLoader<>(provider), priority);
	}

	@Override
	public <T> Undoable register(@Nonnull Class<T> serviceClass, @Nonnull ServiceProviderLoader<T> providerLoader, int priority)
	{
		serviceClass.getClass(); // NPE
		providerLoader.getClass(); // NPE
		ServiceWrapper<T> service = getOrCreateService(serviceClass);
		return service.addProvider(new ServiceProviderRegistration<>(providerLoader, priority));
	}

	@Nonnull
	@Override
	public <T> T provide(@Nonnull Class<T> service)
	{
		return getOrCreateService(service).provide();
	}

	Object resolveProvider(NotResolvedServiceProvider provider)
	{
		Class<?> serviceClass = provider.getClass().getInterfaces()[0];
		return getOrCreateService(serviceClass).resolveProvider();
	}

	private static class ServiceWrapper<T>
	{
		private final Class<T> serviceClass;
		private final ServiceDelegate<T> delegate;
		private final Service desc;
		private final ServiceProviderRegistration<T> notResolvedProviderRegistration;
		private final List<ServiceProviderRegistration<T>> providers = new ArrayList<>();
		private ServiceProviderRegistration<T> currentProvider;

		public ServiceWrapper(Class<T> serviceClass, ServiceDelegate<T> delegate, Service desc, ServiceProviderRegistration<T> notResolvedRegistration)
		{
			this.serviceClass = serviceClass;
			this.delegate = delegate;
			this.desc = desc;
			this.notResolvedProviderRegistration = notResolvedRegistration;
			this.currentProvider = notResolvedProviderRegistration;
			notResolvedRegistration.providerLoader.load(delegate);
		}

		public Service getDesc()
		{
			return desc;
		}

		private void switchTo(ServiceProviderRegistration<T> newProvider)
		{
			if(providers.isEmpty() && newProvider != notResolvedProviderRegistration)
				throw new IllegalStateException("Service provider is not registered");
			ServiceProviderRegistration<T> oldProvider = currentProvider;
			MinecraftForge.EVENT_BUS.post(new ServiceSwitchEvent.Pre(serviceClass, delegate, oldProvider == null ? null : oldProvider.providerLoader, newProvider.providerLoader));
			if(oldProvider != null)
			{
				if(delegate.getProvider() instanceof ServiceStateHandler)
					((ServiceStateHandler)delegate.getProvider()).onDisabled();
				oldProvider.providerLoader.unload();
			}
			newProvider.providerLoader.load(delegate);
			if(delegate.getProvider() instanceof ServiceStateHandler)
				((ServiceStateHandler)delegate.getProvider()).onEnabled();
			currentProvider = newProvider;
			MinecraftForge.EVENT_BUS.post(new ServiceSwitchEvent.Post(serviceClass, delegate, oldProvider == null ? null : oldProvider.providerLoader, newProvider.providerLoader));
		}

		private void forceSwitchToMostRelevant()
		{
			if(providers.isEmpty())
				switchTo(notResolvedProviderRegistration);
			else
				//noinspection OptionalGetWithoutIsPresent
				switchTo(providers.stream().sorted(Comparator.comparingInt((ServiceProviderRegistration o) -> o.priority).reversed()).findFirst().get());
		}

		private boolean isResolved()
		{
			return currentProvider != notResolvedProviderRegistration;
		}

		public synchronized Undoable addProvider(ServiceProviderRegistration<T> provider)
		{
			if(desc.singleProvider() && providers.size() != 0)
				throw new IllegalStateException("Tried to register second provider for single-impl service'"+serviceClass.getName() +
						"'. First provider: " + providers.get(0).providerLoader + ", second provider: " + provider);
			providers.add(provider);
			if((isResolved() || desc.singleProvider()) && provider.priority >= currentProvider.priority)
				switchTo(provider);

			return () -> {
				synchronized(this) {
					providers.remove(provider);
					if(isResolved() && provider == currentProvider)
						forceSwitchToMostRelevant();
				}
			};
		}

		public synchronized T provide()
		{
			return delegate.asService();
		}

		public synchronized T resolveProvider()
		{
			if(providers.isEmpty())
				throw new IllegalStateException("Service provider is not registered for service " + serviceClass);
			forceSwitchToMostRelevant();
			return delegate.getProvider();
		}
	}

	private static class ServiceProviderRegistration<T> implements Comparable<ServiceProviderRegistration>
	{
		public final ServiceProviderLoader<T> providerLoader;
		public final int priority;

		private ServiceProviderRegistration(ServiceProviderLoader<T> providerLoader, int priority)
		{
			this.providerLoader = providerLoader;
			this.priority = priority;
		}

		public int compareTo(ServiceProviderRegistration o)
		{
			return Integer.compare(priority, o.priority);
		}
	}

	private static class SimpleServiceProviderLoader<T> implements ServiceProviderLoader<T>
	{
		public final T provider;

		private SimpleServiceProviderLoader(T provider)
		{
			this.provider = provider;
		}

		@Override
		public void load(ServiceDelegate<T> service)
		{
			service.setProvider(provider);
		}

		@Override
		public void unload()
		{

		}

		@Override
		public String toString()
		{
			return "SimpleServiceProviderLoader{" +
					"provider=" + provider +
					'}';
		}
	}
}
