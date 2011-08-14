package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class PawnTakeOverMove extends PawnMove
{

	public PawnTakeOverMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		//TODO(jontejj): fix better constructor for this move (it's not dependent on any other move, but it shares functionality with PawnMove)
	}

	@Override
	public boolean canBeMadeInternal(ChessBoard board)
	{
		if(getPieceAtDestination() == null || getPieceAtDestination().hasSameAffinityAs(getPiece()))
		{
			//The space is either free (this move requires a take over)
			//Or it's taken by one of your own pieces
			return false;
		}
		//Take over is available
		return true;
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
			//It may be a move that reaches it's destination as well
			return getPieceAtDestination().getValue() + PAWN_PROGRESSIVENESS_VALUE + super.getTakeOverValue();
		}
		return super.getTakeOverValue();
	}
	
	
	@Override
	public boolean canBeTakeOverMove()
	{
		return true;
	}
	
	@Override
	protected int getSecondDimensionIndexInternal()
	{
		return (getColumnChange() > 0) ? 2 : 3;
	}
}
