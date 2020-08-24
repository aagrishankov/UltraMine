package org.ultramine.server.asm.transformers;

import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import org.ultramine.server.asm.UMTBatchTransformer.IUMClassTransformer;
import org.ultramine.server.asm.UMTBatchTransformer.TransformResult;

/**
 * This transformer redirects method invocations: <br />
 * from {@link Math#atan(double)} to {@link org.ultramine.server.util.TrigMath#atan(double)}; <br />
 * from {@link Math#atan2(double, double)} to {@link org.ultramine.server.util.TrigMath#atan2(double, double)}
 */
public class TrigMathTransformer implements IUMClassTransformer
{
	private static final Logger log = LogManager.getLogger();
	
	private static final String TRIGMATH_TYPE = "org/ultramine/server/util/TrigMath";
	private static final String MATH_TYPE = "java/lang/Math";
	private static final String ATAN2_NAME = "atan2";
	private static final String ATAN2_DESC = "(DD)D";
	private static final String ATAN_NAME = "atan";
	private static final String ATAN_DESC = "(D)D";
	
	@Override
	public TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode)
	{
		boolean modified = false;
		for (MethodNode m : classNode.methods)
		{
			for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
			{
				AbstractInsnNode insnNode = it.next();
				if (insnNode.getType() == AbstractInsnNode.METHOD_INSN)
				{
					MethodInsnNode mi = (MethodInsnNode)insnNode;
					if (MATH_TYPE.equals(mi.owner) && ATAN2_NAME.equals(mi.name) && ATAN2_DESC.equals(mi.desc) && mi.getOpcode() == Opcodes.INVOKESTATIC)
					{
						log.trace("Method {}.{}{}: Replacing INVOKESTATIC Math.atan2 with INVOKESTATIC TrigMath.atan2", name, m.name, m.desc);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, TRIGMATH_TYPE, ATAN2_NAME, ATAN2_DESC, false);
						it.add(replace);
						modified = true;
					}
					
					if (MATH_TYPE.equals(mi.owner) && ATAN_NAME.equals(mi.name) && ATAN_DESC.equals(mi.desc) && mi.getOpcode() == Opcodes.INVOKESTATIC)
					{
						log.trace("Method {}.{}{}: Replacing INVOKESTATIC Math.atan with INVOKESTATIC TrigMath.atan", name, m.name, m.desc);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, TRIGMATH_TYPE, ATAN_NAME, ATAN_DESC, false);
						it.add(replace);
						modified = true;
					}
				}
			}
		}

		return modified ? TransformResult.MODIFIED : TransformResult.NOT_MODIFIED;
	}
}
