package org.ultramine.server.util;

import gnu.trove.map.TCharCharMap;
import gnu.trove.map.hash.TCharCharHashMap;

public class TranslitTable
{
	private static final String en = "qwertyuiop[]asdfghjkl;'zxcvbnm,./";
	private static final String ru = "йцукенгшщзхъфывапролджэячсмитьбю.";
	
	private static final TCharCharMap en_ru = new TCharCharHashMap();
	private static final TCharCharMap ru_en = new TCharCharHashMap();
	
	static
	{
		for(int i = 0, s = en.length(); i < s; i++)
		{
			en_ru.put(en.charAt(i), ru.charAt(i));
			ru_en.put(ru.charAt(i), en.charAt(i));
		}
	}
	
	private static String translit(String src, TCharCharMap table)
	{
		StringBuilder dst = new StringBuilder(src.length());
		for(int i = 0, s = src.length(); i < s; i++)
		{
			char c1 = src.charAt(i);
			char c2 = table.get(c1);
			dst.append(c2 != '\00' ? c2 : c1);
		}
		
		return dst.toString();
	}
	
	public static String translitENRU(String src)
	{
		return translit(src, en_ru);
	}
	
	public static String translitRUEN(String src)
	{
		return translit(src, ru_en);
	}
}
