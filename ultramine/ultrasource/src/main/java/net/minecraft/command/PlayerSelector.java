package net.minecraft.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerSelector
{
	private static final Pattern tokenPattern = Pattern.compile("^@([parf])(?:\\[([\\w=,!-]*)\\])?$");
	private static final Pattern intListPattern = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
	private static final Pattern keyValueListPattern = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)");
	private static final String __OBFID = "CL_00000086";

	public static EntityPlayerMP matchOnePlayer(ICommandSender p_82386_0_, String p_82386_1_)
	{
		EntityPlayerMP[] aentityplayermp = matchPlayers(p_82386_0_, p_82386_1_);
		return aentityplayermp != null && aentityplayermp.length == 1 ? aentityplayermp[0] : null;
	}

	public static IChatComponent func_150869_b(ICommandSender p_150869_0_, String p_150869_1_)
	{
		EntityPlayerMP[] aentityplayermp = matchPlayers(p_150869_0_, p_150869_1_);

		if (aentityplayermp != null && aentityplayermp.length != 0)
		{
			IChatComponent[] aichatcomponent = new IChatComponent[aentityplayermp.length];

			for (int i = 0; i < aichatcomponent.length; ++i)
			{
				aichatcomponent[i] = aentityplayermp[i].func_145748_c_();
			}

			return CommandBase.joinNiceString(aichatcomponent);
		}
		else
		{
			return null;
		}
	}

	public static EntityPlayerMP[] matchPlayers(ICommandSender p_82380_0_, String p_82380_1_)
	{
		Matcher matcher = tokenPattern.matcher(p_82380_1_);

		if (matcher.matches())
		{
			Map map = getArgumentMap(matcher.group(2));
			String s1 = matcher.group(1);
			int i = getDefaultMinimumRange(s1);
			int j = getDefaultMaximumRange(s1);
			int k = getDefaultMinimumLevel(s1);
			int l = getDefaultMaximumLevel(s1);
			int i1 = getDefaultCount(s1);
			int j1 = WorldSettings.GameType.NOT_SET.getID();
			ChunkCoordinates chunkcoordinates = p_82380_0_.getPlayerCoordinates();
			Map map1 = func_96560_a(map);
			String s2 = null;
			String s3 = null;
			boolean flag = false;

			if (map.containsKey("rm"))
			{
				i = MathHelper.parseIntWithDefault((String)map.get("rm"), i);
				flag = true;
			}

			if (map.containsKey("r"))
			{
				j = MathHelper.parseIntWithDefault((String)map.get("r"), j);
				flag = true;
			}

			if (map.containsKey("lm"))
			{
				k = MathHelper.parseIntWithDefault((String)map.get("lm"), k);
			}

			if (map.containsKey("l"))
			{
				l = MathHelper.parseIntWithDefault((String)map.get("l"), l);
			}

			if (map.containsKey("x"))
			{
				chunkcoordinates.posX = MathHelper.parseIntWithDefault((String)map.get("x"), chunkcoordinates.posX);
				flag = true;
			}

			if (map.containsKey("y"))
			{
				chunkcoordinates.posY = MathHelper.parseIntWithDefault((String)map.get("y"), chunkcoordinates.posY);
				flag = true;
			}

			if (map.containsKey("z"))
			{
				chunkcoordinates.posZ = MathHelper.parseIntWithDefault((String)map.get("z"), chunkcoordinates.posZ);
				flag = true;
			}

			if (map.containsKey("m"))
			{
				j1 = MathHelper.parseIntWithDefault((String)map.get("m"), j1);
			}

			if (map.containsKey("c"))
			{
				i1 = MathHelper.parseIntWithDefault((String)map.get("c"), i1);
			}

			if (map.containsKey("team"))
			{
				s3 = (String)map.get("team");
			}

			if (map.containsKey("name"))
			{
				s2 = (String)map.get("name");
			}

			World world = flag ? p_82380_0_.getEntityWorld() : null;
			List list;

			if (!s1.equals("p") && !s1.equals("a"))
			{
				if (s1.equals("r"))
				{
					list = MinecraftServer.getServer().getConfigurationManager().findPlayers(chunkcoordinates, i, j, 0, j1, k, l, map1, s2, s3, world);
					Collections.shuffle(list);
					list = list.subList(0, Math.min(i1, list.size()));
					return list.isEmpty() ? new EntityPlayerMP[0] : (EntityPlayerMP[])list.toArray(new EntityPlayerMP[list.size()]);
				}
				else
				{
					return null;
				}
			}
			else
			{
				list = MinecraftServer.getServer().getConfigurationManager().findPlayers(chunkcoordinates, i, j, i1, j1, k, l, map1, s2, s3, world);
				return list.isEmpty() ? new EntityPlayerMP[0] : (EntityPlayerMP[])list.toArray(new EntityPlayerMP[list.size()]);
			}
		}
		else
		{
			return null;
		}
	}

	public static Map func_96560_a(Map p_96560_0_)
	{
		HashMap hashmap = new HashMap();
		Iterator iterator = p_96560_0_.keySet().iterator();

		while (iterator.hasNext())
		{
			String s = (String)iterator.next();

			if (s.startsWith("score_") && s.length() > "score_".length())
			{
				String s1 = s.substring("score_".length());
				hashmap.put(s1, Integer.valueOf(MathHelper.parseIntWithDefault((String)p_96560_0_.get(s), 1)));
			}
		}

		return hashmap;
	}

	public static boolean matchesMultiplePlayers(String p_82377_0_)
	{
		Matcher matcher = tokenPattern.matcher(p_82377_0_);

		if (matcher.matches())
		{
			Map map = getArgumentMap(matcher.group(2));
			String s1 = matcher.group(1);
			int i = getDefaultCount(s1);

			if (map.containsKey("c"))
			{
				i = MathHelper.parseIntWithDefault((String)map.get("c"), i);
			}

			return i != 1;
		}
		else
		{
			return false;
		}
	}

	public static boolean hasTheseArguments(String p_82383_0_, String p_82383_1_)
	{
		Matcher matcher = tokenPattern.matcher(p_82383_0_);

		if (matcher.matches())
		{
			String s2 = matcher.group(1);
			return p_82383_1_ == null || p_82383_1_.equals(s2);
		}
		else
		{
			return false;
		}
	}

	public static boolean hasArguments(String p_82378_0_)
	{
		return hasTheseArguments(p_82378_0_, (String)null);
	}

	private static final int getDefaultMinimumRange(String p_82384_0_)
	{
		return 0;
	}

	private static final int getDefaultMaximumRange(String p_82379_0_)
	{
		return 0;
	}

	private static final int getDefaultMaximumLevel(String p_82376_0_)
	{
		return Integer.MAX_VALUE;
	}

	private static final int getDefaultMinimumLevel(String p_82375_0_)
	{
		return 0;
	}

	private static final int getDefaultCount(String p_82382_0_)
	{
		return p_82382_0_.equals("a") ? 0 : 1;
	}

	private static Map getArgumentMap(String p_82381_0_)
	{
		HashMap hashmap = new HashMap();

		if (p_82381_0_ == null)
		{
			return hashmap;
		}
		else
		{
			Matcher matcher = intListPattern.matcher(p_82381_0_);
			int i = 0;
			int j;

			for (j = -1; matcher.find(); j = matcher.end())
			{
				String s1 = null;

				switch (i++)
				{
					case 0:
						s1 = "x";
						break;
					case 1:
						s1 = "y";
						break;
					case 2:
						s1 = "z";
						break;
					case 3:
						s1 = "r";
				}

				if (s1 != null && matcher.group(1).length() > 0)
				{
					hashmap.put(s1, matcher.group(1));
				}
			}

			if (j < p_82381_0_.length())
			{
				matcher = keyValueListPattern.matcher(j == -1 ? p_82381_0_ : p_82381_0_.substring(j));

				while (matcher.find())
				{
					hashmap.put(matcher.group(1), matcher.group(2));
				}
			}

			return hashmap;
		}
	}
}