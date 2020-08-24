package org.ultramine.server.internal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UMInternalRegistry
{
	private static final List<WeakReference<IRemapHandler>> remapHandlers = new ArrayList<>();

	public static void registerRemapHandler(IRemapHandler handler)
	{
		clearRemapHandlers();
		remapHandlers.add(new WeakReference<>(handler));
	}

	private static void clearRemapHandlers()
	{
		remapHandlers.removeIf(ref -> ref.get() == null);
	}

	static void onRemap()
	{
		clearRemapHandlers();
		for(WeakReference<IRemapHandler> ref : remapHandlers)
		{
			IRemapHandler handler = ref.get();
			if(handler != null)
				handler.remap();
		}
	}

	public interface IRemapHandler
	{
		void remap();
	}
}
