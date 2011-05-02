package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;

public class PawnMove extends DependantMove
{
	public PawnMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
	}

	/**
	 * Makes this move and checks if the pawn has reached the bottom of the board, if so then it replaces that piece with a queen
	 * @throws UnavailableMoveException  if this move isn't available right now
	 */
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
		
		if(isTimeForReplacement())
		{
			//The white/black pawn has reached the bottom/top and now it's time to replace him
			board.replacePawn(myPiece);
		}
		if(getMoveThatIDependUpon() != null)
		{
			//This is the 2-step move for a pawn and when it has been made, it won't be possible again
			getMoveThatIDependUpon().setMoveThatDependsOnMe(null);
			this.removeMove(board);
		}
		if(getMoveDependingOnMe() != null)
		{
			//This is the 1-step move for a pawn, this means that the 2-step move shouldn't be available anymore
			getMoveDependingOnMe().removeMove(board);
			setMoveThatDependsOnMe(null);
		}
	}
	
	private boolean isTimeForReplacement()
	{
		int destinationRow = (myPiece.getAffinity() == Piece.BLACK) ? 0 :(ChessBoard.BOARD_SIZE - 1);
		return myPiece.getCurrentPosition().getRow() == destinationRow;
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{
		//No bounds checking needs to be done because a pawn will be replaced by another piece before then
		Position newPosition = this.getPositionIfPerformed();
		
		Piece pieceAtDestination = board.getPiece(newPosition);
		if(pieceAtDestination == null)
			return true; //The space is free
		
		return false;
	}

}
