package org.ultramine.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface UndoableAction
{
	Undoable apply();

	static UndoableAction ofAll(UndoableAction... actions)
	{
		actions.getClass(); //NPE
		return ofAll(Arrays.asList(actions));
	}

	static UndoableAction ofAll(Collection<UndoableAction> actions)
	{
		actions.getClass(); //NPE
		for(UndoableAction action : actions)
			action.getClass(); // NPE
		return () -> {
			for(UndoableAction action : actions)
				action.getClass(); // NPE (again, collection may be mutable)
			List<Undoable> undoables = new ArrayList<>(actions.size());
			for(UndoableAction action : actions)
			{
				try
				{
					undoables.add(action.apply());
				}
				catch(Throwable t)
				{
					try
					{
						Undoable.ofAll(undoables).undo();
					}
					catch(Throwable t1)
					{
						t.addSuppressed(t1);
						throw t;
					}

					throw t;
				}
			}

			return Undoable.ofAll(undoables);
		};
	}
}
