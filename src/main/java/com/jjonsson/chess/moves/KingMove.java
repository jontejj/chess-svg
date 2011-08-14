package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.King;

public class KingMove extends IndependantMove
{

	public KingMove(final int rowChange, final int columnChange, final King pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	@Override
	public boolean canBeMade(final ChessBoard board)
	{
		if(board.isMoveUnavailableDueToCheck(this))
		{
			return false;
		}

		return myCanBeMadeCache;
	}

	@Override
	public boolean canBeMadeInternal(final ChessBoard board)
	{
		ImmutablePosition newPosition = this.getDestination();
		if(newPosition == null)
		{
			//The move was out of bounds
			return false;
		}

		Move threateningMove = board.moveThreateningPosition(newPosition, !getAffinity(), getPiece(), true);

		if(threateningMove != null)
		{
			//If this is true for all the king's moves then the game is over
			return false;
		}

		return canBeMadeEnding();
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
			case -1:
				if(columnChange == -1)
				{
					return 0;
				}
				else if(columnChange == 0)
				{
					return 1;
				}
				return 2;
			case 0:
				if(columnChange == -1)
				{
					return 3;
				}
				return 4;
			case 1:
				if(columnChange == -1)
				{
					return 5;
				}
				else if(columnChange == 0)
				{
					return 6;
				}
				return 7;
		}
		//Detect faulty moves early
		return -1;
	}
}
