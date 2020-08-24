package net.minecraft.util;

public class ChatAllowedCharacters
{
	public static final char[] allowedCharacters = new char[] {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
	private static final String __OBFID = "CL_00001606";

	public static boolean isAllowedCharacter(char p_71566_0_)
	{
		return p_71566_0_ != 167 && p_71566_0_ >= 32 && p_71566_0_ != 127;
	}

	public static String filerAllowedCharacters(String p_71565_0_)
	{
		StringBuilder stringbuilder = new StringBuilder();
		char[] achar = p_71565_0_.toCharArray();
		int i = achar.length;

		for (int j = 0; j < i; ++j)
		{
			char c0 = achar[j];

			if (isAllowedCharacter(c0))
			{
				stringbuilder.append(c0);
			}
		}

		return stringbuilder.toString();
	}
}