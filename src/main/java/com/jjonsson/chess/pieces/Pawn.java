package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.moves.Position.BLACK_STARTING_ROW_INDEX;
import static com.jjonsson.chess.moves.Position.WHITE_STARTING_ROW_INDEX;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public abstract class Pawn extends Piece
{
	protected static final int PAWN_VALUE_INCREASE_PER_ROW = 10;

	private PawnTwoStepMove myTwoStepMove;
	private PawnOneStepMove myOneStepMove;

	public Pawn(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	public void setOneStepMove(final PawnOneStepMove oneStepMove)
	{
		myOneStepMove = oneStepMove;
	}

	public void setTwoStepMove(final PawnTwoStepMove move)
	{
		myTwoStepMove = move;
	}

	@Override
	public String getPieceName()
	{
		return "Pawn";
	}

	@Override
	protected byte getPersistenceIdentifierType()
	{
		return Piece.PAWN;
	}

	public void removeTwoStepMove(final ChessBoard board)
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
	public boolean isTimeForReplacement(final Position position)
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
	public static boolean isTimeForReplacement(final Position position, final boolean affinity)
	{
		int destinationRow = (affinity == BLACK) ? WHITE_STARTING_ROW_INDEX : BLACK_STARTING_ROW_INDEX;
		return position.getRow() == destinationRow;
	}

	@Override
	public void revertedAMove(final ChessBoard board, final Position oldPosition)
	{
		if(myTwoStepMove.isRemoved() && isAtStartingRow())
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

	@Override
	public int getFirstDimensionMaxIndex()
	{
		return 0;
	}

	@Override
	public int getSecondDimensionMaxIndex()
	{
		return 3; //Two steps forward, two take over moves
	}
}
