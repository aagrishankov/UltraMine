package org.ultramine.core.util;

public interface UndoableValue<T> extends Undoable
{
	T getValue();

	static <T> UndoableValue<T> of(T value, Undoable undoable)
	{
		return new UndoableValue<T>()
		{
			@Override
			public T getValue()
			{
				return value;
			}

			@Override
			public void undo()
			{
				undoable.undo();
			}
		};
	}
}
