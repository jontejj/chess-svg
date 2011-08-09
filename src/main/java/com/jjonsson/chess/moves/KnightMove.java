package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class KnightMove extends IndependantMove
{

	public KnightMove(final int rowChange, final int columnChange,final Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	@Override
	public boolean canBeMadeInternal(final ChessBoard board)
	{
		return canBeMadeDefault();
	}

	@Override
	protected int getFirstDimensionIndexInternal()
	{
		return 0;
	}

	@Override
	protected int getSecondDimensionIndexInternal()
	{
		int columnChange = getColumnChange();
		switch(getRowChange())
		{
			case -2:
				if(columnChange == -1)
				{
					return 0;
				}
				return 1;
			case -1:
				if(columnChange == -2)
				{
					return 2;
				}
				return 3;
			case 1:
				if(columnChange == -2)
				{
					return 4;
				}
				return 5;
			case 2:
				if(columnChange == -1)
				{
					return 6;
				}
				return 7;
		}
		//Detect faulty moves early
		return -1;
	}

}
