package org.ultramine.server.util;

import java.util.ArrayList;
import java.util.Collection;

public class ModificationControlList<E> extends ArrayList<E>
{
	private int lastModCount;
	
	public ModificationControlList(int initialCapacity)
	{
		super(initialCapacity);
	}

	public ModificationControlList()
	{
	}

	public ModificationControlList(Collection<? extends E> c)
	{
		super(c);
	}
	
	public int getModCount()
	{
		return modCount;
	}
	
	public boolean isModified()
	{
		return lastModCount != modCount;
	}
	
	public void resetModified()
	{
		lastModCount = modCount;
	}
	
	public boolean checkModifiedAndReset()
	{
		boolean isMod = isModified();
		resetModified();
		return isMod;
	}
}
