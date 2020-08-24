package org.ultramine.server.service;

import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.ultramine.server.util.UnsafeUtil;

import org.ultramine.core.service.ServiceDelegate;
import sun.misc.Unsafe;

public class ServiceDelegateGenerator
{
	private static final Unsafe U = UnsafeUtil.getUnsafe();
	private static final String ServiceDelegate_INTERNAL_NAME = Type.getInternalName(ServiceDelegate.class);
	private static final String NotImplementedServiceProvider_INTERNAL_NAME = Type.getInternalName(NotResolvedServiceProvider.class);

	@SuppressWarnings("unchecked")
	public static <T> Class<ServiceDelegate<T>> makeServiceDelegate(Class<?> base, String name, Class<T> iface)
	{
		return (Class<ServiceDelegate<T>>) U.defineAnonymousClass(base, makeServiceDelegate(name, iface), null);
	}

	public static byte[] makeServiceDelegate(String name, Class<?> iface)
	{
		if(!iface.isInterface())
			throw new IllegalArgumentException("iface should be an interface");

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		String thisClassInternalName = name.replace('.',  '/');
		String ifaceInternalName = Type.getInternalName(iface);
		String ifaceDesc = Type.getDescriptor(iface);

		cw.visit(V1_5, ACC_PUBLIC | ACC_SUPER, thisClassInternalName, null, "java/lang/Object", new String[]{ ifaceInternalName, ServiceDelegate_INTERNAL_NAME });
		cw.visitSource(".dynamic", null);

		{
			cw.visitField(ACC_PUBLIC, "instance", ifaceDesc, null, null).visitEnd();
		}

		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setProvider", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, ifaceInternalName);
			mv.visitFieldInsn(PUTFIELD, thisClassInternalName, "instance", ifaceDesc);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getProvider", "()Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, thisClassInternalName, "instance", ifaceDesc);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		for(Method method : iface.getDeclaredMethods())
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, thisClassInternalName, "instance", ifaceDesc);
			int argCounter = 1;
			for(Parameter par : method.getParameters())
			{
				int insn = loadInsnForType(par.getType());
				mv.visitVarInsn(insn, argCounter);
				argCounter += insn == LLOAD || insn == DLOAD ? 2 : 1;
			}
			mv.visitMethodInsn(INVOKEINTERFACE, ifaceInternalName, method.getName(), Type.getMethodDescriptor(method), true);

			mv.visitInsn(returnInsnForType(method.getReturnType()));
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		cw.visitEnd();
		return cw.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> makeNotResolvedServiceProvider(Class<?> base, String name, Class<T> iface)
	{
		return (Class<T>) U.defineAnonymousClass(base, makeNotResolvedServiceProvider(name, iface), null);
	}

	public static byte[] makeNotResolvedServiceProvider(String name, Class<?> iface)
	{
		if(!iface.isInterface())
			throw new IllegalArgumentException("iface should be an interface");

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		String thisClassInternalName = name.replace('.',  '/');
		String ifaceInternalName = Type.getInternalName(iface);
		String ifaceDesc = Type.getDescriptor(iface);

		cw.visit(V1_5, ACC_PUBLIC | ACC_SUPER, thisClassInternalName, null, NotImplementedServiceProvider_INTERNAL_NAME,
				new String[]{ ifaceInternalName});
		cw.visitSource(".dynamic", null);

		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, NotImplementedServiceProvider_INTERNAL_NAME, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		for(Method method : iface.getDeclaredMethods())
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, thisClassInternalName, "resolveProvider", "()Ljava/lang/Object;", false);
			int argCounter = 1;
			for(Parameter par : method.getParameters())
			{
				int insn = loadInsnForType(par.getType());
				mv.visitVarInsn(insn, argCounter);
				argCounter += insn == LLOAD || insn == DLOAD ? 2 : 1;
			}
			mv.visitMethodInsn(INVOKEINTERFACE, ifaceInternalName, method.getName(), Type.getMethodDescriptor(method), true);

			mv.visitInsn(returnInsnForType(method.getReturnType()));
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static int loadInsnForType(Class<?> cls)
	{
		if(cls == boolean.class || cls == byte.class || cls == short.class || cls == int.class) return ILOAD;
		if(cls == long.class) return LLOAD;
		if(cls == float.class) return FLOAD;
		if(cls == double.class) return DLOAD;
		return ALOAD;
	}

	private static int returnInsnForType(Class<?> cls)
	{
		if(cls == boolean.class || cls == byte.class || cls == short.class || cls == int.class) return IRETURN;
		if(cls == long.class) return LRETURN;
		if(cls == float.class) return FRETURN;
		if(cls == double.class) return DRETURN;
		if(cls == void.class) return RETURN;
		return ARETURN;
	}
}
