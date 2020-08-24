package org.ultramine.core.util;

import com.google.common.base.Throwables;

import java.util.Arrays;
import java.util.Collection;

public interface Undoable
{
	void undo();

	static Undoable ofAll(Undoable... undoables)
	{
		undoables.getClass(); // NPE
		return ofAll(Arrays.asList(undoables));
	}

	static Undoable ofAll(Collection<Undoable> undoables)
	{
		undoables.getClass(); // NPE
		return () -> {
			Throwable toRethrow = null;
			for(Undoable undoable : undoables)
			{
				try
				{
					if(undoable != null)
						undoable.undo();
				}
				catch(Throwable t)
				{
					if(toRethrow == null)
						toRethrow = t;
					else
						toRethrow.addSuppressed(t);
				}
			}
			if(toRethrow != null)
				Throwables.propagate(toRethrow);
		};
	}
}
