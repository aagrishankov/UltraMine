package org.ultramine.server;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

import java.io.File;
import java.util.Map;

import net.minecraft.launchwrapper.LaunchClassLoader;

@SortingIndex(Integer.MAX_VALUE) //UMTransformerCollection must be always the last
public class UltraminePlugin implements IFMLLoadingPlugin
{
	public static File location;
	public static boolean isObfEnv;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{
				"org.ultramine.server.asm.transformers.UMTransformerCollection", //must be always the last
		};
	}

	@Override
	public String getModContainerClass()
	{
		return "org.ultramine.server.UltramineServerModContainer";
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		location = (File)data.get("coremodLocation");
		isObfEnv = (Boolean)data.get("runtimeDeobfuscationEnabled");
		LaunchClassLoader cl = (LaunchClassLoader)this.getClass().getClassLoader();
		cl.addClassLoaderExclusion("org.ultramine.server.bootstrap.");
		cl.addTransformerExclusion("org.ultramine.server.asm.");
		
		cl.addClassLoaderExclusion("jline.");
		cl.addClassLoaderExclusion("org.fusesource.jansi.");
		cl.addTransformerExclusion("org.yaml.snakeyaml.");
		cl.addTransformerExclusion("com.lmax.disruptor.");
		cl.addTransformerExclusion("org.apache.commons.dbcp2.");
		cl.addTransformerExclusion("org.apache.commons.pool2.");
		cl.addTransformerExclusion("org.apache.commons.logging.");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
