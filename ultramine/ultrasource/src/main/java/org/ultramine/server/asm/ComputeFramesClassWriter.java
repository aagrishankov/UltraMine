package org.ultramine.server.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.ultramine.server.UltraminePlugin;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.patcher.ClassPatchManager;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class ComputeFramesClassWriter extends ClassWriter
{
	private static final LaunchClassLoader CLASSLOADER = (LaunchClassLoader)ComputeFramesClassWriter.class.getClassLoader();
	private static final String JAVA_LANG_OBJECT = "java/lang/Object";
	
	public ComputeFramesClassWriter()
	{
		super(ClassWriter.COMPUTE_FRAMES);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2)
	{
		if(type1.equals(type2))
			return type1;
		if(type1.equals(JAVA_LANG_OBJECT) || type2.equals(JAVA_LANG_OBJECT))
			return JAVA_LANG_OBJECT;
		
		ClassReader node1 = getClassNode(type1);
		ClassReader node2 = getClassNode(type2);
		
		if((node1 != null && (node1.getAccess() & Opcodes.ACC_INTERFACE) != 0) || (node2 != null && (node2.getAccess() & Opcodes.ACC_INTERFACE) != 0))
			return JAVA_LANG_OBJECT;
		
		List<String> sup1 = getSuperTypesStack(type1, node1);
		if(sup1 == null)
			return JAVA_LANG_OBJECT;
		List<String> sup2 = getSuperTypesStack(type2, node2);
		if(sup2 == null)
			return JAVA_LANG_OBJECT;
		
		if(sup2.contains(type1))
			return type1;
		if(sup1.contains(type2))
			return type2;
		
		if(sup1.isEmpty() || sup2.isEmpty())
			return JAVA_LANG_OBJECT;
		
		for(int i = 0; i < sup1.size(); i++)
		{
			String s1 = sup1.get(i);
			if(sup2.contains(s1))
				return s1;
		}
		return JAVA_LANG_OBJECT;
	}
	
	private static ClassReader getClassNode(String name)
	{
		try
		{
			byte[] classBytes = ClassPatchManager.INSTANCE.getPatchedResource(UltraminePlugin.isObfEnv ? FMLDeobfuscatingRemapper.INSTANCE.unmap(name) : name,
					FMLDeobfuscatingRemapper.INSTANCE.map(name), CLASSLOADER);
			if(classBytes != null)
			{
				ClassReader reader = new ClassReader(classBytes);
				return reader;
			}
			return null;
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static List<String> getSuperTypesStack(String type, ClassReader node)
	{
		try
		{
			if(node == null)
			{
				Class<?> cls1 = Class.forName(type.replace('/', '.'), false, CLASSLOADER);
				if(cls1.isInterface())
					return null;
				return getSuperTypesStack(cls1);
			}
			else
			{
				return getSuperTypesStack(node);
			}
		}
		catch(ClassNotFoundException e)
		{
			return Collections.emptyList();
		}
	}
	
	private static List<String> getSuperTypesStack(ClassReader node)
	{
		String superName = FMLDeobfuscatingRemapper.INSTANCE.map(node.getSuperName());
		if(superName.equals(JAVA_LANG_OBJECT))
			return Collections.emptyList();
		List<String> list = new ArrayList<String>(4);
		list.add(superName);
		getSuperTypesStack(list, superName);
		return list;
	}
	
	private static List<String> getSuperTypesStack(Class<?> cls)
	{
		if(cls.getSuperclass() == Object.class)
			return Collections.emptyList();
		List<String> list = new ArrayList<String>(4);
		while((cls = cls.getSuperclass()) != Object.class)
			list.add(cls.getName().replace('.', '/'));
		return list;
	}
	
	private static void getSuperTypesStack(List<String> list, String name)
	{
		ClassReader node = getClassNode(name);
		if(node != null)
		{
			String superName = FMLDeobfuscatingRemapper.INSTANCE.map(node.getSuperName());
			if(!superName.equals(JAVA_LANG_OBJECT))
			{
				list.add(superName);
				getSuperTypesStack(list, superName);
			}
		}
		else
		{
			try
			{
				Class<?> cls = Class.forName(name.replace('/', '.'), false, CLASSLOADER);
				for(; cls != Object.class; cls = cls.getSuperclass())
					list.add(cls.getName().replace('.', '/'));
			}
			catch(ClassNotFoundException ignored)
			{
				//will be used incomplete hierarchy
			}
		}
	}
}
