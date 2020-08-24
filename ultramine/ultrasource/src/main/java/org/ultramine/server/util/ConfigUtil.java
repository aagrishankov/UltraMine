package org.ultramine.server.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigUtil
{
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T deepClone(T obj)
	{
		if(obj == null)
			return null;
		Class<?> cls = obj.getClass();
		if(cls.isPrimitive() || cls == String.class || cls == Boolean.class || obj instanceof Number || obj instanceof Enum)
			return obj;
		else if(obj instanceof List)
		{
			List orig = (List)obj;
			List ret = new ArrayList(orig.size());
			for(Object o : orig)
				ret.add(deepClone(o));
			return (T)ret;
		}
		else if(obj instanceof Set)
		{
			Set orig = (Set)obj;
			Set ret = new HashSet();
			for(Object o : orig)
				ret.add(deepClone(o));
			return (T)ret;
		}
		else if(obj instanceof Map)
		{
			Map<?, ?> orig = (Map)obj;
			Map ret = new HashMap();
			for(Map.Entry ent : orig.entrySet())
				ret.put(deepClone(ent.getKey()), deepClone(ent.getValue()));
			return (T)ret;
		}
		else if(cls.isArray())
		{
			int len = Array.getLength(obj);
			Object ret = Array.newInstance(cls.getComponentType(), len);
			for(int i = 0; i < len; i++)
				Array.set(ret, i, deepClone(Array.get(obj, i)));
			return (T)ret;
		}
		else
		{
			try
			{
				Object ret = cls.newInstance();
				for(Field f : cls.getDeclaredFields())
				{
					f.setAccessible(true);
					f.set(ret, deepClone(f.get(obj)));
				}
				return (T)ret;
			}
			catch (Exception e)
			{
				throw new RuntimeException("Failed to clone object: "+obj, e);
			}
		}
	}
}
