package net.minecraft.command;

import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ultramine.server.WorldConstants;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;

public abstract class CommandBase implements ICommand
{
	private static IAdminCommand theAdmin;
	private static final String __OBFID = "CL_00001739";

	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	public List getCommandAliases()
	{
		return null;
	}

	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
	{
		return p_71519_1_.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
	}

	public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
	{
		return null;
	}

	public static int parseInt(ICommandSender p_71526_0_, String p_71526_1_)
	{
		try
		{
			return Integer.parseInt(p_71526_1_);
		}
		catch (NumberFormatException numberformatexception)
		{
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {p_71526_1_});
		}
	}

	public static int parseIntWithMin(ICommandSender p_71528_0_, String p_71528_1_, int p_71528_2_)
	{
		return parseIntBounded(p_71528_0_, p_71528_1_, p_71528_2_, Integer.MAX_VALUE);
	}

	public static int parseIntBounded(ICommandSender p_71532_0_, String p_71532_1_, int p_71532_2_, int p_71532_3_)
	{
		int k = parseInt(p_71532_0_, p_71532_1_);

		if (k < p_71532_2_)
		{
			throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[] {Integer.valueOf(k), Integer.valueOf(p_71532_2_)});
		}
		else if (k > p_71532_3_)
		{
			throw new NumberInvalidException("commands.generic.num.tooBig", new Object[] {Integer.valueOf(k), Integer.valueOf(p_71532_3_)});
		}
		else
		{
			return k;
		}
	}

	public static double parseDouble(ICommandSender p_82363_0_, String p_82363_1_)
	{
		try
		{
			double d0 = Double.parseDouble(p_82363_1_);

			if (!Doubles.isFinite(d0))
			{
				throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {p_82363_1_});
			}
			else
			{
				return d0;
			}
		}
		catch (NumberFormatException numberformatexception)
		{
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {p_82363_1_});
		}
	}

	public static double parseDoubleWithMin(ICommandSender p_110664_0_, String p_110664_1_, double p_110664_2_)
	{
		return parseDoubleBounded(p_110664_0_, p_110664_1_, p_110664_2_, Double.MAX_VALUE);
	}

	public static double parseDoubleBounded(ICommandSender p_110661_0_, String p_110661_1_, double p_110661_2_, double p_110661_4_)
	{
		double d2 = parseDouble(p_110661_0_, p_110661_1_);

		if (d2 < p_110661_2_)
		{
			throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] {Double.valueOf(d2), Double.valueOf(p_110661_2_)});
		}
		else if (d2 > p_110661_4_)
		{
			throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] {Double.valueOf(d2), Double.valueOf(p_110661_4_)});
		}
		else
		{
			return d2;
		}
	}

	public static boolean parseBoolean(ICommandSender p_110662_0_, String p_110662_1_)
	{
		if (!p_110662_1_.equals("true") && !p_110662_1_.equals("1"))
		{
			if (!p_110662_1_.equals("false") && !p_110662_1_.equals("0"))
			{
				throw new CommandException("commands.generic.boolean.invalid", new Object[] {p_110662_1_});
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	public static EntityPlayerMP getCommandSenderAsPlayer(ICommandSender p_71521_0_)
	{
		if (p_71521_0_ instanceof EntityPlayerMP)
		{
			return (EntityPlayerMP)p_71521_0_;
		}
		else
		{
			throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
		}
	}

	public static EntityPlayerMP getPlayer(ICommandSender p_82359_0_, String p_82359_1_)
	{
		EntityPlayerMP entityplayermp = PlayerSelector.matchOnePlayer(p_82359_0_, p_82359_1_);

		if (entityplayermp != null)
		{
			return entityplayermp;
		}
		else
		{
			entityplayermp = MinecraftServer.getServer().getConfigurationManager().func_152612_a(p_82359_1_);

			if (entityplayermp == null)
			{
				throw new PlayerNotFoundException();
			}
			else
			{
				return entityplayermp;
			}
		}
	}

	public static String func_96332_d(ICommandSender p_96332_0_, String p_96332_1_)
	{
		EntityPlayerMP entityplayermp = PlayerSelector.matchOnePlayer(p_96332_0_, p_96332_1_);

		if (entityplayermp != null)
		{
			return entityplayermp.getCommandSenderName();
		}
		else if (PlayerSelector.hasArguments(p_96332_1_))
		{
			throw new PlayerNotFoundException();
		}
		else
		{
			return p_96332_1_;
		}
	}

	public static IChatComponent func_147178_a(ICommandSender p_147178_0_, String[] p_147178_1_, int p_147178_2_)
	{
		return func_147176_a(p_147178_0_, p_147178_1_, p_147178_2_, false);
	}

	public static IChatComponent func_147176_a(ICommandSender p_147176_0_, String[] p_147176_1_, int p_147176_2_, boolean p_147176_3_)
	{
		ChatComponentText chatcomponenttext = new ChatComponentText("");

		for (int j = p_147176_2_; j < p_147176_1_.length; ++j)
		{
			if (j > p_147176_2_)
			{
				chatcomponenttext.appendText(" ");
			}

			Object object = ForgeHooks.newChatWithLinks(p_147176_1_[j]);

			if (p_147176_3_)
			{
				IChatComponent ichatcomponent = PlayerSelector.func_150869_b(p_147176_0_, p_147176_1_[j]);

				if (ichatcomponent != null)
				{
					object = ichatcomponent;
				}
				else if (PlayerSelector.hasArguments(p_147176_1_[j]))
				{
					throw new PlayerNotFoundException();
				}
			}

			chatcomponenttext.appendSibling((IChatComponent)object);
		}

		return chatcomponenttext;
	}

	public static String func_82360_a(ICommandSender p_82360_0_, String[] p_82360_1_, int p_82360_2_)
	{
		StringBuilder stringbuilder = new StringBuilder();

		for (int j = p_82360_2_; j < p_82360_1_.length; ++j)
		{
			if (j > p_82360_2_)
			{
				stringbuilder.append(" ");
			}

			String s = p_82360_1_[j];
			stringbuilder.append(s);
		}

		return stringbuilder.toString();
	}

	public static double func_110666_a(ICommandSender p_110666_0_, double p_110666_1_, String p_110666_3_)
	{
		return func_110665_a(p_110666_0_, p_110666_1_, p_110666_3_, -WorldConstants.MAX_BLOCK_COORD, WorldConstants.MAX_BLOCK_COORD);
	}

	public static double func_110665_a(ICommandSender p_110665_0_, double p_110665_1_, String p_110665_3_, int p_110665_4_, int p_110665_5_)
	{
		boolean flag = p_110665_3_.startsWith("~");

		if (flag && Double.isNaN(p_110665_1_))
		{
			throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {Double.valueOf(p_110665_1_)});
		}
		else
		{
			double d1 = flag ? p_110665_1_ : 0.0D;

			if (!flag || p_110665_3_.length() > 1)
			{
				boolean flag1 = p_110665_3_.contains(".");

				if (flag)
				{
					p_110665_3_ = p_110665_3_.substring(1);
				}

				d1 += parseDouble(p_110665_0_, p_110665_3_);

				if (!flag1 && !flag)
				{
					d1 += 0.5D;
				}
			}

			if (p_110665_4_ != 0 || p_110665_5_ != 0)
			{
				if (d1 < (double)p_110665_4_)
				{
					throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] {Double.valueOf(d1), Integer.valueOf(p_110665_4_)});
				}

				if (d1 > (double)p_110665_5_)
				{
					throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] {Double.valueOf(d1), Integer.valueOf(p_110665_5_)});
				}
			}

			return d1;
		}
	}

	public static Item getItemByText(ICommandSender p_147179_0_, String p_147179_1_)
	{
		Item item = (Item)Item.itemRegistry.getObject(p_147179_1_);

		if (item == null)
		{
			try
			{
				Item item1 = Item.getItemById(Integer.parseInt(p_147179_1_));

//				if (item1 != null)
//				{
//					ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.generic.deprecatedId", new Object[] {Item.itemRegistry.getNameForObject(item1)});
//					chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.GRAY);
//					p_147179_0_.addChatMessage(chatcomponenttranslation);
//				}

				item = item1;
			}
			catch (NumberFormatException numberformatexception)
			{
				;
			}
		}

		if (item == null)
		{
			throw new NumberInvalidException("commands.give.notFound", new Object[] {p_147179_1_});
		}
		else
		{
			return item;
		}
	}

	public static Block getBlockByText(ICommandSender p_147180_0_, String p_147180_1_)
	{
		if (Block.blockRegistry.containsKey(p_147180_1_))
		{
			return (Block)Block.blockRegistry.getObject(p_147180_1_);
		}
		else
		{
			try
			{
				int i = Integer.parseInt(p_147180_1_);

				if (Block.blockRegistry.containsId(i))
				{
					Block block = Block.getBlockById(i);
//					ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.generic.deprecatedId", new Object[] {Block.blockRegistry.getNameForObject(block)});
//					chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.GRAY);
//					p_147180_0_.addChatMessage(chatcomponenttranslation);
					return block;
				}
			}
			catch (NumberFormatException numberformatexception)
			{
				;
			}

			throw new NumberInvalidException("commands.give.notFound", new Object[] {p_147180_1_});
		}
	}

	public static String joinNiceString(Object[] p_71527_0_)
	{
		StringBuilder stringbuilder = new StringBuilder();

		for (int i = 0; i < p_71527_0_.length; ++i)
		{
			String s = p_71527_0_[i].toString();

			if (i > 0)
			{
				if (i == p_71527_0_.length - 1)
				{
					stringbuilder.append(" and ");
				}
				else
				{
					stringbuilder.append(", ");
				}
			}

			stringbuilder.append(s);
		}

		return stringbuilder.toString();
	}

	public static IChatComponent joinNiceString(IChatComponent[] p_147177_0_)
	{
		ChatComponentText chatcomponenttext = new ChatComponentText("");

		for (int i = 0; i < p_147177_0_.length; ++i)
		{
			if (i > 0)
			{
				if (i == p_147177_0_.length - 1)
				{
					chatcomponenttext.appendText(" and ");
				}
				else if (i > 0)
				{
					chatcomponenttext.appendText(", ");
				}
			}

			chatcomponenttext.appendSibling(p_147177_0_[i]);
		}

		return chatcomponenttext;
	}

	public static String joinNiceStringFromCollection(Collection p_96333_0_)
	{
		return joinNiceString(p_96333_0_.toArray(new String[p_96333_0_.size()]));
	}

	public static boolean doesStringStartWith(String p_71523_0_, String p_71523_1_)
	{
		return p_71523_1_.regionMatches(true, 0, p_71523_0_, 0, p_71523_0_.length());
	}

	public static List getListOfStringsMatchingLastWord(String[] p_71530_0_, String ... p_71530_1_)
	{
		String s1 = p_71530_0_[p_71530_0_.length - 1];
		ArrayList arraylist = new ArrayList();
		String[] astring1 = p_71530_1_;
		int i = p_71530_1_.length;

		for (int j = 0; j < i; ++j)
		{
			String s2 = astring1[j];

			if (doesStringStartWith(s1, s2))
			{
				arraylist.add(s2);
			}
		}

		return arraylist;
	}

	public static List getListOfStringsFromIterableMatchingLastWord(String[] p_71531_0_, Iterable p_71531_1_)
	{
		String s = p_71531_0_[p_71531_0_.length - 1];
		ArrayList arraylist = new ArrayList();
		Iterator iterator = p_71531_1_.iterator();

		while (iterator.hasNext())
		{
			String s1 = (String)iterator.next();

			if (doesStringStartWith(s, s1))
			{
				arraylist.add(s1);
			}
		}

		return arraylist;
	}

	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
	{
		return false;
	}

	public static void func_152373_a(ICommandSender p_152373_0_, ICommand p_152373_1_, String p_152373_2_, Object ... p_152373_3_)
	{
		func_152374_a(p_152373_0_, p_152373_1_, 0, p_152373_2_, p_152373_3_);
	}

	public static void func_152374_a(ICommandSender p_152374_0_, ICommand p_152374_1_, int p_152374_2_, String p_152374_3_, Object ... p_152374_4_)
	{
		if (theAdmin != null)
		{
			theAdmin.func_152372_a(p_152374_0_, p_152374_1_, p_152374_2_, p_152374_3_, p_152374_4_);
		}
	}

	public static void setAdminCommander(IAdminCommand p_71529_0_)
	{
		theAdmin = p_71529_0_;
	}

	public int compareTo(ICommand p_compareTo_1_)
	{
		return this.getCommandName().compareTo(p_compareTo_1_.getCommandName());
	}

	public int compareTo(Object p_compareTo_1_)
	{
		return this.compareTo((ICommand)p_compareTo_1_);
	}
}