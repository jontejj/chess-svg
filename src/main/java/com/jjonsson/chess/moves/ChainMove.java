package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.pieces.Piece;

public class ChainMove extends DependantMove
{

	public ChainMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{	
		return canBeMadeDefault();
	}

}
