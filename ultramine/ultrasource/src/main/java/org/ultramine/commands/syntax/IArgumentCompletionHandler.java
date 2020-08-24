package org.ultramine.commands.syntax;

import java.util.List;

public interface IArgumentCompletionHandler
{
	List<String> handleCompletion(String val, String[] params);
	boolean isUsername();
}