package org.ultramine.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
	public String name();
	public String group();
	public String[] syntax() default {};
	public String[] aliases() default {};
	public String[] permissions() default {};
	public boolean isUsableFromServer() default true;
}