package org.ultramine.server.util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.oredict.OreDictionary;

public class BasicTypeParser
{
	public static boolean isInt(String val)
	{
		int len = val.length();
		if(len > 11 || len == 0) return false;
		if(len > 9)
		{
			try
			{
				Integer.parseInt(val);
				return true;
			} catch(NumberFormatException e){return false;}
		}
		
		int i = 0;
		if(val.charAt(0) == '-')
		{
			i = 1;
			if(len == 1) return false;
		}
		
		for(; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
	
	public static boolean isUnsignedInt(String val)
	{
		int len = val.length();
		if(len > 10 || len == 0) return false;
		if(len == 10)
		{
			try
			{
				if(Integer.parseInt(val) >= 0) return true;
			} catch(NumberFormatException e){return false;}
		}
		
		for(int i = 0; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
	
	public static EnumDifficulty parseDifficulty(String str)
	{
		if(isUnsignedInt(str))
		{
			return EnumDifficulty.getDifficultyEnum(Math.min(Integer.parseInt(str), 3));
		}
		
		str = str.toLowerCase();
		
		if(str.equals("p") || str.equals("peaceful"))
		{
			return EnumDifficulty.PEACEFUL;
		}
		else if(str.equals("e") || str.equals("easy"))
		{
			return EnumDifficulty.EASY;
		}
		else if(str.equals("n") || str.equals("normal"))
		{
			return EnumDifficulty.NORMAL;
		}
		else if(str.equals("h") || str.equals("hard"))
		{
			return EnumDifficulty.HARD;
		}
		
		return null;
	}
	
	public static EnumChatFormatting parseColor(String str)
	{
		if(!str.isEmpty())
		{
			char c = str.charAt(0);
			return EnumChatFormatting.getByColorCode(str.length() == 1 ? c : c == '&' ? str.charAt(1) : c);
		}
		
		return null;
	}
	
	public static ItemStack parseItemStack(String str)
	{
		return parseItemStack(str, false);
	}
	
	public static ItemStack parseItemStack(String str, boolean allowNBT)
	{
		return parseItemStack(str, false, -1, allowNBT);
	}
	
	public static ItemStack parseItemStack(String str, boolean allowWildcardData, int size, boolean allowNBT)
	{
		int len = str.length();
		int ind3 = str.indexOf('{');
		int ind1 = str.indexOf(':');
		int ind4 = str.indexOf(':', ind1+1);
		int ind2 = str.indexOf('*');
		if(ind3 != -1 && ind1 > ind3) ind1 = -1;
		if(ind3 != -1 && ind2 > ind3) ind2 = -1;
		if(ind4 != -1 && (ind4 < ind3 || ind3 == -1)) ind1 = ind4;
		if(ind1+1 == ind2) {
			ind2 = str.indexOf('*', ind2+1);
			if(ind3 != -1 && ind2 > ind3) ind2 = -1;
		}
		boolean hasData = ind1 != -1;
		boolean hasSize = ind2 != -1;
		boolean hasNBT = ind3 != -1;
		int idEndInd = hasData ? ind1 : hasSize ? ind2 : hasNBT ? ind3 : len;
		int dataEndInd = hasSize ? ind2 : hasNBT ? ind3 : len;
		int sizeEndInd = hasNBT ? ind3 : len;
		int data = 0;
		NBTTagCompound nbt = null;
		int endInd;
		if(hasData)
		{
			String dataS = str.substring(ind1+1, dataEndInd).trim();
			if(dataS.equals("*") && allowWildcardData)
				data = OreDictionary.WILDCARD_VALUE;
			else if(isUnsignedInt(dataS))
				data = Integer.parseInt(dataS);
			else
				idEndInd = dataEndInd;
		}
		Item item = CommandBase.getItemByText(null, str.substring(0, idEndInd).trim());
		endInd = dataEndInd;
		if(hasSize && size == -1)
		{
			String sizeS = str.substring(endInd+1, endInd = sizeEndInd).trim();
			try
			{
				size = Integer.parseInt(sizeS);
			}
			catch(NumberFormatException e)
			{
				throw new CommandException("commands.generic.itemstack.size", sizeS);
			}
		}
		if(hasNBT && allowNBT)
		{
			try
			{
				NBTBase nbtbase = JsonToNBT.func_150315_a(str.substring(endInd, len).trim());
				if(nbtbase instanceof NBTTagCompound)
					nbt = (NBTTagCompound)nbtbase;
			}
			catch(NBTException e)
			{
				throw new CommandException("commands.setblock.tagError", e.getMessage());
			}
		}
		
		ItemStack is = new ItemStack(item, 1, data);
		if(nbt != null)
			is.setTagCompound(nbt);
		is.stackSize = size != -1 ? size : is.getMaxStackSize();
		
		return is;
	}
	
	public static ItemStack parseStackType(String str)
	{
		return parseItemStack(str, true, 1, true);
	}
	
	/**
	 * Examples - 10, 5s, 24h, 10d7h5m3s<br />
	 * s - second<br />
	 * m - minute<br />
	 * h - hour<br />
	 * d - day<br />
	 * @return time mills
	 */
	public static long parseTime(String str)
	{
		long time = 0;
		int lastInd = 0;
		for(int i = 0, s = str.length(); i < s; i++)
		{
			char c = str.charAt(i);
			if("smhd".indexOf(c) != -1)
			{
				long t;
				String s1 = str.substring(lastInd, i);
				try
				{
					t = Long.parseLong(s1);
				}
				catch(NumberFormatException e)
				{
					throw new CommandException("commands.generic.num.invalid", s1);
				}
				lastInd = i+1;
				int mod = c == 's' ? 1 : c == 'm' ? 60 : c == 'h' ? 60*60 : c == 'd' ? 60*60*24 : 1;
				
				time += t*mod;
			}
		}
		
		if(lastInd != str.length())
		{
			String s1 = str.substring(lastInd, str.length());
			try
			{
				time += Long.parseLong(s1);
			}
			catch(NumberFormatException e)
			{
				throw new CommandException("commands.generic.num.invalid", s1);
			}
		}
		
		return time*1000L;
	}
}
