package net.minecraft.util;

import net.openhft.koloboke.collect.map.LongObjMap;
import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;

public class LongHashMap
{
	private final LongObjMap<Object> map;
	private static final String __OBFID = "CL_00001492";

	public LongHashMap(LongObjMap<Object> map)
	{
		this.map = map;
	}

	public LongHashMap()
	{
		this(HashLongObjMaps.newMutableMap());
	}

	private static int getHashedKey(long p_76155_0_)
	{
		return hash((int)(p_76155_0_ ^ p_76155_0_ >>> 32));
	}

	private static int hash(int p_76157_0_)
	{
		p_76157_0_ ^= p_76157_0_ >>> 20 ^ p_76157_0_ >>> 12;
		return p_76157_0_ ^ p_76157_0_ >>> 7 ^ p_76157_0_ >>> 4;
	}

	private static int getHashIndex(int p_76158_0_, int p_76158_1_)
	{
		return p_76158_0_ & p_76158_1_ - 1;
	}

	public int getNumHashElements()
	{
		return map.size();
	}

	public Object getValueByKey(long p_76164_1_)
	{
		return map.get(p_76164_1_);
	}

	public boolean containsItem(long p_76161_1_)
	{
		return map.containsKey(p_76161_1_);
	}

	final LongHashMap.Entry getEntry(long p_76160_1_)
	{
		return null;
	}

	public void add(long p_76163_1_, Object p_76163_3_)
	{
		map.put(p_76163_1_, p_76163_3_);
	}

	private void resizeTable(int p_76153_1_)
	{

	}

	private void copyHashTableTo(LongHashMap.Entry[] p_76154_1_)
	{

	}

	public Object remove(long p_76159_1_)
	{
		return map.remove(p_76159_1_);
	}

	final LongHashMap.Entry removeKey(long p_76152_1_)
	{
		return null;
	}

	private void createKey(int p_76156_1_, long p_76156_2_, Object p_76156_4_, int p_76156_5_)
	{

	}

	static class Entry
		{
			final long key;
			Object value;
			LongHashMap.Entry nextEntry;
			final int hash;
			private static final String __OBFID = "CL_00001493";

			Entry(int p_i1553_1_, long p_i1553_2_, Object p_i1553_4_, LongHashMap.Entry p_i1553_5_)
			{
				this.value = p_i1553_4_;
				this.nextEntry = p_i1553_5_;
				this.key = p_i1553_2_;
				this.hash = p_i1553_1_;
			}

			public final long getKey()
			{
				return this.key;
			}

			public final Object getValue()
			{
				return this.value;
			}

			public final boolean equals(Object p_equals_1_)
			{
				if (!(p_equals_1_ instanceof LongHashMap.Entry))
				{
					return false;
				}
				else
				{
					LongHashMap.Entry entry = (LongHashMap.Entry)p_equals_1_;
					Long olong = Long.valueOf(this.getKey());
					Long olong1 = Long.valueOf(entry.getKey());

					if (olong == olong1 || olong != null && olong.equals(olong1))
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
				return LongHashMap.getHashedKey(this.key);
			}

			public final String toString()
			{
				return this.getKey() + "=" + this.getValue();
			}
		}
}