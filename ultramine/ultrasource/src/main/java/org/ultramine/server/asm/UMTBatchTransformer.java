package org.ultramine.server.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UMTBatchTransformer implements IClassTransformer
{
	private static final boolean REPAIR_FRAMES = Boolean.parseBoolean(System.getProperty("org.ultramine.core.asm.repairJavaClassFrames", "true"));
	private List<IUMClassTransformer> globalTransformers = new ArrayList<>();
	private Map<String, List<IUMClassTransformer>> specialTransformers = new HashMap<>();

	protected void registerGlobalTransformer(IUMClassTransformer transformer)
	{
		globalTransformers.add(transformer);
	}

	protected void registerSpecialTransformer(IUMClassTransformer transformer, String className)
	{
		specialTransformers.computeIfAbsent(className, k -> new ArrayList<>(1)).add(transformer);
	}

	protected void registerSpecialTransformer(IUMClassTransformer transformer, String... classNames)
	{
		for(String name : classNames)
			registerSpecialTransformer(transformer, name);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(basicClass == null)
			return null;
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);

		int flags = 0;
		for(IUMClassTransformer transformer : specialTransformers.getOrDefault(transformedName, Collections.emptyList()))
			flags |= transformer.transform(name, transformedName, classReader, classNode).ordinal();
		for(IUMClassTransformer transformer : globalTransformers)
			flags |= transformer.transform(name, transformedName, classReader, classNode).ordinal();

		// Computing frames even if we did not changed class to fix other mod changes of 1.7 & 1.8 classes
		boolean shouldComputeFrames = REPAIR_FRAMES && (classNode.version & 0xFFFF) > Opcodes.V1_6;
		if(flags == 0 && !shouldComputeFrames)
			return basicClass;

		ClassWriter writer = shouldComputeFrames ? new ComputeFramesClassWriter() : new ClassWriter(flags == 1 ? 0 : 1);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public enum TransformResult
	{
		/** ClassNode not modified by this transformer*/
		NOT_MODIFIED,
		/** ClassNode modified, but stack max and stack map not changed */
		MODIFIED,
		/** ClassNode modified and stack changed, stack max (and frames, of class version > 1.6) will be recounted */
		MODIFIED_STACK
	}

	public interface IUMClassTransformer
	{
		@Nonnull TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode);
	}
}
