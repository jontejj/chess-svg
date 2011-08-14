package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public abstract class PawnMove extends DependantMove
{
	/**
	 * This value determines how much it's prioritized that pawns goes forward
	 */
	protected static final int PAWN_PROGRESSIVENESS_VALUE = 20;
	
	public PawnMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
	}

	/**
	 * Makes this move and checks if the pawn has reached the bottom of the board, if so then it replaces that piece with a queen
	 * <br>When a pawn move is made it means that the 2-step move shouldn't be available anymore
	 * @throws UnavailableMoveException  if this move isn't available right now
	 */
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
		
		if(Pawn.class.cast(getPiece()).isTimeForReplacement(getCurrentPosition()))
		{
			//The white/black pawn has reached the bottom/top and now it's time to replace him
			getRevertingMove().setPieceThatReplacedMyPiece(board.replacePawn(getPiece()));
		}
		Pawn.class.cast(getPiece()).removeTwoStepMove(board);
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{
		if(getPieceAtDestination() == null)
		{
			return true; //The space is free
		}
		
		return false;
	}
	
	@Override
	public int getProgressiveValue()
	{
		return PAWN_PROGRESSIVENESS_VALUE;
	}
	
	/**
	 * Overridden to prioritize replacement moves (Not actually a take over)
	 * @return the value of the piece at this move's destination
	 */
	@Override
	public int getTakeOverValue()
	{
		if(myCanBeMadeCache && Pawn.isTimeForReplacement(getDestination(), getAffinity()))
		{
			return Piece.QUEEN_VALUE - Piece.PAWN_VALUE;
		}
		return 0;
	}
	
	@Override
	public boolean canBeTakeOverMove()
	{
		return false;
	}
	
	@Override
	protected int getFirstDimensionIndexInternal()
	{
		return 0;
	}
}
