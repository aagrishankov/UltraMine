package org.ultramine.gradle.internal;

import net.md_5.specialsource.provider.InheritanceProvider;
import net.md_5.specialsource.repo.ClassRepo;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RepoInheritanceProvider implements InheritanceProvider
{
	private final ClassRepo repo;

	public RepoInheritanceProvider(ClassRepo repo)
	{
		this.repo = repo;
	}

	@Override
	public Collection<String> getParents(String owner) {
		ClassNode node = repo.findClass(owner);
		if(node == null) {
			return null;
		} else {
			ArrayList<String> parents = new ArrayList<String>(node.interfaces.size() + 1);
			parents.addAll(node.interfaces);

			if(node.superName != null)
				parents.add(node.superName);

			return parents;
		}
	}
}
