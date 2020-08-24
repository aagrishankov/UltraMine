package org.ultramine.server.util;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.time.Duration;

public class BasicTypeFormatter
{
	public static IChatComponent formatTime(Duration duration)
	{
		return formatTime(duration, false);
	}

	public static IChatComponent formatTime(Duration duration, boolean genitive)
	{
		return formatTime(duration, genitive, true);
	}

	public static IChatComponent formatTime(Duration duration, boolean genitive, boolean printSec)
	{
		return formatTime(duration.getSeconds()*1000, genitive, printSec);
	}

	public static IChatComponent formatTime(long timeMills)
	{
		return formatTime(timeMills, false);
	}
	
	public static IChatComponent formatTime(long timeMills, boolean genitive)
	{
		return formatTime(timeMills, genitive, true);
	}
	
	public static IChatComponent formatTime(long timeMills, boolean genitive, boolean printSec)
	{
		int seconds = (int) ((timeMills / 1000) % 60);
		int minutes = (int) ((timeMills / (60000)) % 60);
		int hours   = (int) ((timeMills / (3600000)) % 24);
		long days	= 		(timeMills / (86400000));
		
		String dayN;
		int daydd = (int)(days % 10);
		if(daydd == 1)
			dayN = "ultramine.time.day.1";
		else if(daydd > 1 && daydd < 5)
			dayN = "ultramine.time.day.2";
		else
			dayN = "ultramine.time.day.3";
		
		String hourN;
		int hourdd = hours % 10;
		if(hourdd == 1)
			hourN = "ultramine.time.hour.1";
		else if(hourdd > 1 && hourdd < 5)
			hourN = "ultramine.time.hour.2";
		else
			hourN = "ultramine.time.hour.3";
		
		String minN;
		int mindd = minutes % 10;
		if(mindd == 1)
			minN = genitive ? "ultramine.time.min.1.gen" : "ultramine.time.min.1";
		else if(mindd > 1 && mindd < 5)
			minN = "ultramine.time.min.2";
		else
			minN = "ultramine.time.min.3";
		
		String secN;
		int secdd = seconds % 10;
		if(secdd == 1)
			secN = genitive ? "ultramine.time.sec.1.gen" : "ultramine.time.sec.1";
		else if(secdd > 1 && secdd < 5)
			secN = "ultramine.time.sec.2";
		else
			secN = "ultramine.time.sec.3";

		Object comp1 = days > 0 ? new ChatComponentTranslation("%s %s ", days, new ChatComponentTranslation(dayN)) : "";
		Object comp2 = hours > 0 ? new ChatComponentTranslation("%s %s ", hours, new ChatComponentTranslation(hourN)) : "";
		Object comp3 = minutes > 0 ? new ChatComponentTranslation("%s %s ", minutes, new ChatComponentTranslation(minN)) : "";
		Object comp4 = (printSec && (seconds != 0 || minutes == 0 && hours == 0 && days == 0)) ?
				new ChatComponentTranslation("%s %s", seconds, new ChatComponentTranslation(secN)) : "";

		return new ChatComponentTranslation("%s%s%s%s", comp1, comp2, comp3, comp4);
	}
	
	public static IChatComponent formatMessage(EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		for(int i = 0; i < args.length; i++)
		{
			Object o = args[i];
			if(!(o instanceof IChatComponent))
				args[i] = new ChatComponentText(o.toString()).setChatStyle(new ChatStyle().setColor(argsColor));
		}
		
		ChatComponentTranslation comp = new ChatComponentTranslation(msg, args);
		comp.getChatStyle().setColor(tplColor);
		return comp;
	}
}
