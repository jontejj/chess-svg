package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public class PawnTwoStepMove extends PawnMove
{

	public PawnTwoStepMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		
		Pawn.class.cast(pieceThatTheMoveWillBeMadeWith).setTwoStepMove(this);
	}
	
	@Override
	public boolean isTakeOverMove()
	{
		return false;
	}
	
	/**
	 * Overridden to not accumulate takeover/protecting values as this move can't either protect nor take over a piece
	 */
	@Override
	public void setPieceAtDestination(Piece p, ChessBoard board)
	{
		myPieceAtDestination = p;
	}
}
