package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public abstract class Pawn extends Piece
{
	protected static final int PAWN_VALUE_INCREASE_PER_ROW = 10;
	
	private PawnTwoStepMove myTwoStepMove;
	private PawnOneStepMove myOneStepMove;
	
	public Pawn(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	public void setOneStepMove(PawnOneStepMove oneStepMove)
	{
		myOneStepMove = oneStepMove;
	}
	
	public void setTwoStepMove(PawnTwoStepMove move)
	{
		myTwoStepMove = move;
	}

	@Override
	public String getPieceName()
	{
		return "Pawn";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.PAWN;
	}
	
	public void removeTwoStepMove(ChessBoard board)
	{
		if(!myTwoStepMove.isRemoved())
		{
			myOneStepMove.setMoveThatDependsOnMe(null);
			myTwoStepMove.removeFromBoard(board);
		}
	}
	
	protected abstract boolean isAtStartingRow();
	
	/**
	 * 
	 * @param position the position that should be evaluated (usually the Pawns current position)
	 * @return true if it's time for this pawn to be replaced
	 */
	public boolean isTimeForReplacement(Position position)
	{
		int destinationRow = (isBlack()) ? 0 :(ChessBoard.BOARD_SIZE - 1);
		return position.getRow() == destinationRow;
	}
	
	/**
	 * 
	 * @param position the position that should be evaluated (usually the Pawns current position)
	 * @param affinity the affinity of the pawn
	 * @return true if it's time for this pawn to be replaced
	 */
	public static boolean isTimeForReplacement(Position position, boolean affinity)
	{
		int destinationRow = (affinity == Piece.BLACK) ? 0 :(ChessBoard.BOARD_SIZE - 1);
		return position.getRow() == destinationRow;
	}
	
	@Override
	public void revertedAMove(ChessBoard board, Position oldPosition)
	{
		if(isAtStartingRow() && myTwoStepMove.isRemoved())
		{
			//The two step move can now be re-enabled
			myTwoStepMove.reEnable();
			myOneStepMove.setMoveThatDependsOnMe(myTwoStepMove);
			myTwoStepMove.updateMove(board);
		}
		//The pawn was removed because it reached it's destination, we need to add it again
		if(isTimeForReplacement(oldPosition))
		{
			board.addPiece(this, true, false);
		}
	}
}
