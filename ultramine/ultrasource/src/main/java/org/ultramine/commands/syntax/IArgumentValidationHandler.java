package org.ultramine.commands.syntax;

public interface IArgumentValidationHandler
{
	boolean handleValidation(String val, String[] params);
}