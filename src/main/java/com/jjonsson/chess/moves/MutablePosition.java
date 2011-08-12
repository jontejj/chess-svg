package com.jjonsson.chess.moves;

public class MutablePosition extends Position
{

	public MutablePosition(final byte row, final byte column)
	{
		super(row, column);
	}


	public void applyMove(final Move move)
	{
		myRow += move.getRowChange();
		myColumn += move.getColumnChange();
	}

	@Override
	public MutablePosition copy()
	{
		return new MutablePosition(myRow, myColumn);
	}
}
