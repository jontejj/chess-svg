package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class PawnTakeOverMove extends PawnMove
{

	public PawnTakeOverMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		//TODO: fix better constructor for this move (it's not dependent on any other move, but it shares functionality with PawnMove)
	}

	@Override
	public boolean canBeMadeInternal(ChessBoard board)
	{
		//No bounds checking needs to be done because a pawn will be replaced by another piece before then
		Position newPosition = this.getPositionIfPerformed();
		
		Piece pieceAtDestination = board.getPiece(newPosition);
		setPieceAtDestination(pieceAtDestination, board);
		if(pieceAtDestination == null)
			return false; //The space is free but this move requires a take over
		else if(pieceAtDestination.hasSameAffinityAs(myPiece))
			return false; //You can't take over your own pieces
		else
		{
			//Take over is available
			return true;
		}
	}
	
	/**
	 * Overridden to prioritize moves that takes pawns further
	 * @return the value of the piece at this move's destination
	 */
	@Override
	public int getTakeOverValue()
	{
		if(isTakeOverMove())
		{
			return getPieceAtDestination().getValue() + 10;
		}
		return 0;
	}
}
