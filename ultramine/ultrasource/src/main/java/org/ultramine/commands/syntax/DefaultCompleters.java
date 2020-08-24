package org.ultramine.commands.syntax;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import org.ultramine.server.util.BasicTypeParser;

import com.google.common.collect.Iterables;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;

import java.util.ArrayList;
import java.util.List;

public class DefaultCompleters
{
	@ArgumentCompleter(value = "player", isUsername = true)
	public static List<String> player(String val, String[] args)
	{
		return filterArray(val, MinecraftServer.getServer().getAllUsernames());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ArgumentCompleter("item")
	public static  List<String> item(String val, String[] args)
	{
		Iterable it = Iterables.concat(Item.itemRegistry.getKeys(), ((FMLControlledNamespacedRegistry)Item.itemRegistry).getAliases().keySet());
		List<String> ret = filterCollection(val, it);
		if(val.indexOf(':') == -1)
			ret.addAll(filterCollection("minecraft:".concat(val), it));
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ArgumentCompleter("block")
	public static List<String> block(String val, String[] args)
	{
		return filterCollection(val, Iterables.concat(Block.blockRegistry.getKeys(), ((FMLControlledNamespacedRegistry)Block.blockRegistry).getAliases().keySet()));
	}

	@SuppressWarnings({ "unchecked" })
	@ArgumentCompleter("entity")
	public static List<String> entity(String val, String[] args)
	{
		return filterCollection(val, EntityList.func_151515_b());
	}

	@ArgumentCompleter("list")
	public static List<String> list(String val, String[] args)
	{
		return filterArray(val, args);
	}
	
	@ArgumentCompleter("world")
	public static List<String> world(String val, String[] args)
	{
		return filterCollection(val, MinecraftServer.getServer().getMultiWorld().getAllNames());
	}
	
	@ArgumentCompleter("warp")
	public static List<String> warp(String val, String[] args)
	{
		return filterCollection(val, MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarps().keySet());
	}

	@ArgumentValidator("int")
	public static boolean int_validator(String val, String[] args)
	{
		return BasicTypeParser.isInt(val);
	}
	
	@ArgumentValidator("world")
	public static boolean world_validator(String val, String[] args)
	{
		return BasicTypeParser.isInt(val) || MinecraftServer.getServer().getMultiWorld().getAllNames().contains(val);
	}

	public static List<String> filterArray(String filter, String[] strings)
	{
		List<String> result = new ArrayList<String>();

		for (String str : strings)
		{
			if (CommandBase.doesStringStartWith(filter, str))
				result.add(str);
		}

		return result;
	}

	public static List<String> filterCollection(String filter, Iterable<String> iterable)
	{
		List<String> result = new ArrayList<String>();

		for (String str : iterable)
		{
			if (CommandBase.doesStringStartWith(filter, str))
				result.add(str);
		}

		return result;
	}
}
