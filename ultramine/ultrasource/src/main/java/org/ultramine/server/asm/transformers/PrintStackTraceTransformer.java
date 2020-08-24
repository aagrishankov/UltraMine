package org.ultramine.server.asm.transformers;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

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
 * from {@link Throwable#printStackTrace()} to {@link org.ultramine.server.internal.UMHooks#printStackTrace(Throwable)};
 */
public class PrintStackTraceTransformer implements IUMClassTransformer
{
	private static final Logger log = LogManager.getLogger();
	
	private static final String UMHOOKS_TYPE = "org/ultramine/server/internal/UMHooks";
	private static final Set<String> THROWABLE_TYPES = new HashSet<String>();
	private static final String PST_NAME = "printStackTrace";
	private static final String PST_DESC = "()V";
	private static final String UM_PST_DESC = "(Ljava/lang/Throwable;)V";
	
	static
	{
		THROWABLE_TYPES.add("java/lang/Throwable");
		THROWABLE_TYPES.add("java/lang/Exception");
		THROWABLE_TYPES.add("java/lang/RuntimeException");
		THROWABLE_TYPES.add("java/io/IOException");
	}
	
	@Override
	public TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode)
	{
		boolean modified = false;
		for(MethodNode m : classNode.methods)
		{
			for(ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
			{
				AbstractInsnNode insnNode = it.next();
				if(insnNode.getType() == AbstractInsnNode.METHOD_INSN)
				{
					MethodInsnNode mi = (MethodInsnNode)insnNode;
					if(THROWABLE_TYPES.contains(mi.owner) && PST_NAME.equals(mi.name) && PST_DESC.equals(mi.desc) && mi.getOpcode() == Opcodes.INVOKEVIRTUAL)
					{
						log.trace("Method {}.{}{}: Replacing INVOKEVIRTUAL {}.printStackTrace with INVOKESTATIC UMHooks.printStackTrace", name, m.name, m.desc, mi.owner);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, UMHOOKS_TYPE, PST_NAME, UM_PST_DESC, false);
						it.add(replace);
						modified = true;
					}
				}
			}
		}

		return modified ? TransformResult.MODIFIED : TransformResult.NOT_MODIFIED;
	}
}
