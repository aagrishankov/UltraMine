package net.minecraft.profiler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.stack.TLongStack;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler
{
	private static final Logger logger = LogManager.getLogger();
	private final List sectionList = new ArrayList();
	private final TLongStack timestampList = new TLongArrayStack();
	public boolean profilingEnabled;
	private String profilingSection = "";
	private final TObjectLongMap<String> profilingMap = new TObjectLongHashMap<String>();
	private static final String __OBFID = "CL_00001497";

	public void clearProfiling()
	{
		this.profilingMap.clear();
		this.profilingSection = "";
		this.sectionList.clear();
	}

	public void startSection(String p_76320_1_)
	{
		if (this.profilingEnabled)
		{
			if (this.profilingSection.length() > 0)
			{
				this.profilingSection = this.profilingSection + ".";
			}

			this.profilingSection = this.profilingSection + p_76320_1_;
			this.sectionList.add(this.profilingSection);
			this.timestampList.push(System.nanoTime());
		}
	}

	public void endSection()
	{
		if (this.profilingEnabled)
		{
			long i = System.nanoTime();
			long j = this.timestampList.pop();
			this.sectionList.remove(this.sectionList.size() - 1);
			long k = i - j;

			if (this.profilingMap.containsKey(this.profilingSection))
			{
				this.profilingMap.put(this.profilingSection, this.profilingMap.get(profilingSection) + k);
			}
			else
			{
				this.profilingMap.put(this.profilingSection, k);
			}

			if (k > 50000000L)
			{
				logger.warn("Something\'s taking too long! \'" + this.profilingSection + "\' took aprox " + (double)k / 1000000.0D + " ms");
			}

			this.profilingSection = !this.sectionList.isEmpty() ? (String)this.sectionList.get(this.sectionList.size() - 1) : "";
		}
	}

	public List getProfilingData(String p_76321_1_)
	{
		if (!this.profilingEnabled)
		{
			return null;
		}
		else
		{
			long i = this.profilingMap.containsKey("root") ? this.profilingMap.get("root") : 0L;
			long j = this.profilingMap.containsKey(p_76321_1_) ? this.profilingMap.get(p_76321_1_) : -1L;
			ArrayList arraylist = new ArrayList();

			if (p_76321_1_.length() > 0)
			{
				p_76321_1_ = p_76321_1_ + ".";
			}

			long k = 0L;
			Iterator iterator = this.profilingMap.keySet().iterator();

			while (iterator.hasNext())
			{
				String s1 = (String)iterator.next();

				if (s1.length() > p_76321_1_.length() && s1.startsWith(p_76321_1_) && s1.indexOf(".", p_76321_1_.length() + 1) < 0)
				{
					k += this.profilingMap.get(s1);
				}
			}

			float f = (float)k;

			if (k < j)
			{
				k = j;
			}

			if (i < k)
			{
				i = k;
			}

			Iterator iterator1 = this.profilingMap.keySet().iterator();
			String s2;

			while (iterator1.hasNext())
			{
				s2 = (String)iterator1.next();

				if (s2.length() > p_76321_1_.length() && s2.startsWith(p_76321_1_) && s2.indexOf(".", p_76321_1_.length() + 1) < 0)
				{
					long l = this.profilingMap.get(s2);
					double d0 = (double)l * 100.0D / (double)k;
					double d1 = (double)l * 100.0D / (double)i;
					String s3 = s2.substring(p_76321_1_.length());
					arraylist.add(new Profiler.Result(s3, d0, d1));
				}
			}

			iterator1 = this.profilingMap.keySet().iterator();

			while (iterator1.hasNext())
			{
				s2 = (String)iterator1.next();
				this.profilingMap.put(s2, this.profilingMap.get(s2) * 999L / 1000L);
			}

			if ((float)k > f)
			{
				arraylist.add(new Profiler.Result("unspecified", (double)((float)k - f) * 100.0D / (double)k, (double)((float)k - f) * 100.0D / (double)i));
			}

			Collections.sort(arraylist);
			arraylist.add(0, new Profiler.Result(p_76321_1_, 100.0D, (double)k * 100.0D / (double)i));
			return arraylist;
		}
	}

	public void endStartSection(String p_76318_1_)
	{
		this.endSection();
		this.startSection(p_76318_1_);
	}

	public String getNameOfLastSection()
	{
		return this.sectionList.size() == 0 ? "[UNKNOWN]" : (String)this.sectionList.get(this.sectionList.size() - 1);
	}

	public static final class Result implements Comparable
		{
			public double field_76332_a;
			public double field_76330_b;
			public String field_76331_c;
			private static final String __OBFID = "CL_00001498";

			public Result(String p_i1554_1_, double p_i1554_2_, double p_i1554_4_)
			{
				this.field_76331_c = p_i1554_1_;
				this.field_76332_a = p_i1554_2_;
				this.field_76330_b = p_i1554_4_;
			}

			public int compareTo(Profiler.Result p_compareTo_1_)
			{
				return p_compareTo_1_.field_76332_a < this.field_76332_a ? -1 : (p_compareTo_1_.field_76332_a > this.field_76332_a ? 1 : p_compareTo_1_.field_76331_c.compareTo(this.field_76331_c));
			}

			@SideOnly(Side.CLIENT)
			public int func_76329_a()
			{
				return (this.field_76331_c.hashCode() & 11184810) + 4473924;
			}

			public int compareTo(Object p_compareTo_1_)
			{
				return this.compareTo((Profiler.Result)p_compareTo_1_);
			}
		}
}