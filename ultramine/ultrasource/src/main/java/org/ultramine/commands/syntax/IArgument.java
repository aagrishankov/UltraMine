package org.ultramine.commands.syntax;

import java.util.List;

interface IArgument
{
	boolean isUsername();
	List<String> getCompletionOptions(String[] args);
	
	boolean hasValidation();
	boolean validate(String val);
}