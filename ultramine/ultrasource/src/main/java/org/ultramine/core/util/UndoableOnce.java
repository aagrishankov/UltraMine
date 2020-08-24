package org.ultramine.core.util;

import jline.internal.Nullable;

public final class UndoableOnce implements Undoable
{
	private static final UndoableOnce EMPTY = new UndoableOnce(null);
	@Nullable private Undoable undoable;

	private UndoableOnce(@Nullable Undoable undoable)
	{
		this.undoable = undoable;
	}

	public UndoableOnce of(Undoable undoable)
	{
		return new UndoableOnce(undoable);
	}

	public UndoableOnce empty()
	{
		return EMPTY;
	}

	@Override
	public void undo()
	{
		if(undoable != null)
		{
			undoable.undo();
			undoable = null;
		}
	}
}
