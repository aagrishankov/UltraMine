package net.minecraft.util;

import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import java.util.HashSet;
import java.util.Set;

public class IntHashMap
{
	private final IntObjMap<Object> map;
	private static final String __OBFID = "CL_00001490";

	public IntHashMap(IntObjMap<Object> map)
	{
		this.map = map;
	}

	public IntHashMap()
	{
		this(HashIntObjMaps.newMutableMap());
	}

	private static int computeHash(int p_76044_0_)
	{
		p_76044_0_ ^= p_76044_0_ >>> 20 ^ p_76044_0_ >>> 12;
		return p_76044_0_ ^ p_76044_0_ >>> 7 ^ p_76044_0_ >>> 4;
	}

	private static int getSlotIndex(int p_76043_0_, int p_76043_1_)
	{
		return p_76043_0_ & p_76043_1_ - 1;
	}

	public Object lookup(int p_76041_1_)
	{
		return map.get(p_76041_1_);
	}

	public boolean containsItem(int p_76037_1_)
	{
		return map.containsKey(p_76037_1_);
	}

	final IntHashMap.Entry lookupEntry(int p_76045_1_)
	{
		return null;
	}

	public void addKey(int p_76038_1_, Object p_76038_2_)
	{
		map.put(p_76038_1_, p_76038_2_);
	}

	private void grow(int p_76047_1_)
	{

	}

	private void copyTo(IntHashMap.Entry[] p_76048_1_)
	{

	}

	public Object removeObject(int p_76049_1_)
	{
		return map.remove(p_76049_1_);
	}

	final IntHashMap.Entry removeEntry(int p_76036_1_)
	{
		return null;
	}

	public void clearMap()
	{
		map.clear();
	}

	private void insert(int p_76040_1_, int p_76040_2_, Object p_76040_3_, int p_76040_4_)
	{

	}

	static class Entry
		{
			final int hashEntry;
			Object valueEntry;
			IntHashMap.Entry nextEntry;
			final int slotHash;
			private static final String __OBFID = "CL_00001491";

			Entry(int p_i1552_1_, int p_i1552_2_, Object p_i1552_3_, IntHashMap.Entry p_i1552_4_)
			{
				this.valueEntry = p_i1552_3_;
				this.nextEntry = p_i1552_4_;
				this.hashEntry = p_i1552_2_;
				this.slotHash = p_i1552_1_;
			}

			public final int getHash()
			{
				return this.hashEntry;
			}

			public final Object getValue()
			{
				return this.valueEntry;
			}

			public final boolean equals(Object p_equals_1_)
			{
				if (!(p_equals_1_ instanceof IntHashMap.Entry))
				{
					return false;
				}
				else
				{
					IntHashMap.Entry entry = (IntHashMap.Entry)p_equals_1_;
					Integer integer = Integer.valueOf(this.getHash());
					Integer integer1 = Integer.valueOf(entry.getHash());

					if (integer == integer1 || integer != null && integer.equals(integer1))
					{
						Object object1 = this.getValue();
						Object object2 = entry.getValue();

						if (object1 == object2 || object1 != null && object1.equals(object2))
						{
							return true;
						}
					}

					return false;
				}
			}

			public final int hashCode()
			{
				return IntHashMap.computeHash(this.hashEntry);
			}

			public final String toString()
			{
				return this.getHash() + "=" + this.getValue();
			}
		}
}