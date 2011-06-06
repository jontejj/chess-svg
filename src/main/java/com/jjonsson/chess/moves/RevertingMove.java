package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;

/**
 * A move that can revert another move
 * @author jontejj
 *
 */
public class RevertingMove extends IndependantMove {

	private Move myMoveToRevert;
	private Piece myPieceToPlaceAtOldPosition;
	
	//Used when pawns reach the top/bottom of the board (i.e the queen should be removed)
	private Piece myPieceThatReplacedMyPiece;
	public RevertingMove(Move moveToRevert) 
	{
		super(-moveToRevert.getRowChange(), -moveToRevert.getColumnChange(), moveToRevert.myPiece);
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
	protected boolean canBeMadeInternal(ChessBoard board) 
	{
		return false;
	}
	
	/**
	 * A reverting move can always be made if the it belongs to the move that was made last
	 */
	@Override
	public boolean canBeMade(ChessBoard board)
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
	 * @param p the piece that the pawn was replaced with
	 */
	public void setPieceThatReplacedMyPiece(Piece p)
	{
		myPieceThatReplacedMyPiece = p;
	}
	
	public void setPieceAtOldPosition(Piece removedPiece)
	{
		myPieceToPlaceAtOldPosition = removedPiece;
	}
	
	
	/**
	 * 
	 * @return the piece that this move took over or null if it didn't take over any piece
	 */
	public Piece getPieceThatITookOver()
	{
		return myPieceToPlaceAtOldPosition;
	}
	
	/**
	 * Returns the position that the piece this move is connected to previously was at
	 */
	@Override
	public Position getPositionIfPerformed()
	{
		Position curPos = myPiece.getCurrentPosition();
		try
		{
			//TODO(jontejj): this could be cached
			return Position.createPosition(curPos.getRow() + getRowChange() + 1, curPos.getColumn() + getColumnChange() + 1);
		}
		catch (InvalidPosition e)
		{
			//Could happen for a reverting move if the move it's connected to isn't possible to do due to it being out of bounds
		}
		return null;
	}
	
	@Override
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		if(canBeMade(board))
		{
			//If a pawn was replaced by another piece
			if(myPieceThatReplacedMyPiece != null)
				myPieceThatReplacedMyPiece.removeFromBoard(board);
			
			super.makeMove(board);
			
			//The move that this move reverts took over a piece so that needs to be restored
			if(myPieceToPlaceAtOldPosition != null)
			{
				myPieceToPlaceAtOldPosition.reEnablePossibleMoves();
				board.addPiece(myPieceToPlaceAtOldPosition, true, false);
				board.updatePossibilityOfMovesForPosition(myPieceToPlaceAtOldPosition.getCurrentPosition());
			}
			
			myPiece.reEnablePossibleMoves();
			
			board.popLastMoveIfEqual(myMoveToRevert);
			
			myPiece.revertedAMove(board);
		}
		else
			throw new UnavailableMoveException(this);
	}
	
	/**
	 * 
	 * @return if this move was the last one to be made this will return the position where the piece previously was at
	 */
	@Override
	public Position getOldPosition()
	{
		return myMoveToRevert.getPositionIfPerformed();
	}

}
