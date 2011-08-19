package com.jjonsson.chess.moves;

import static com.jjonsson.utilities.Logger.LOGGER;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * A move that can revert another move
 * @author jontejj
 *
 */
public class RevertingMove extends IndependantMove {

	private Move myMoveToRevert;
	private Piece myPieceToPlaceAtOldPosition;

	/**
	 * Used when pawns reach the top/bottom of the board (i.e the queen should be removed)
	 */
	private Piece myPawnPromotionPiece;

	public RevertingMove(final Move moveToRevert)
	{
		super(-moveToRevert.getRowChange(), -moveToRevert.getColumnChange(), moveToRevert.getPiece());
		myMoveToRevert = moveToRevert;
	}

	/**
	 * It will not be possible to redo a undone move
	 */
	@Override
	protected void setRevertingMove()
	{
		myRevertingMove = this;
	}


	/**
	 * Should never matter for a reverting move
	 */
	@Override
	protected boolean canBeMadeInternal(final ChessBoard board)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>A reverting move can always be made if the it belongs to the move that was made last
	 */
	@Override
	public boolean canBeMade(final ChessBoard board)
	{
		Move lastMove = board.getLastMove();
		return lastMove != null && lastMove.getRevertingMove() == this;
	}

	@Override
	public byte getRowChange()
	{
		return (byte) -myMoveToRevert.getRowChange();
	}

	@Override
	public byte getColumnChange()
	{
		return (byte) -myMoveToRevert.getColumnChange();
	}

	/**
	 * Should be used when a pawn has reached the top/bottom and the piece that the pawn was replaced with should be removed from
	 * the board
	 * @param p the piece that the pawn was promoted to
	 */
	public void setPawnPromotionPiece(final Piece p)
	{
		myPawnPromotionPiece = p;
	}

	public void setPieceToPlaceAtOldPosition(final Piece removedPiece)
	{
		myPieceToPlaceAtOldPosition = removedPiece;
	}

	/**
	 * Returns the position that the piece this move is connected to previously was at
	 */
	@Override
	public ImmutablePosition getDestination()
	{
		Position curPos = getCurrentPosition();
		int row = curPos.getRow() + getRowChange();
		int column = curPos.getColumn() + getColumnChange();
		if(Position.isInvalidPosition(row, column))
		{
			//Could happen for a reverting move if the move it's connected to isn't possible to do due to it being out of bounds
			return null;
		}
		return ImmutablePosition.from(row, column);
	}

	@Override
	public boolean makeMove(final ChessBoard board)
	{
		if(canBeMade(board))
		{
			Position oldPosition = getCurrentPosition();

			if(myPawnPromotionPiece != null)
			{
				myPawnPromotionPiece.removeFromBoard(board);
			}

			boolean wasRemoved = getPiece().isRemoved();
			//This makes it possible for the piece to make once again if the piece was removed previously
			getPiece().reEnablePossibleMoves();

			if(!super.makeMoveWithoutChecking(board))
			{
				//Revert what we just did because this reverting move is not possible to make
				LOGGER.warning("Failed to revert: " + this);
				if(wasRemoved)
				{
					getPiece().removeFromBoard(board);
				}
				if(myPawnPromotionPiece != null)
				{
					board.addPiece(myPawnPromotionPiece, true, false);
				}
				return false;
			}
			myPawnPromotionPiece =null;

			//The move that this move reverts took over a piece so that needs to be restored
			if(myPieceToPlaceAtOldPosition != null)
			{
				myPieceToPlaceAtOldPosition.reEnablePossibleMoves();
				board.addPiece(myPieceToPlaceAtOldPosition, true, false);
				board.updatePossibilityOfMovesForPosition(myPieceToPlaceAtOldPosition.getCurrentPosition());
			}

			board.popLastMoveIfEqual(myMoveToRevert);
			//Decrement how many times the move to revert has been made
			myMoveToRevert.setMovesMade(myMoveToRevert.getMovesMade()-1);
			//Super increased the moves made
			getPiece().setMovesMade(getPiece().getMovesMade() - 2);

			getPiece().revertedAMove(board, oldPosition);
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return if this move was the last one to be made this will return the position where the piece previously was at
	 */
	@Override
	public ImmutablePosition getOldPosition()
	{
		return myMoveToRevert.getDestination();
	}

	@Override
	public boolean isPartOfAnotherMove()
	{
		return myMoveToRevert.isPartOfAnotherMove();
	}

	@Override
	public boolean shouldBeIncludedInMoveTable()
	{
		return false;
	}
}
