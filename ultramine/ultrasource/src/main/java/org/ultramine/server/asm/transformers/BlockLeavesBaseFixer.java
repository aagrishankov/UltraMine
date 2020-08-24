package org.ultramine.server.asm.transformers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.ultramine.server.asm.UMTBatchTransformer.IUMClassTransformer;
import org.ultramine.server.asm.UMTBatchTransformer.TransformResult;

import java.util.Iterator;

/**
 * Removing conflicting {@link net.minecraft.block.BlockLeavesBase#onNeighborBlockChange} methods
 */
public class BlockLeavesBaseFixer implements IUMClassTransformer
{
	private static final Logger log = LogManager.getLogger();
	private static final String TARGET_METHOD_NAME = "onNeighborBlockChange";
	private static final String TARGET_METHOD_NAME_OBF = "func_149695_a";

	@Override
	public TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode)
	{
		boolean modified = false;
		int methodCount = 0;
		for(MethodNode m : classNode.methods)
		{
			if(isTargetMethod(m.name))
				methodCount++;
		}

		// removing ultramine method
		if(methodCount > 1)
		{
			for(Iterator<MethodNode> it = classNode.methods.iterator(); it.hasNext();)
			{
				MethodNode m = it.next();
				// ultramine BlockLeavesBase.onNeighborBlockChange signature
				if(isTargetMethod(m.name) && m.maxStack == 8 && m.maxLocals == 7 && m.instructions.size() == 48)
				{
					log.warn("Method net.minecraft.block.BlockLeavesBase.onNeighborBlockChange() is now overridden by other mod, fastLeafDecay world setting will not work");
					it.remove();
					--methodCount;
					modified = true;
					break;
				}
			}
		}

		// removing other mod methods if still conflicting
		if(methodCount > 1)
		{
			for(Iterator<MethodNode> it = classNode.methods.iterator(); it.hasNext();)
			{
				MethodNode m = it.next();
				if(isTargetMethod(m.name))
				{
					log.warn("Removed conflicted method net.minecraft.block.BlockLeavesBase.onNeighborBlockChange()");
					it.remove();
					modified = true;
					if(--methodCount == 1)
						break;
				}
			}
		}

		return modified ? TransformResult.MODIFIED : TransformResult.NOT_MODIFIED;
	}

	private static boolean isTargetMethod(String name)
	{
		return name.equals(TARGET_METHOD_NAME) || name.equals(TARGET_METHOD_NAME_OBF);
	}
}
