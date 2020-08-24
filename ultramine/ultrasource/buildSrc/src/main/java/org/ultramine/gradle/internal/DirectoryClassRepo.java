package org.ultramine.gradle.internal;

import net.md_5.specialsource.repo.CachingRepo;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;

public class DirectoryClassRepo extends CachingRepo
{
	private final File dir;

	public DirectoryClassRepo(File dir)
	{
		this.dir = dir;
	}

	@Override
	protected ClassNode findClass0(String internalName)
	{
		File file = new File(dir, internalName + ".class");
		if(!file.isFile())
			return null;
		try {
			byte[] bytes = FileUtils.readFileToByteArray(file);

			ClassReader reader = new ClassReader(bytes);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			return node;
		} catch (IOException e) {
			throw new RuntimeException("Failed to read classfile: "+file.getAbsolutePath(), e);
		}
	}
}
