package org.ultramine.server.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.ultramine.server.asm.UMTBatchTransformer.IUMClassTransformer;
import org.ultramine.server.asm.UMTBatchTransformer.TransformResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ServiceInjectionTransformer implements IUMClassTransformer
{
	private static final String INJECT_SERVICE_DESC = "Lorg/ultramine/core/service/InjectService;";
	private static final String SBA_CLASS = "org/ultramine/core/service/ServiceBytecodeAdapter";

	@Nonnull
	@Override
	public TransformResult transform(String name, String transformedName, ClassReader classReader, ClassNode classNode)
	{
		List<FieldNode> fieldsToInject = new ArrayList<>();
		for(FieldNode f : classNode.fields)
			if(f.visibleAnnotations != null)
				for(AnnotationNode ann : f.visibleAnnotations)
					if(ann.desc.equals(INJECT_SERVICE_DESC))
						fieldsToInject.add(f);

		if(fieldsToInject.size() > 0)
		{
			for(FieldNode field : fieldsToInject)
			{
				if((field.access & Opcodes.ACC_STATIC) == 0)
					throw new RuntimeException("Service injection for non-static fields is not supported: " + classNode.name + "#" + field.name);
				if((field.access & Opcodes.ACC_FINAL) != 0)
					throw new RuntimeException("Service injection for final fields is not supported: " + classNode.name + "#" + field.name);
			}

			for(FieldNode field : fieldsToInject)
				field.access |= Opcodes.ACC_FINAL;

			boolean clinitFound = false;
			for(MethodNode method : classNode.methods)
			{
				if(method.name.equals("<clinit>"))
				{
					for(FieldNode field : fieldsToInject)
						method.instructions.insert(buildInjectorFor(classNode.name, field));
					clinitFound = true;
					break;
				}
			}

			if(!clinitFound)
			{
				MethodNode method = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
				for(FieldNode field : fieldsToInject)
					method.instructions.insert(buildInjectorFor(classNode.name, field));
				method.instructions.add(new InsnNode(Opcodes.RETURN));
				classNode.methods.add(method);
			}

			return TransformResult.MODIFIED_STACK;
		}

		return TransformResult.NOT_MODIFIED;
	}

	private static InsnList buildInjectorFor(String owner, FieldNode field)
	{
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(Type.getType(field.desc)));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SBA_CLASS, "provideService", "(Ljava/lang/Class;)Ljava/lang/Object;", false));
		list.add(new TypeInsnNode(Opcodes.CHECKCAST, field.desc.substring(1, field.desc.length()-1)));
		list.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, field.name, field.desc));
		return list;
	}
}
