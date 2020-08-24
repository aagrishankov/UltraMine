package org.ultramine.core.service;

public interface ServiceStateHandler
{
	default void onEnabled() {}

	default void onDisabled() {}
}
